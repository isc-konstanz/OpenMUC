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

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.ChannelOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.annotation.Factory;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelContext extends Configurable implements ChannelOptions, ChannelFactory, ChannelScannerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChannelContext.class);

    DeviceContext context;

    Class<? extends ChannelScanner> scannerClass;

    Class<? extends DeviceChannel> channelClass;

    final Map<String, DeviceChannel> channels = new HashMap<String, DeviceChannel>();

    ChannelContext() {
        bindContext(getClass());
    }

    ChannelContext(Class<? extends ChannelContext> context) {
        bindContext(context);
    }

    @SuppressWarnings("unchecked")
	ChannelContext bindContext(Class<? extends ChannelContext> context) {
        Factory factory = context.getAnnotation(Factory.class);
        if (factory != null) {
            scannerClass = (Class<? extends ChannelScanner>) factory.scanner();
        }
        this.channelClass = getChannelClass(context);
        
        return this;
    }

    @SuppressWarnings("unchecked")
	private Class<? extends DeviceChannel> getChannelClass(Class<?> deviceClass) {
        while (deviceClass.getSuperclass() != null) {
            if (deviceClass.getSuperclass().equals(Device.class)) {
                break;
            }
            deviceClass = deviceClass.getSuperclass();
        }
        // This operation is safe. Because deviceClass is a direct sub-class, getGenericSuperclass() will
        // always return the Type of this class. Because this class is parameterized, the cast is safe
        ParameterizedType superClass = (ParameterizedType) deviceClass.getGenericSuperclass();
        return (Class<? extends DeviceChannel>) superClass.getActualTypeArguments()[0];
    }

    public final DeviceContext getContext() {
        return context;
    }

    @Override
    public final Options getAddressOptions() {
        Options channelAddress = null;
        if (channelClass != null) {
            channelAddress = Options.parseAddress(channelClass);
        }
        return channelAddress;
    }

    @Override
    public final Options getSettingsOptions() {
        Options channelSettings = null;
        if (channelClass != null) {
            channelSettings = Options.parseSettings(channelClass);
        }
        return channelSettings;
    }

    @Override
    public final Options getScanSettingsOptions() {
        Options scanSettings = null;
        if (scannerClass != null && !scannerClass.equals(ChannelScanner.class)) {
            scanSettings = Options.parseSettings(scannerClass);
        }
        return scanSettings;
    }

    public ChannelScanner newScanner(String settings) throws ArgumentSyntaxException {
        return this.newScanner(Configurations.parseSettings(settings, scannerClass));
    }

    @Override
    public ChannelScanner newScanner(Settings settings) throws ArgumentSyntaxException {
        return this.newScanner();
    }

    protected ChannelScanner newScanner() {
        return DriverContext.newInstance(scannerClass);
    }

    final void bindScanner(Class<? extends ChannelScanner> scannerClass) {
        this.scannerClass = scannerClass;
    }

    public DeviceChannel getChannel(String id) {
    	return channels.get(id);
    }

	final DeviceChannel getChannel(ChannelTaskContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        DeviceChannel channel = channels.get(id);
        try {
            if (channel == null) {
                channel = newChannel(container);
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

    final DeviceChannel newChannel(ChannelTaskContainer container) throws ArgumentSyntaxException {
        return this.newChannel(container.getChannel().getAddress(), 
                               container.getChannel().getSettings());
    }

    public DeviceChannel newChannel(String address, String settings) throws ArgumentSyntaxException {
        return this.newChannel(Configurations.parseAddress(address, channelClass),
                              Configurations.parseSettings(settings, channelClass));
    }

    @Override
    public DeviceChannel newChannel(Address address, Settings settings) throws ArgumentSyntaxException {
        return this.newChannel();
    }

    protected DeviceChannel newChannel() {
        return DriverContext.newInstance(channelClass);
    }

    final void bindChannel(Class<? extends DeviceChannel> channelClass) {
        this.channelClass = channelClass;
    }

    public List<DeviceChannel> getChannels() {
        return (List<DeviceChannel>) channels.values();
    }

	final List<DeviceChannel> getChannels(List<? extends ChannelTaskContainer> containers) {
        List<DeviceChannel> channels = new ArrayList<DeviceChannel>();
        for (ChannelTaskContainer container : containers) {
            try {
				channels.add(getChannel(container));
				
			} catch (ArgumentSyntaxException | NullPointerException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                
            	setChannelContainerFlag(container, Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
			}
        }
        return channels;
    }

	final List<DeviceChannel> newChannels(List<? extends ChannelTaskContainer> containers) {
        List<DeviceChannel> channels = new ArrayList<DeviceChannel>();
        for (ChannelTaskContainer container : containers) {
        	DeviceChannel channel;
            try {
                channel = newChannel(container);
                channel.doCreate(this);
                channel.doConfigure(container);
                
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
