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

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.ChannelOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.annotation.Device;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverChannelContext extends Reflectable implements ChannelOptions {

    private static final Logger logger = LoggerFactory.getLogger(DriverChannelContext.class);

    DriverDeviceContext context;

    Class<? extends DriverChannelScanner> scannerClass = DriverChannelScanner.class;
    Class<? extends DriverChannel>        channelClass = DriverChannel.class;

    final Map<String, DriverChannel> channels = new HashMap<String, DriverChannel>();

    DriverChannelContext() {
        bindContext(getClass());
    }

    DriverChannelContext(Class<? extends DriverChannelContext> context) {
        bindContext(context);
    }

    DriverChannelContext bindContext(Class<? extends DriverChannelContext> context) {
        scannerClass = getScannerClass(context);
        channelClass = getChannelClass(context);
        
        return this;
    }

    Device getDeviceAnnotation(Class<? extends DriverChannelContext> context) {
        Device device = context.getAnnotation(Device.class);
        if (device == null) {
            throw new RuntimeException("Implementation invalid without annotation");
        }
        return device;
    }

    @SuppressWarnings("unchecked")
    Class<? extends DriverChannelScanner> getScannerClass(Class<? extends DriverChannelContext> context) {
        if (this instanceof DriverChannelScannerFactory) {
            try {
                Method method = getClass().getMethod("newScanner", Settings.class);
                return (Class<? extends DriverChannelScanner>) method.getReturnType();
                
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        Device device = getDeviceAnnotation(context);
        return device.scanner();
    }

    @SuppressWarnings("unchecked")
    Class<? extends DriverChannel> getChannelClass(Class<? extends DriverChannelContext> context) {
        if (this instanceof DriverChannelFactory) {
            try {
                Method method = getClass().getMethod("newChannel", Address.class, Settings.class);
                return (Class<? extends DriverChannel>) method.getReturnType();
                
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        Device device = getDeviceAnnotation(context);
        return device.channel();
    }

    public final DriverDeviceContext getContext() {
        return context;
    }

    @Override
    public final Options getAddressOptions() {
        Options channelAddress = null;
        if (channelClass != DriverChannel.class) {
            channelAddress = Options.parse(ADDRESS, channelClass);
        }
        return channelAddress;
    }

    @Override
    public final Options getSettingsOptions() {
        Options channelSettings = null;
        if (channelClass != DriverChannel.class) {
            channelSettings = Options.parse(SETTING, channelClass);
        }
        return channelSettings;
    }

    @Override
    public final Options getScanSettingsOptions() {
        Options scanSettings = null;
        if (scannerClass != DriverChannelScanner.class) {
            scanSettings = Options.parse(SETTING, scannerClass);
        }
        return scanSettings;
    }

    final DriverChannelScanner newScanner(String settingsStr) throws RuntimeException, ArgumentSyntaxException {
        DriverChannelScanner scanner;
        
        Settings settings = Configurations.parseSettings(settingsStr, scannerClass);
        
        if (this instanceof DriverChannelScannerFactory) {
            scanner = ((DriverChannelScannerFactory) this).newScanner(settings);
        }
        else if (scannerClass != DriverChannelScanner.class) {
            scanner = newInstance(scannerClass);
        }
        else {
            return null;
        }
        scanner.invokeConfigure(this, settings);
        
        return scanner;
    }

    @SuppressWarnings("unchecked")
    final <C extends DriverChannel> DriverChannel newChannel(ChannelTaskContainer container) 
            throws RuntimeException, ArgumentSyntaxException {
        
        Address address = Configurations.parseAddress(container.getChannelAddress(), channelClass);
        Settings settings = Configurations.parseSettings(container.getChannelSettings(), channelClass);
        
        C channel;
        if (this instanceof DriverChannelFactory) {
            channel = (C) ((DriverChannelFactory) this).newChannel(address, settings);
        }
        else {
            channel = (C) newInstance(channelClass);
        }
        return channel;
    }

    final <C extends DriverChannel> DriverChannel getChannel(ChannelTaskContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        DriverChannel channel = channels.get(id);
        try {
            if (channel == null) {
                channel = newChannel(container);
                channels.put(id, channel);
            }
            channel.invokeConfigure(this, container);
            
        } catch (ArgumentSyntaxException e) {
            channels.remove(id);
            
            throw e;
        }
        return channel;
    }

    public final DriverChannel getChannel(String id) {
        return channels.get(id);
    }

    public final List<DriverChannel> getChannels() {
        return (List<DriverChannel>) channels.values();
    }

    @SuppressWarnings("unchecked")
    final <C extends DriverChannel> List<C> getChannels(List<? extends ChannelTaskContainer> containers) {
        List<C> channels = new ArrayList<C>();
        for (ChannelTaskContainer container : containers) {
            try {
                channels.add((C) getChannel(container));
                
            } catch (ArgumentSyntaxException | NullPointerException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                
                setChannelContainerFlag(container, Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
            }
        }
        return channels;
    }

    @SuppressWarnings("unchecked")
    final <C extends DriverChannel> List<C> newChannels(List<? extends ChannelTaskContainer> containers) {
        List<C> channels = new ArrayList<C>();
        for (ChannelTaskContainer container : containers) {
            C channel;
            try {
                channel = (C) newChannel(container);
                channel.invokeConfigure(this, container);
                channels.add(channel);
                
            } catch (ArgumentSyntaxException | NullPointerException e) {
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
