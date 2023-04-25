/*
 * Copyright 2011-2022 Fraunhofer ISE
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

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.DeviceOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Driver;
import org.openmuc.framework.driver.spi.ConnectionException;

public abstract class DriverDeviceContext extends Reflectable implements DeviceOptions {

    final Class<? extends DriverDeviceScanner> scannerClass;
    final Class<? extends DriverDevice> deviceClass;

    DriverContext driverContext;

    // TODO: This could be replaced with a dynamic approach, dependent on device options
    DriverChannelContext channelContext;

    DriverDeviceContext() {
        this.deviceClass  = getDeviceClass();
        this.scannerClass = getScannerClass();
        this.channelContext = new DriverChannelContext(deviceClass);
    }

    Driver getDriverAnnotation() {
        Driver driver = getClass().getAnnotation(Driver.class);
        if (driver == null) {
            throw new RuntimeException("Implementation invalid without annotation");
        }
        return driver;
    }

    public final DriverContext getContext() {
        return driverContext;
    }

    @Override
    public final DriverChannelContext getChannel() {
        return channelContext;
    }

    @SuppressWarnings("unchecked")
    Class<? extends DriverDeviceScanner> getScannerClass() {
        if (this instanceof DriverDeviceScannerFactory) {
            try {
                Method method = getClass().getMethod("newScanner", Settings.class);
                return (Class<? extends DriverDeviceScanner>) method.getReturnType();
                
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        Driver driver = getDriverAnnotation();
        return driver.scanner();
    }

    @SuppressWarnings("unchecked")
    Class<? extends DriverDevice> getDeviceClass() {
        if (this instanceof DriverDeviceFactory) {
            try {
                Method method = getClass().getMethod("newDevice", Address.class, Settings.class);
                return (Class<? extends DriverDevice>) method.getReturnType();
                
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        Driver driver = getDriverAnnotation();
        return driver.device();
    }

    @Override
    public final Options getAddressOptions() {
        Options deviceAddress = null;
        if (deviceClass != DriverDevice.class) {
            deviceAddress = Options.parse(ADDRESS, deviceClass);
        }
        return deviceAddress;
    }

    @Override
    public final Options getSettingsOptions() {
        Options deviceSettings = null;
        if (deviceClass != DriverDevice.class) {
            deviceSettings = Options.parse(SETTING, deviceClass);
        }
        return deviceSettings;
    }

    @Override
    public final Options getScanSettingsOptions() {
        Options scanSettings = null;
        if (scannerClass != DriverDeviceScanner.class) {
            scanSettings = Options.parse(SETTING, scannerClass);
        }
        return scanSettings;
    }

    DriverDeviceScanner newScanner(String settingsStr) throws RuntimeException, ArgumentSyntaxException {
        DriverDeviceScanner scanner;
        
        Settings settings = Configurations.parseSettings(settingsStr, scannerClass);
        
        if (this instanceof DriverDeviceScannerFactory) {
            scanner = ((DriverDeviceScannerFactory) this).newScanner(settings);
        }
        else if (scannerClass != DriverDeviceScanner.class) {
            scanner = newInstance(scannerClass);
        }
        else {
            return null;
        }
        scanner.invokeConfigure(this, settings);
        
        return scanner;
    }

    DriverDevice newDevice(String addressStr, String settingsStr) 
            throws ConnectionException, RuntimeException, ArgumentSyntaxException {
        
        Address address = Configurations.parseAddress(addressStr, deviceClass);
        Settings settings = Configurations.parseSettings(settingsStr, deviceClass);
        
        DriverDevice device;
        if (this instanceof DriverDeviceFactory) {
            device = ((DriverDeviceFactory) this).newDevice(address, settings);
        }
        else {
            device = newInstance(deviceClass);
        }
        device.invokeConfigure(this, address, settings);
        device.invokeConnect();
        this.invokeConnect(device);
        
        return device;
    }

    void invokeConnect(DriverDevice device) {
        invokeMethod(Connect.class, this, device);
    }

    void invokeDisconnect(DriverDevice device) {
        invokeMethod(Disconnect.class, this, device);
    }

}
