/*
 * Copyright 2011-2021 Fraunhofer ISE
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

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Listen;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.annotation.Write;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;

public abstract class DriverDevice extends DriverChannelContext implements Connection {

//    TODO: Implement initial configuration of all available channels of this device
//    @Override
//    public final void configureChannels(List<DriverChannel> channels) {
//        
//    }

    void invokeConfigure(DriverDeviceContext context, 
            Address address, Settings settings) throws ArgumentSyntaxException {
        this.configure(address);
        this.configure(settings);
        this.context = context;
        
        invokeMethod(Configure.class, this, context, address, settings);
        invokeMethod(Configure.class, this, context);
        invokeMethod(Configure.class, this);
    }

    void invokeConnect() throws ConnectionException {
        invokeMethod(Connect.class, this);
    }

    void invokeDisconnect() {
        invokeMethod(Disconnect.class, this);
    }

    @Override
    public final void disconnect() {
        this.invokeDisconnect();
        context.invokeDisconnect(this);
        channels.clear();
    }

    @Override
    public final List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        if (scannerClass == DriverChannelScanner.class) {
            throw new UnsupportedOperationException("Scanning channels unsupported for " + getClass().getSimpleName());
        }
        DriverChannelScanner scanner = newScanner(settings);
        
        return scanner.scan();
    }

    @Override
    public final void startListening(List<ChannelRecordContainer> containers, 
            RecordsReceivedListener listener) throws UnsupportedOperationException, ConnectionException {
        
        synchronized(channels) {
            List<DriverChannel> channels = newChannels(containers);
            
            if (hasMethod(Listen.class, this, channels, listener)) {
                invokeMethod(Listen.class, this, channels, listener);
            }
            else if (hasMethod(Listen.class, channelClass, listener)) {
                for (DriverChannel driverChannel : channels) {
                    driverChannel.invokeListening(listener);
                }
            }
            else {
                throw new UnsupportedOperationException("Listening for values unsupported for " + getClass().getSimpleName());
            }
        }
    }

    @Override
    public final Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        long timestamp = System.currentTimeMillis();

        synchronized(channels) {
            List<DriverChannel> channels = getChannels(containers);
            
            if (hasMethod(Read.class, this, channels, samplingGroup)) {
                invokeMethod(Read.class, this, channels, samplingGroup);
            }
            else if (hasMethod(Read.class, channelClass, timestamp)) {
                for (DriverChannel driverChannel : channels) {
                    driverChannel.invokeRead(timestamp);
                }
            }
            else {
                throw new UnsupportedOperationException("Reading values unsupported for " + getClass().getSimpleName());
            }
        }
        return null;
    }

    @Override
    public final Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {

        synchronized(channels) {
            List<DriverChannel> channels = getChannels(containers);
            
            if (hasMethod(Write.class, this, channels)) {
                invokeMethod(Write.class, this, channels);
            }
            else if (hasMethod(Write.class, channelClass)) {
                for (DriverChannel driverChannel : channels) {
                    driverChannel.invokeWrite();
                }
            }
            else {
                throw new UnsupportedOperationException("Writing values unsupported for " + getClass().getSimpleName());
            }
        }
        return null;
    }

}
