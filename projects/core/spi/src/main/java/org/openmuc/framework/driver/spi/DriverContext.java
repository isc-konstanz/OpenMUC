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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.options.Configurable;
import org.openmuc.framework.options.DriverInfoFactory;
import org.openmuc.framework.options.DriverOptions;
import org.openmuc.framework.options.Options;

public abstract class DriverContext implements DriverService {

	final DriverOptions info;

	Class<? extends DeviceContext> device = null;
	Class<? extends Configurable> deviceScanner = null;

	Class<? extends ChannelContext> channel = null;
	Class<? extends Configurable> channelScanner = null;

	@SuppressWarnings("unchecked")
	protected DriverContext() {
        this.info = DriverInfoFactory.getInfo(getId());
        setDevice((Class<? extends DeviceContext>) getType(this.getClass(), Driver.class, DriverContext.class));
        setChannel((Class<? extends ChannelContext>) getType(device, DeviceConnection.class, DeviceContext.class));
    }

	private Type getType(Class<?> clazz, Class<?> type, Class<?> context) {
		while (clazz.getSuperclass() != null) {
			if (clazz.getSuperclass().equals(type) || clazz.getSuperclass().equals(context)) {
				break;
			}
            clazz = clazz.getSuperclass();
		}
        // This operation is safe. Because clazz is a direct sub-class, getGenericSuperclass() will
        // always return the Type of this class. Because this class is parameterized, the cast is safe
        ParameterizedType superclass = (ParameterizedType) clazz.getGenericSuperclass();
        return superclass.getActualTypeArguments()[0];
	}

    /**
     * Returns the ID of the driver. The ID may only contain ASCII letters, digits, hyphens and underscores. By
     * convention the ID should be meaningful and all lower case letters (e.g. "mbus", "modbus").
     * 
     * @return the unique ID of the driver.
     */
    public abstract String getId();

    public final String getName() {
        return info.getName();
    }

    public final DriverContext setName(String name) {
    	info.setName(name);
        return this;
    }

    public final String getDescription() {
        return info.getDescription();
    }

    public final DriverContext setDescription(String description) {
    	info.setDescription(description);
        return this;
    }

    @Override
    public DriverInfo getInfo() {
    	return info;
    }

    public abstract Driver<?> getDriver();

	@SuppressWarnings("unchecked")
	<D extends DeviceConfigs> D newDeviceConfigs(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
		D device;
		try {
			device = (D) this.device.getDeclaredConstructor().newInstance();
			device.doConfigure(address, settings);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to instance {0}: {1}", 
            		deviceScanner.getSimpleName(), e.getMessage()));
		}
		return device;
	}

    public final DriverContext setDevice(Class<? extends DeviceContext> device) {
    	info.setDeviceAddress(Options.parseAddress(device));
    	info.setDeviceSettings(Options.parseSettings(device));
        this.device = device;
        return this;
    }

    public final DriverContext setDeviceAddress(Class<? extends Configurable> configs) {
    	info.setDeviceAddress(Options.parseAddress(configs));
        return this;
    }

    public final DriverContext setDeviceSettings(Class<? extends Configurable> configs) {
    	info.setDeviceSettings(Options.parseSettings(configs));
        return this;
    }

    boolean hasDeviceScanner() {
    	return deviceScanner != null;
    }

	@SuppressWarnings("unchecked")
	<S extends DeviceScanner> S newDeviceScanner(String settings) throws ArgumentSyntaxException {
		S scanner;
    	try {
			scanner = (S) deviceScanner.getDeclaredConstructor().newInstance();
			scanner.doConfigure(settings);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to instance {0}: {1}", 
            		deviceScanner.getSimpleName(), e.getMessage()));
		}
    	return scanner;
	}

    public final <S extends DeviceScanner> DriverContext setDeviceScanner(Class<S> scanner) {
    	info.setDeviceScanSettings(Options.parseSettings(scanner));
    	this.deviceScanner = scanner;
        return this;
    }

	@SuppressWarnings("unchecked")
	<C extends ChannelConfigs> C newChannelConfigs(DeviceContext context, ChannelContainer container) throws ArgumentSyntaxException {
		C channel;
		try {
			channel = (C) this.channel.getDeclaredConstructor().newInstance();
			channel.doConfigure(container);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to instance {0}: {1}", 
            		deviceScanner.getSimpleName(), e.getMessage()));
		}
		return channel;
	}

    public final DriverContext setChannel(Class<? extends ChannelContext> channel) {
    	info.setChannelAddress(Options.parseAddress(channel));
    	info.setChannelSettings(Options.parseSettings(channel));
    	this.channel = channel;
        return this;
    }

    public final DriverContext setChannelAddress(Class<? extends Configurable> configs) {
    	info.setChannelAddress(Options.parseAddress(configs));
        return this;
    }

    public final DriverContext setChannelSettings(Class<? extends Configurable> configs) {
    	info.setChannelSettings(Options.parseSettings(configs));
        return this;
    }

    boolean hasChannelScanner() {
    	return channelScanner != null;
    }

	@SuppressWarnings("unchecked")
	<S extends ChannelScanner> S newChannelScanner(DeviceContext context, String settings) throws ArgumentSyntaxException, ConnectionException {
		S scanner;
		try {
			scanner = (S) channelScanner.getDeclaredConstructor().newInstance();
			scanner.doConfigure(settings);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to instance {0}: {1}", 
            		channelScanner.getSimpleName(), e.getMessage()));
		}
		return scanner;
	}

    public final <S extends ChannelScanner> DriverContext setChannelScanner(Class<S> scanner) {
    	info.setChannelScanSettings(Options.parseSettings(scanner));
    	this.channelScanner = scanner;
        return this;
    }

}
