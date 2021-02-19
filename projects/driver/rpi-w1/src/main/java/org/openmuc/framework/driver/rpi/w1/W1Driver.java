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
package org.openmuc.framework.driver.rpi.w1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.DeviceConnection;
import org.openmuc.framework.driver.DeviceFactory.Factory;
import org.openmuc.framework.driver.Driver;
import org.openmuc.framework.driver.DriverContext;
import org.openmuc.framework.driver.rpi.w1.device.TemperatureDevice;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.w1.W1Master;

@Component(service = DriverService.class)
@Factory(device = W1Device.class, scanner = W1Scanner.class)
public class W1Driver extends Driver {
    private static final Logger logger = LoggerFactory.getLogger(W1Driver.class);

    private static final String ID = "rpi-w1";
    private static final String NAME = "1-Wire (Raspberry Pi)";
    private static final String DESCRIPTION = 
            "The 1-Wire Driver enables the access to 1-Wire devices, connected to the Raspberry Pi platform.";

    private final List<String> connected = Collections.synchronizedList(new ArrayList<String>());

    private W1Master master;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void onCreate(DriverContext context) {
        context.setName(NAME)
                .setDescription(DESCRIPTION);
    }

    @Override
    public void onActivate() {
        // Pass the ClassLoader, as the W1Master may otherwise not be able to load and 
        // recognize available devices according to their DeviceType
        master = new W1Master(W1Driver.class.getClassLoader());
    }

    @Override
    protected W1Scanner newScanner() {
        return new W1Scanner(master.getDevices(), connected);
    }

    @Override
    public DeviceConnection newDevice(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
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

    @Override
    public void onConnect(DeviceConnection connection) {
        String id = ((W1Device) connection).getId();
        connected.add(id);
    }

    @Override
    public void onDisconnect(DeviceConnection connection) {
        String id = ((W1Device) connection).getId();
        connected.remove(id);
    }

}
