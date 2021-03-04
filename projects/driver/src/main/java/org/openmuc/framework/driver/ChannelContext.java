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

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.ChannelOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.driver.annotation.Factory;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;

public class ChannelContext extends Configurable implements ChannelOptions, ChannelFactory, ChannelScannerFactory {

    Class<? extends ChannelScanner> scannerClass;

    Class<? extends DeviceChannel> channelClass;

    DeviceContext context;

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

    final void bindChannel(Class<? extends DeviceChannel> channelClass) {
        this.channelClass = channelClass;
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

    final void bindScanner(Class<? extends ChannelScanner> scannerClass) {
        this.scannerClass = scannerClass;
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

}
