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
package org.openmuc.framework.driver.rpi.w1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.driver.Driver;
import org.openmuc.framework.driver.DriverContext;
import org.openmuc.framework.driver.rpi.w1.configs.W1Configs;
import org.openmuc.framework.driver.rpi.w1.configs.W1Type;
import org.openmuc.framework.driver.rpi.w1.device.TemperatureDevice;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

@Component
public class W1Driver extends Driver<W1Configs> implements DriverService {
    private static final Logger logger = LoggerFactory.getLogger(W1Driver.class);

    private static final String ID = "rpi-w1";
    private static final String NAME = "1-Wire (Raspberry Pi)";
    private static final String DESCRIPTION = 
    		"The 1-Wire Driver enables the access to 1-Wire devices, connected to the Raspberry Pi platform.";

    private final List<String> connected = new ArrayList<String>();

    private W1Master master;

	@Override
    public String getId() {
    	return ID;
    }

	@Override
	protected void onCreate(DriverContext context) {
		context.setName(NAME)
				.setDescription(DESCRIPTION)
				.setDeviceScanner(W1Scanner.class);
	}

	@Override
	public void onActivate() {
	    // Pass the ClassLoader, as the W1Master may otherwise not be able to load and 
	    // recognize available devices according to their DeviceType
	    master = new W1Master(W1Driver.class.getClassLoader());
	}

	@Override
    protected W1Scanner newScanner(String settings) throws ArgumentSyntaxException {
		return new W1Scanner(master.getDevices(), connected, settings);
    }

    @Override
	protected W1Connection onCreateConnection(W1Configs configs) 
			throws ArgumentSyntaxException, ConnectionException {
        
    	String id = configs.getId();
        logger.trace("Connect 1-Wire {}: {}", configs.getType(), id);
        
        try {
            List<W1Device> devices = master.getDevices();
            for (W1Device device : devices) {
                if (device.getId().trim().replace("\n", "").replace("\r", "").equals(id)) {
                    W1Type type = W1Type.valueOf(device);
                    if (type != configs.getType()) {
                        throw new ConnectionException(MessageFormat.format("1-Wire device \"{0}\" not the expected type: {1}", 
                                id, configs.getType()));
                    }
                    switch (type) {
					case SENSOR_TEMPERATURE:
	                    return new TemperatureDevice(id, (TemperatureSensor) device);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to configure 1-Wire device \"{0}\" : {1}",
            		id, e.getMessage()));
        }
        throw new ConnectionException("Unable to find specified 1-Wire device: " + id);
    }

    @Override
    public void onConnect(Device<?> device) {
        connected.add(((W1Device) device).getId());
    }

    @Override
    public void onDisconnect(Device<?> device) {
        connected.remove(((W1Device) device).getId());
    }

}
