package org.openmuc.framework.lib.mqtt;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.openmuc.framework.lib.filePersistence.FilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

public class MqttWriter {
    private static final Logger logger = LoggerFactory.getLogger(MqttWriter.class);
    private static final Queue<MessageTuple> MESSAGE_BUFFER = new LinkedList<>();

    private final MqttConnection connection;
    private final long maxBufferSize;
    private final FilePersistence filePersistence;
    private boolean connected = false;
    private long currentBufferSize = 0L;

    public MqttWriter(MqttConnection connection) {
        this.connection = connection;
        addConnectedListener();
        addDisconnectedListener();
        maxBufferSize = connection.getSettings().getMaxBufferSize();
        MqttSettings settings = connection.getSettings();
        filePersistence = new FilePersistence("data/mqtt", settings.getMaxFileCount(), settings.getMaxFileSize());
    }

    private void addConnectedListener() {
        connection.addConnectedListener(context -> {
            connected = true;
            emptyMessageBuffer();
            emptyFilePersistence();
        });
    }

    private void addDisconnectedListener() {
        connection.addDisconnectedListener(context -> {
            connected = false;
        });
    }

    public void write(byte[] message) {
        if (connected) {
            startPublishing(message);
        }
        else {
            logger.debug("Not connected. Adding message to buffer");
            addToMessageBuffer(message);
        }
    }

    private void startPublishing(byte[] message) {
        publish(message).whenComplete((publish, exception) -> {
            if (exception != null) {
                logger.debug("A message could not be sent. Adding message to buffer");
                addToMessageBuffer(message);
            }
        });
    }

    CompletableFuture<Mqtt3Publish> publish(byte[] message) {
        return connection.getClient().publishWith().topic(connection.getSettings().getTopic()).payload(message).send();
    }

    public MqttConnection getConnection() {
        return connection;
    }

    private void addToMessageBuffer(byte[] message) {
        System.out.println("--------");
        System.out.println("currentBufferSize: " + currentBufferSize);
        if (currentBufferSize + message.length <= maxBufferSize) {
            currentBufferSize += message.length;
            System.out.println("A) maxBufferSize: " + maxBufferSize + "  " + "currentBufferSize: " + currentBufferSize);
        }
        else if (maxBufferSize > 0) {
            System.out.println("B) maxBufferSize: " + maxBufferSize + "  " + "currentBufferSize: " + currentBufferSize);
            addToFilePersistence();
        }
        MESSAGE_BUFFER.add(new MessageTuple(connection.getSettings().getTopic(), message));
        System.out.println("currentBufferSize end:" + currentBufferSize);
    }

    private void emptyMessageBuffer() {
        while (!MESSAGE_BUFFER.isEmpty()) {
            MessageTuple messageTuple = MESSAGE_BUFFER.remove();
            if (logger.isTraceEnabled()) {
                logger.trace("resending buffered message");
            }
            write(messageTuple.message);
        }
    }

    private void addToFilePersistence() {
        while (!MESSAGE_BUFFER.isEmpty()) {
            MessageTuple messageTuple = MESSAGE_BUFFER.remove();
            logger.trace("adding message to file buffer");
            logToFile(messageTuple);
        }
        currentBufferSize = 0;
    }

    private void logToFile(MessageTuple messageTuple) {
        try {
            filePersistence.fileLog("messageBuffer", messageTuple.message);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void emptyFilePersistence() {
        try {
            byte[] message = filePersistence.emptyFile("messageBuffer");
            while (message != null) {
                logger.trace("resending file buffered message");
                write(message);
                message = filePersistence.emptyFile("messageBuffer");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static class MessageTuple {
        private final String topic;
        private final byte[] message;

        private MessageTuple(String topic, byte[] message) {
            this.topic = topic;
            this.message = message;
        }
    }
}
