package org.openmuc.framework.lib.mqtt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;

@ExtendWith(MockitoExtension.class)
public class MqttWriterTest {

    private static MqttClientConnectedListener connectedListener;
    private static MqttClientDisconnectedListener disconnectedListener;
    private MqttWriter mqttWriter;

    @BeforeEach
    void setup() {
        MqttConnection connection = mock(MqttConnection.class);

        doAnswer((Answer<Void>) invocation -> {
            connectedListener = invocation.getArgument(0);
            return null;
        }).when(connection).addConnectedListener(any(MqttClientConnectedListener.class));

        doAnswer((Answer<Void>) invocation -> {
            disconnectedListener = invocation.getArgument(0);
            return null;
        }).when(connection).addDisconnectedListener(any(MqttClientDisconnectedListener.class));

        when(connection.getSettings())
                .thenReturn(new MqttSettings("localhost", 1883, null, null, false, 1024, 1024, 2, "test"));

        mqttWriter = new MqttWriterStub(connection);
        connectedListener.onConnected(() -> null);
    }

    @Test
    void testWriteWithReconnectionAndSimulatedDisconnection() throws IOException {
        MqttClientDisconnectedContext disconnectedContext = mock(MqttClientDisconnectedContext.class);
        disconnectedListener.onDisconnected(disconnectedContext);

        File file = FileSystems.getDefault().getPath("data", "mqtt", "messageBuffer").toFile();
        File file1 = FileSystems.getDefault().getPath("data", "mqtt", "messageBuffer.1").toFile();

        String message300bytes = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula "
                + "eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur "
                + "ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat"
                + " massa quis enim. Donec.";

        mqttWriter.write(message300bytes.getBytes()); // 300
        mqttWriter.write(message300bytes.getBytes()); // 600
        mqttWriter.write(message300bytes.getBytes()); // 900
        // buffer limit not yet reached
        assertFalse(file.exists() || file1.exists());
        mqttWriter.write(message300bytes.getBytes()); // 1200 > 1024 write to file => 0
        // buffer limit reached, first file written
        assertTrue(file.exists() && !file1.exists());
        mqttWriter.write(message300bytes.getBytes()); // 300
        mqttWriter.write(message300bytes.getBytes()); // 600
        mqttWriter.write(message300bytes.getBytes()); // 900
        // buffer limit not yet reached second time
        assertTrue(file.exists() && !file1.exists());
        mqttWriter.write(message300bytes.getBytes()); // 1200 > 1024 write to file
        // buffer limit reached, second file written
        assertTrue(file.exists() && file1.exists());

        // simulate connection
        connectedListener.onConnected(() -> null);

        // files should be emptied and therefore removed
        assertFalse(file.exists() || file1.exists());
    }
}
