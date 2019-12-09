/*
 * Copyright 2011-18 Fraunhofer ISE
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
package org.openmuc.framework.driver.spi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DeviceConnection<C extends ChannelConfigs> extends DeviceContext implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(DeviceConnection.class);

    private final Map<String, C> channels = new HashMap<String, C>();

    private DeviceCallbacks callbacks;

    protected DeviceConnection() {
    }

    protected DeviceConnection(String address, String settings) throws ArgumentSyntaxException {
        doConfigure(address, settings);
    }

    void doCreate(DriverContext context) throws ArgumentSyntaxException {
        if (callbacks == null) {
            callbacks = context.getDriver();
        }
    	super.doCreate(context);
    }

    void doConnect() throws ArgumentSyntaxException, ConnectionException {
        this.onConnect();
        callbacks.onConnected(this);
    }

    protected void onConnect() throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
    }

    @Override
    public final void disconnect() {
        this.onDisconnect();
        callbacks.onDisconnected(this);
        
        for (C channel : channels.values()) {
        	if (channel instanceof ChannelContext) {
                ((ChannelContext) channel).onDestroy();
        	}
        }
        channels.clear();
        onDestroy();
    }

    public void onDisconnect() {
        // Placeholder for the optional implementation
    }

    public final DeviceContext getContext() {
        return this;
    }

    @Override
    public final List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        ChannelScanner scanner = newScanner(settings);
        scanner.doCreate(this);
        
        return scanner.doScan();
    }

    protected ChannelScanner newScanner(String settings) 
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        
        if (!hasChannelScanner()) {
            throw new UnsupportedOperationException();
        }
        return newChannelScanner(settings);
    }

	@Override
    @SuppressWarnings("unchecked")
    public final void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
            List<C> channels = new LinkedList<C>();
            for (ChannelRecordContainer container : containers) {
                try {
                    channels.add((C) newChannel(container));
                    
                } catch (ArgumentSyntaxException e) {
                    logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                    container.setRecord(new Record(null, System.currentTimeMillis(), Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE));
                }
            }
            onStartListening(channels, listener);
        }
    }

    public void onStartListening(List<C> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        for (C channel : channels) {
            if (channel instanceof Channel) {
                ((Channel) channel).doStartListening(listener);
            }
        }
    }

    @Override
    public final Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
            return onRead(getChannels(containers), containerListHandle, samplingGroup);
        }
    }

    public Object onRead(List<C> channels, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        long timestamp = System.currentTimeMillis();
        for (C channel : channels) {
            if (channel instanceof Channel) {
                ((Channel) channel).doRead(timestamp);
            }
        }
        return null;
    }

    @Override
    public final Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {

        synchronized(channels) {
            return onWrite(getChannels(containers), containerListHandle);
        }
    }

    public Object onWrite(List<C> channels, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        for (C channel : channels) {
            if (channel instanceof Channel) {
                ((Channel) channel).doWrite();
            }
        }
        return null;
    }

    private <T extends ChannelContainer> List<C> getChannels(List<T> containers) {
        List<C> channels = new LinkedList<C>();
        for (ChannelContainer container : containers) {
            try {
                channels.add(getChannel(container));
                
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                if (container instanceof ChannelRecordContainer) {
                    ((ChannelRecordContainer) container).setRecord(new Record(null, 
                            System.currentTimeMillis(), Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE));
                }
                else if (container instanceof ChannelValueContainer) {
                    ((ChannelValueContainer) container).setFlag(Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
                }
            }
        }
        return channels;
    }

    @SuppressWarnings("unchecked")
	protected C getChannel(ChannelContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        C channel = channels.get(id);
        if (channel == null) {
            channel = (C) newChannel(container);
            if (channel instanceof Channel) {
                ((Channel) channel).doCreate(this);
            }
            channels.put(id, channel);
        }
        else {
            channel.doConfigure(container);
        }
        return channel;
    }

    protected Channel newChannel(ChannelContainer container) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    	C context = newChannelConfigs(container);
		return newChannel(context);
	}

	protected Channel newChannel(C context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    	return (Channel) context;
    }

}
