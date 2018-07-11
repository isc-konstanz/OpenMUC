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
package org.openmuc.framework.driver.rpi.gpio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.driver.rpi.gpio.GpioConnection.GpioConnectionCallbacks;
import org.openmuc.framework.driver.rpi.gpio.count.EdgeCounter;
import org.openmuc.framework.driver.rpi.gpio.settings.DeviceAddress;
import org.openmuc.framework.driver.rpi.gpio.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RCMPin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.GpioUtil;

@Component
public class GpioDriver implements DriverService, GpioConnectionCallbacks {
    private final static Logger logger = LoggerFactory.getLogger(GpioDriver.class);

    private final DriverInfo info = DriverInfoFactory.getPreferences(GpioDriver.class);

    private final List<GpioPin> pins;

    private GpioController gpio;

//    private volatile boolean isDeviceScanInterrupted = false;

    public GpioDriver() {
        pins = Collections.synchronizedList(new ArrayList<GpioPin>());
        
        // Check if privileged access is required on the running system and enable non-
        // privileged GPIO access if not.
        if (!GpioUtil.isPrivilegedAccessRequired()) {
            GpioUtil.enableNonPrivilegedAccess();
        }
        else {
            logger.warn("Privileged access is required on this system to access GPIO pins");
        }
        gpio = GpioFactory.getInstance();
    }

    @Override
    public DriverInfo getInfo() {
        return info;
    }

    @Override
    public Connection connect(String addressStr, String settingsStr) throws ArgumentSyntaxException, ConnectionException {
        
        logger.trace("Connect Raspberry Pi device address \"{}\": {}", addressStr, settingsStr);
        DeviceAddress address = info.parse(addressStr, DeviceAddress.class);
        DeviceSettings settings = info.parse(settingsStr, DeviceSettings.class);
        
        try {
            synchronized(pins) {
                if (pins.size() == 0 && gpio.isShutdown()) {
                    gpio = GpioFactory.getInstance();
                }
            }
            GpioConnection connection;
            
            Pin pin;
            if (address.useBroadcomScheme()) {
                pin = RCMPin.getPinByAddress(address.getPin());
            }
            else {
                pin = RaspiPin.getPinByAddress(address.getPin());
            }
            if (pin == null) {
                throw new ConnectionException("Unable to configure GPIO pin: " + address.getPin());
            }
            
            GpioPinDigital gpioPin = null;
            switch(settings.getType()) {
            case INPUT:
                gpioPin = gpio.provisionDigitalInputPin(pin, settings.getPullResistance());
                
                if (settings.isCounter()) {
                    connection = new EdgeCounter(this, gpioPin, settings.getPullResistance(), settings.getBounceTime());
                }
                else {
                    connection = new InputPin(this, gpioPin);
                }
                break;
            case OUTPUT:
                gpioPin = gpio.provisionDigitalOutputPin(pin, settings.getDefaultState());

                connection = new OutputPin(this, gpioPin);
                break;
            default:
                throw new ArgumentSyntaxException("GPIO pins not supported for type: " + settings.getPreferenceType());
            }
            gpioPin.setShutdownOptions(true, settings.getShutdownState(), settings.getShutdownPullResistance());
            
            pins.add(gpioPin);
            
            return connection;

        } catch (IllegalArgumentException e) {
            throw new ArgumentSyntaxException("Unable to configure GPIO pin: " + e.getMessage());
        }
    }

    @Override
    public void onDisconnect(GpioPin pin) {
        synchronized(pins) {
            
            // Unprovision pins, to enable getPin() to provision them again later
            // GpioPinExistsException would be thrown otherwise
            gpio.unprovisionPin(pin);
            pins.remove(pin);
            
            if (pins.size() == 0) {
                // Stop all GPIO activity/threads by shutting down the GPIO controller
                // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
                gpio.shutdown();
            }
        }
    }
}