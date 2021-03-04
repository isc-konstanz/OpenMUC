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
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.DeviceOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.driver.annotation.Factory;
import org.openmuc.framework.driver.spi.ConnectionException;

public abstract class DeviceContext implements DeviceOptions, DeviceFactory, DeviceScannerFactory, Device.Callbacks {

    private Class<? extends DeviceScanner> scannerClass;

    private Class<? extends Device<?>> deviceClass;

    // TODO: This could be replaced with a dynamic approach, dependent on device options
    ChannelContext channel;

    DriverContext context;

    @SuppressWarnings("unchecked")
	DeviceContext() {
        Factory factory = getClass().getAnnotation(Factory.class);
        if (factory != null) {
            scannerClass = (Class<? extends DeviceScanner>) factory.scanner();
        }
        this.deviceClass =  getDeviceClass();
        this.channel = new ChannelContext(deviceClass);
    }

    @SuppressWarnings("unchecked")
	private Class<? extends Device<?>> getDeviceClass() {
    	Class<?> driverClass = getClass();
        while (driverClass.getSuperclass() != null) {
            if (driverClass.getSuperclass().equals(Driver.class)) {
                break;
            }
            driverClass = driverClass.getSuperclass();
        }
        // This operation is safe. Because driverClass is a direct sub-class, getGenericSuperclass() will
        // always return the Type of this class. Because this class is parameterized, the cast is safe
        ParameterizedType superClass = (ParameterizedType) driverClass.getGenericSuperclass();
        return (Class<? extends Device<?>>) superClass.getActualTypeArguments()[0];
    }

    @Override
    public final ChannelContext getChannel() {
        return channel;
    }

    public final DriverContext getContext() {
        return context;
    }

    @Override
    public final Options getAddressOptions() {
        Options deviceAddress = null;
        if (deviceClass != null) {
            deviceAddress = Options.parseAddress(deviceClass);
        }
        return deviceAddress;
    }

    @Override
    public final Options getSettingsOptions() {
        Options deviceSettings = null;
        if (deviceClass != null) {
            deviceSettings = Options.parseSettings(deviceClass);
        }
        return deviceSettings;
    }

    @Override
    public final Options getScanSettingsOptions() {
        Options scanSettings = null;
        if (scannerClass != null && !scannerClass.equals(DeviceScanner.class)) {
            scanSettings = Options.parseSettings(scannerClass);
        }
        return scanSettings;
    }

    final void bindDevice(Class<? extends Device<?>> deviceClass) {
        this.deviceClass = deviceClass;
    }

    public Device<?> newDevice(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
        return this.newDevice(Configurations.parseAddress(address, deviceClass),
                              Configurations.parseSettings(settings, deviceClass));
    }

    @Override
    public Device<?> newDevice(Address address, Settings settings) throws ArgumentSyntaxException, ConnectionException {
        return this.newDevice();
    }

    protected Device<?> newDevice() throws ConnectionException {
        return DriverContext.newInstance(deviceClass);
    }

    final void bindScanner(Class<? extends DeviceScanner> scannerClass) {
        this.scannerClass = scannerClass;
    }

    public DeviceScanner newScanner(String settings) throws ArgumentSyntaxException {
        return this.newScanner(Configurations.parseSettings(settings, scannerClass));
    }

    @Override
    public DeviceScanner newScanner(Settings settings) throws ArgumentSyntaxException {
        return this.newScanner();
    }

    protected DeviceScanner newScanner() {
        return DriverContext.newInstance(scannerClass);
    }

}
