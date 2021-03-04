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

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;

public abstract class Device<C extends DeviceChannel> extends ChannelContext implements Connection {

    public static interface Callbacks {

        void onConnect(Connection connection);

        void onDisconnect(Connection connection);
    }

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
    @SuppressWarnings("unchecked")
    public final void startListening(List<ChannelRecordContainer> containers, 
    		RecordsReceivedListener listener) throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
        	onStartListening((List<C>) newChannels(containers), listener);
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
    @SuppressWarnings("unchecked")
    public final Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
            onRead((List<C>) getChannels(containers), samplingGroup);
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
    @SuppressWarnings("unchecked")
    public final Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {

        synchronized(channels) {
            onWrite((List<C>) getChannels(containers));
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

}
