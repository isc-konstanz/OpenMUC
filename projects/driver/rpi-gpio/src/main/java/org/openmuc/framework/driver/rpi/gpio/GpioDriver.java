/*
 * Copyright 2011-16 Fraunhofer ISE
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
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.rpi.gpio.GpioConnection.GpioConnectionCallbacks;
import org.openmuc.framework.driver.rpi.gpio.settings.DeviceAddress;
import org.openmuc.framework.driver.rpi.gpio.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
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
    public void scanForDevices(String settingsStr, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {

        logger.info("Scan for Raspberry Pi GPIO pins.");
//        Parameters settings = DEVICE_OPTIONS.parseScanSettings(settingsStr);

        // TODO: retrieve information about the platform and change results accordingly

//        List<RpiConnetorInfo> infos = new ArrayList<RpiConnetorInfo>();
//        infos.add(W1Connector.getInfo());
//        infos.add(GpioConnector.getInfo());
//
//        double counter = 0;
//        for (RpiConnetorInfo info : infos) {
//            if (isDeviceScanInterrupted) {
//                break;
//            }
//            
//            String address = "";
//            String settings = GpioDeviceOptions.TYPE_KEY + "=" + info.getType().getName();
//            
//            listener.deviceFound(new DeviceScanInfo("rpi_device_"+info.getId(), 
//                    address, settings, info.getDescription()));
//            
//            listener.scanProgressUpdate((int) (counter / infos.size() * 100.0));
//            
//            counter++;
//        }
        
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
//        isDeviceScanInterrupted = true;
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
                break;
            case OUTPUT:
                gpioPin = gpio.provisionDigitalOutputPin(pin, settings.getDefaultState());
                break;
            default:
                throw new UnsupportedOperationException("GPIO pins not supported for type: " + settings.getPreferenceType());
            }
            gpioPin.setShutdownOptions(true, settings.getShutdownState(), settings.getShutdownPullResistance());
            
            pins.add(gpioPin);
            
            return new GpioConnection(this, gpioPin);

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
