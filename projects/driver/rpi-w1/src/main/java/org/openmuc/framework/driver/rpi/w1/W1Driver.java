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
package org.openmuc.framework.driver.rpi.w1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.DriverActivator;
import org.openmuc.framework.driver.DriverDeviceFactory;
import org.openmuc.framework.driver.DriverDeviceScannerFactory;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Driver;
import org.openmuc.framework.driver.rpi.w1.device.TemperatureDevice;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.w1.W1Master;

@Component(service = DriverService.class)
@Driver(id = W1Driver.ID,
        name = W1Driver.NAME, description = W1Driver.DESCRIPTION)
public class W1Driver extends DriverActivator implements DriverDeviceFactory, DriverDeviceScannerFactory {

    private static final Logger logger = LoggerFactory.getLogger(W1Driver.class);

    public static final String ID = "rpi-w1";
    public static final String NAME = "1-Wire (Raspberry Pi)";
    public static final String DESCRIPTION = 
            "The 1-Wire Driver enables the access to 1-Wire devices, connected to the Raspberry Pi platform.";

    final List<String> connected = Collections.synchronizedList(new ArrayList<String>());

    W1Master master;

    @Activate
    public void activate() {
        // Pass the ClassLoader, as the W1Master may otherwise not be able to load and 
        // recognize available devices according to their DeviceType
        master = new W1Master(W1Driver.class.getClassLoader());
    }

    @Override
    public W1Scanner newScanner(Settings settings) {
        return new W1Scanner(master.getDevices(), connected);
    }

    @Override
    public W1Device newDevice(Address address, Settings settings) throws ArgumentSyntaxException, ConnectionException {
        logger.trace("Connect 1-Wire device: {}", address);
        try {
            List<com.pi4j.io.w1.W1Device> devices = master.getDevices();
            for (com.pi4j.io.w1.W1Device device : devices) {
                if (device.getId().trim().replace("\n", "").replace("\r", "").equals(address)) {
                    switch (W1Type.valueOf(device)) {
                    case SENSOR_TEMPERATURE:
                        return new TemperatureDevice(device);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to configure 1-Wire device \"{0}\" : {1}",
                    address, e.getMessage()));
        }
        throw new ConnectionException("Unable to find specified 1-Wire device: " + address);
    }

    @Connect
    protected void connect(W1Device device) {
        connected.add(device.getId());
    }

    @Disconnect
    protected void disconnect(W1Device device) {
        connected.remove(device.getId());
    }

}
