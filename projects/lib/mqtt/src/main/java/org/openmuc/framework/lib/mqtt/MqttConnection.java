package org.openmuc.framework.lib.mqtt;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openmuc.framework.lib.ssl.SslManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;

public class MqttConnection {
    private static final Logger logger = LoggerFactory.getLogger(MqttConnection.class);
    private final MqttSettings settings;

    private final Mqtt3ClientBuilder clientBuilder;
    private Mqtt3AsyncClient client;

    public MqttConnection(MqttSettings settings) {
        this.settings = settings;
        clientBuilder = getClientBuilder();
        client = buildClient();
    }

    public void connect() {
        client = buildClient();
        if (settings.getUsername() == null) {
            client.connect().whenComplete((ack, e) -> {
                if (e != null) {
                    logger.error("Something went wrong while connecting: {}", e.getMessage());
                }
                else {
                    logger.info("Connected to MQTT broker {}:{}", settings.getHost(), settings.getPort());
                }
            });
        }
        else {
            client.connectWith()
                    .simpleAuth()
                    .username(settings.getUsername())
                    .password(settings.getPassword().getBytes())
                    .applySimpleAuth()
                    .send()
                    .whenComplete((ack, e) -> {
                        if (e != null) {
                            logger.error("Something went wrong while connecting: {}", e.getMessage());
                        }
                        else {
                            logger.debug("Connected to MQTT broker {}:{}", settings.getHost(), settings.getPort());
                        }
                    });
        }
        client.connect();
    }

    public void disconnect() {
        client.disconnect();
    }

    public void addConnectedListener(MqttClientConnectedListener listener) {
        clientBuilder.addConnectedListener(listener);
    }

    public void addDisconnectedListener(MqttClientDisconnectedListener listener) {
        clientBuilder.addDisconnectedListener(listener);
    }

    public Mqtt3AsyncClient getClient() {
        return client;
    }

    public MqttSettings getSettings() {
        return settings;
    }

    private Mqtt3ClientBuilder getClientBuilder() {
        Mqtt3ClientBuilder clientBuilder = Mqtt3Client.builder()
                .identifier(UUID.randomUUID().toString())
                .automaticReconnectWithDefaultConfig()
                .addDisconnectedListener(context -> {
                    if (context.getClientConfig().getState() == MqttClientState.CONNECTING) {
                        context.getReconnector().reconnect(false);
                    }
                })
                .serverHost(settings.getHost())
                .serverPort(settings.getPort());
        if (settings.isSsl()) {
            MqttClientSslConfig sslConfig = MqttClientSslConfig.builder()
                    .keyManagerFactory(SslManager.getInstance().getKeyManagerFactory())
                    .trustManagerFactory(SslManager.getInstance().getTrustManagerFactory())
                    .handshakeTimeout(10, TimeUnit.SECONDS)
                    .build();

            clientBuilder.sslConfig(sslConfig);
            return clientBuilder;
        }
        else {
            return clientBuilder;
        }
    }

    private Mqtt3AsyncClient buildClient() {
        return clientBuilder.buildAsync();
    }
}
