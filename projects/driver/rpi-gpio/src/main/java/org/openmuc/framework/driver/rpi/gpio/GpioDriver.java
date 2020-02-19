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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.driver.Driver;
import org.openmuc.framework.driver.DriverContext;
import org.openmuc.framework.driver.rpi.gpio.configs.GpioConfigs;
import org.openmuc.framework.driver.rpi.gpio.count.EdgeCounter;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.GpioUtil;

@Component
public class GpioDriver extends Driver<GpioConfigs> implements DriverService {
    private static final Logger logger = LoggerFactory.getLogger(GpioDriver.class);

    private static final String ID = "rpi-gpio";
    private static final String NAME = "GPIO (Raspberry Pi)";
    private static final String DESCRIPTION = 
    		"This driver enables the access to the variety of pins of the Raspberry Pi platform. " +
            "Devices represent the General-Purpose Inputs/Outputs (GPIOs) of the Raspberry Pi, " +
            "generic pins to be used either as input or output.";

    private GpioController gpio;

	@Override
    public String getId() {
    	return ID;
    }

	@Override
	protected void onCreate(DriverContext context) {
		context.setName(NAME)
				.setDescription(DESCRIPTION)
				.setDeviceScanner(GpioScanner.class);
	}

	@Override
    public void onActivate() {
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
    public void onDeactivate() {
        // Stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
    }

    @Override
	protected GpioPin onCreateConnection(GpioConfigs configs) throws ArgumentSyntaxException, ConnectionException {
        logger.trace("Connect Raspberry Pi {} pin {}", configs.getPinMode().getName(), configs.getPin());
        try {
            GpioPin connection;
            
            Pin p = RaspiPin.getPinByAddress(configs.getPin());
            if (p == null) {
                throw new ConnectionException("Unable to configure GPIO pin: " + configs.getPin());
            }
            
            GpioPinDigital pin = null;
            switch(configs.getPinMode()) {
                case DIGITAL_INPUT:
                    pin = gpio.provisionDigitalInputPin(p, configs.getPullResistance());

                    if (configs.isCounter()) {
                        connection = new EdgeCounter(pin, configs.getPullResistance(), configs.getBounceTime());
                    }
                    else {
                        connection = new InputPin(pin);
                    }
                    break;
                case DIGITAL_OUTPUT:
                    pin = gpio.provisionDigitalOutputPin(p, configs.getDefaultState());
                    
                    connection = new OutputPin(pin);
                    break;
                default:
                    throw new ArgumentSyntaxException("GPIO pins not supported for mode: " + configs.getPinMode());
            }
            pin.setShutdownOptions(true, configs.getShutdownState(), configs.getShutdownPullResistance());
            
            return connection;
            
        } catch (RuntimeException e) {
            throw new ArgumentSyntaxException("Unable to configure GPIO pin: " + e);
        }
    }

    @Override
	protected void onDisconnect(Device<?> device) {
    	gpio.unprovisionPin(((GpioPin) device).getPin());
	}

}
