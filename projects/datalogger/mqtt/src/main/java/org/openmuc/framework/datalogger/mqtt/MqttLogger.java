/*
 * Copyright 2011-2020 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openmuc.framework.datalogger.mqtt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.openmuc.framework.lib.ssl.SslManager;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;

@Component
public class MqttLogger implements DataLoggerService {
    private static final Logger logger = LoggerFactory.getLogger(MqttLogger.class);
    private static final List<String> LOGGED_CHANNELS = new LinkedList<>();
    private static final HashMap<String, ParserService> PARSERS = new HashMap<>();
    private static final Queue<byte[]> MESSAGE_BUFFER = new LinkedList<>();
    private String topic;
    private String parser;
    private boolean logMultiple;
    private Mqtt3AsyncClient client;
    private boolean connected = false;

    @Activate
    public void activate(ComponentContext context) {
        logger.info("Activating MQTT logger");
        connect();
        listenForParser(context.getBundleContext());
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        logger.info("Deactivating MQTT logger");
        client.disconnect();
    }

    @Override
    public String getId() {
        return "mqttlogger";
    }

    @Override
    public void setChannelsToLog(List<LogChannel> channels) {
        LOGGED_CHANNELS.clear();

        for (LogChannel channel : channels) {
            LOGGED_CHANNELS.add(channel.getId());
        }
    }

    @Override
    public void log(List<LogRecordContainer> containers, long timestamp) {
        List<LogRecordContainer> loggedContainers = new ArrayList<>();

        for (LogRecordContainer container : containers) {
            if (LOGGED_CHANNELS.contains(container.getChannelId())) {
                if (logMultiple && container.getRecord().getValue() != null) {
                    loggedContainers.add(container);
                    continue;
                }
                parse(Collections.singletonList(container));
            }
        }
        if (logMultiple && !loggedContainers.isEmpty()) {
            parse(loggedContainers);
        }
    }

    /**
     * Parses a message with {@link ParserService} and publishes it
     * 
     * @param containers
     *            List of {@link LogRecordContainer}
     */
    private void parse(List<LogRecordContainer> containers) {
        byte[] message;
        if (PARSERS.containsKey(parser)) {
            try {
                message = PARSERS.get(parser).serialize(containers);
                publish(message);
                if (logger.isTraceEnabled()) {
                    logger.trace(new String(message));
                }
            } catch (SerializationException e) {
                logger.error(e.getMessage());
            }
        }
        else {
            logger.error("No parser available!");
        }
    }

    @Override
    public void logEvent(List<LogRecordContainer> containers, long timestamp) {
        log(containers, timestamp);
    }

    @Override
    public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads settings from system.properties and connects to MQTT Broker
     */
    void connect() {
        String packageName = MqttLogger.class.getPackage().getName().toLowerCase();
        String host = System.getProperty(packageName + ".host");
        int port = Integer.parseInt(System.getProperty(packageName + ".port"));
        boolean ssl = Boolean.parseBoolean(System.getProperty(packageName + ".ssl"));
        String username = System.getProperty(packageName + ".username");
        String password = System.getProperty(packageName + ".password");
        topic = System.getProperty(packageName + ".topic");
        parser = System.getProperty(packageName + ".parser");
        logMultiple = Boolean.parseBoolean(System.getProperty(packageName + ".multiple"));

        Mqtt3ClientBuilder clientBuilder = Mqtt3Client.builder()
                .identifier(UUID.randomUUID().toString())
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener(context -> {
                    while (!MESSAGE_BUFFER.isEmpty()) {
                        publish(MESSAGE_BUFFER.remove());
                    }
                })
                .addDisconnectedListener(context -> {
                    if (context.getClientConfig().getState() == MqttClientState.CONNECTING) {
                        context.getReconnector().reconnect(false);
                    }
                })
                .serverHost(host)
                .serverPort(port);

        if (ssl) {
            try {
                MqttClientSslConfig sslConfig = MqttClientSslConfig.builder()
                        .keyManagerFactory(SslManager.getInstance().getKeyManagerFactory())
                        .trustManagerFactory(SslManager.getInstance().getTrustManagerFactory())
                        .handshakeTimeout(10, TimeUnit.SECONDS)
                        .build();

                client = clientBuilder.sslConfig(sslConfig).buildAsync();
            } catch (Exception e) {
                logger.error("Couldn't connect with SSL enabled: {}", e.getMessage());
            }
        }
        else {
            client = clientBuilder.buildAsync();
        }

        if (username == null || password == null) {
            client.connect().whenComplete((ack, e) -> {
                if (e != null) {
                    logger.error("Something went wrong while connecting: {}", e.getMessage());
                }
                else {
                    logger.info("Connected to MQTT broker {}", host);
                    connected = true;
                }
            });
        }
        else {
            client.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .send()
                    .whenComplete((ack, e) -> {
                        if (e != null) {
                            logger.error("Something went wrong while connecting: {}", e.getMessage());
                        }
                        else {
                            logger.debug("Connected to MQTT broker {}", host);
                            connected = true;
                        }
                    });
        }
    }

    private void listenForParser(BundleContext context) {
        String filter = '(' + Constants.OBJECTCLASS + '=' + ParserService.class.getName() + ')';
        try {
            context.addServiceListener(event -> {
                ServiceReference<?> serviceReference = event.getServiceReference();
                String parserId = (String) serviceReference.getProperty("parserID");
                ParserService parser = (ParserService) context.getService(serviceReference);

                if (event.getType() == ServiceEvent.UNREGISTERING) {
                    logger.info("{} unregistering, removing Parser", parser.getClass().getName());
                    PARSERS.remove(parserId);
                }
                else {
                    logger.info("{} changed, updating Parser", parser.getClass().getName());
                    PARSERS.put(parserId, parser);
                }
            }, filter);
        } catch (InvalidSyntaxException e) {
            logger.error("Service listener can't be added to framework", e);
        }
    }

    /**
     * publishes a message to the MQTT broker
     * 
     * @param message
     *            the message to be published
     */
    void publish(byte[] message) {
        logger.info(new String(message));
        if (connected) {
            client.publishWith().topic(topic).payload(message).send().whenComplete((publish, e) -> {
                if (e != null) {
                    logger.debug("A message could not be sent. Adding message to buffer");
                    MESSAGE_BUFFER.add(message);
                }
            });
        }
        else {
            logger.debug("Not connected to broker yet. Adding message to buffer");
            MESSAGE_BUFFER.add(message);
        }
    }
}
