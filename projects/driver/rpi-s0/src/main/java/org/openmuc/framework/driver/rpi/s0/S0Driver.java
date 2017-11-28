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
package org.openmuc.framework.driver.rpi.s0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.rpi.s0.S0Connection.S0ConnectionCallbacks;
import org.openmuc.framework.driver.rpi.s0.options.S0DevicePreferences;
import org.openmuc.framework.driver.rpi.s0.options.S0DriverInfo;
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
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RCMPin;
import com.pi4j.io.gpio.RaspiPin;


@Component
public class S0Driver implements DriverService, S0ConnectionCallbacks {
    private final static Logger logger = LoggerFactory.getLogger(S0Driver.class);
    private final S0DriverInfo info = S0DriverInfo.getInfo();

    private final List<GpioPin> pins;

    private GpioController gpio;

//    private volatile boolean isDeviceScanInterrupted = false;

    public S0Driver() {
        pins = Collections.synchronizedList(new ArrayList<GpioPin>());
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
        S0DevicePreferences prefs = info.getDevicePreferences(addressStr, settingsStr);
        
        try {
            Pin pin;
            if (prefs.useBroadcomScheme()) {
                pin = RCMPin.getPinByAddress(prefs.getPin());
            }
            else {
                pin = RaspiPin.getPinByAddress(prefs.getPin());
            }
            if (pin == null) {
                throw new ConnectionException("Unable to configure GPIO pin: " + prefs.getPin());
            }
            
            PinPullResistance pullResistance = prefs.getPullResistance();
            GpioPinDigitalInput gpioPin = gpio.provisionDigitalInputPin(pin, pullResistance);
            gpioPin.setShutdownOptions(true, prefs.getShutdownState(), pullResistance);
            
            return new S0Connection(this, gpioPin, pullResistance, prefs.getBounceTime());

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
