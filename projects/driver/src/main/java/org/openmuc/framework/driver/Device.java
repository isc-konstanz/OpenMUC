package org.openmuc.framework.driver;

import java.util.List;

import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;

public abstract class Device<C extends ChannelContainer> extends DeviceConnection {

    @Override
    @SuppressWarnings("unchecked")
    protected Object onStartListening(List<ChannelContainer> channels, Object containerListHandle, 
    		RecordsReceivedListener listener) throws UnsupportedOperationException, ConnectionException {
        this.onStartListening((List<C>) channels, listener);
        return null;
    }

    protected void onStartListening(List<C> channels, 
    		RecordsReceivedListener listener) throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        for (ChannelContainer channel : channels) {
            channel.doStartListening(listener);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object onRead(List<ChannelContainer> channels, Object handle, String samplingGroup)
            throws  ConnectionException {
        this.onRead((List<C>) channels, samplingGroup);
        return null;
    }

    protected void onRead(List<C> channels, String samplingGroup) throws ConnectionException {
        // Placeholder for the optional implementation
        long timestamp = System.currentTimeMillis();
        for (ChannelContainer channel : channels) {
            channel.doRead(timestamp);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object onWrite(List<ChannelContainer> channels, Object handle)
            throws ConnectionException {
        this.onWrite((List<C>) channels);
        return null;
    }

    protected void onWrite(List<C> channels) throws ConnectionException {
        // Placeholder for the optional implementation
        for (ChannelContainer channel : channels) {
            channel.doWrite();
        }
    }
}
