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
package org.openmuc.framework.driver;

import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Device<C extends Channel> extends DeviceConfigs<C> implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(Device.class);

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
        
        for (C channel : channels.values()) {
        	if (channel instanceof ChannelContext) {
                ((ChannelContext) channel).onDestroy();
        	}
        }
        channels.clear();
        onDestroy();
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

    protected ChannelScanner newScanner(String settings) 
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        
        if (!context.hasChannelScanner()) {
            throw new UnsupportedOperationException();
        }
        return context.newChannelScanner();
    }

	@Override
    public final void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
            List<C> channels = new LinkedList<C>();
            for (ChannelRecordContainer container : containers) {
                try {
                    channels.add((C) onCreateChannel(container));
                    
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
            channel.doStartListening(listener);
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
            channel.doRead(timestamp);
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
            channel.doWrite();
        }
        return null;
    }

}
