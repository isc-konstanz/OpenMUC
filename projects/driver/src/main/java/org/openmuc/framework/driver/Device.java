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
package org.openmuc.framework.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Device<C extends DeviceChannel> extends ChannelContext implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(Device.class);

    public static interface Callbacks {

        void onConnect(Connection connection);

        void onDisconnect(Connection connection);
    }

    private final Map<String, C> channels = new HashMap<String, C>();

    final void doCreate(DeviceContext context) {
        this.context = context;
        this.onCreate(context);
        this.onCreate();
    }

    protected void onCreate(DeviceContext context) {
        // Placeholder for the optional implementation
    }

    protected void onCreate() {
        // Placeholder for the optional implementation
    }

    protected void onDestroy() {
        // Placeholder for the optional implementation
    }

    final void doConfigure(String address, String settings) throws ArgumentSyntaxException {
        configure(address, settings);
        onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    void doConnect() throws ArgumentSyntaxException, ConnectionException {
        this.onConnect();
        context.onConnect(this);
    }

    protected void onConnect() throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
    }

    @Override
    public final void disconnect() {
        this.onDisconnect();
        context.onDisconnect(this);
        
        for (DeviceChannel channel : channels.values()) {
            channel.onDestroy();
        }
        channels.clear();
        this.onDestroy();
    }

    protected void onDisconnect() {
        // Placeholder for the optional implementation
    }

    @Override
    public final List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        ChannelScanner scanner = newScanner(settings);
        scanner.doCreate(this);
        scanner.doConfigure(settings);
        
        return scanner.doScan();
    }

    @Override
    public final void startListening(List<ChannelRecordContainer> containers, 
    		RecordsReceivedListener listener) throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
        	onStartListening(newChannels(containers), listener);
        }
    }

    protected void onStartListening(List<C> channels, 
    		RecordsReceivedListener listener) throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        for (DeviceChannel channel : channels) {
            channel.doStartListening(listener);
        }
    }

    @Override
    public final Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
            onRead(getChannels(containers), samplingGroup);
        }
        return null;
    }

	protected void onRead(List<C> channels, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        long timestamp = System.currentTimeMillis();
        for (DeviceChannel channel : channels) {
            channel.doRead(timestamp);
        }
    }

    @Override
    public final Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {

        synchronized(channels) {
            onWrite(getChannels(containers));
        }
        return null;
    }

    protected void onWrite(List<C> channels)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        for (DeviceChannel channel : channels) {
            channel.doWrite();
        }
    }

    @SuppressWarnings("unchecked")
	final DeviceChannel getChannel(ChannelTaskContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        C channel = channels.get(id);
        try {
            if (channel == null) {
                channel = (C) newChannel(container);
                channel.doCreate(this);
                channel.doConfigure(container);
                
                channels.put(id, channel);
            }
            else {
	            channel.doConfigure(container);
        	}
        } catch (ArgumentSyntaxException e) {
        	
            channels.remove(id);
            
            throw e;
        }
        return channel;
    }

    public List<C> getChannels() {
        return (List<C>) channels.values();
    }

    @SuppressWarnings("unchecked")
	final List<C> getChannels(List<? extends ChannelTaskContainer> containers) {
        List<C> channels = new ArrayList<C>();
        for (ChannelTaskContainer container : containers) {
            try {
				channels.add((C) getChannel(container));
				
			} catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                
            	setChannelContainerFlag(container, Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
			}
        }
        return channels;
    }

	@SuppressWarnings("unchecked")
	final List<C> newChannels(List<? extends ChannelTaskContainer> containers) {
        List<C> channels = new ArrayList<C>();
        for (ChannelTaskContainer container : containers) {
        	C channel;
            try {
                channel = (C) newChannel(container);
                channel.doCreate(this);
                channel.doConfigure(container);
                
                channels.add(channel);
                
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                
            	setChannelContainerFlag(container, Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
            }
        }
        return channels;
    }

    private void setChannelContainerFlag(ChannelTaskContainer container, Flag flag) {
        if (container instanceof ChannelRecordContainer) {
        	setChannelContainerFlag((ChannelRecordContainer) container, flag);
        }
        else if (container instanceof ChannelValueContainer) {
        	setChannelContainerFlag((ChannelValueContainer) container, flag);
        }
    }

    private void setChannelContainerFlag(ChannelRecordContainer container, Flag flag) {
        container.setRecord(new Record(Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE));
    }

    private void setChannelContainerFlag(ChannelValueContainer container, Flag flag) {
        container.setFlag(Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
    }

}
