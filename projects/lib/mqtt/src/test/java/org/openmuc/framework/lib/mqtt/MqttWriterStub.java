package org.openmuc.framework.lib.mqtt;

import java.util.concurrent.CompletableFuture;

import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

/**
 * MqttWriter stub that simulates successful publishes when connection is simulated as connected
 */
public class MqttWriterStub extends MqttWriter {
    public MqttWriterStub(MqttConnection connection) {
        super(connection);
    }

    @Override
    CompletableFuture<Mqtt3Publish> publish(byte[] message) {
        CompletableFuture<Mqtt3Publish> future = new CompletableFuture<>();
        future.complete(Mqtt3Publish.builder().topic("test").build());
        return future;
    }
}
