package org.openmuc.framework.lib.mqtt;

public class MqttSettings {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean ssl;
    private final long maxBufferSize;
    private final long maxFileSize;
    private final int maxFileCount;
    private final String topic;

    public MqttSettings(String host, int port, String username, String password, boolean ssl, long maxBufferSize,
            long maxFileSize, int maxFileCount, String topic) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ssl = ssl;
        this.maxBufferSize = maxBufferSize;
        this.maxFileSize = maxFileSize;
        this.maxFileCount = maxFileCount;
        this.topic = topic;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public long getMaxBufferSize() {
        return maxBufferSize;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public int getMaxFileCount() {
        return maxFileCount;
    }

    public String getTopic() {
        return topic;
    }
}
