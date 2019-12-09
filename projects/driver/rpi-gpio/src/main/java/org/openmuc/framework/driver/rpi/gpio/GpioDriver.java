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
import org.openmuc.framework.driver.rpi.gpio.configs.GpioConfigs;
import org.openmuc.framework.driver.rpi.gpio.count.EdgeCounter;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.Driver;
import org.openmuc.framework.driver.spi.DriverContext;
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

@Component(service = DriverService.class)
public class GpioDriver extends Driver<GpioConfigs> {
    private static final Logger logger = LoggerFactory.getLogger(GpioDriver.class);

    private static final String ID = "rpi-gpio";
    private static final String NAME = "GPIO (Raspberry Pi)";
    private static final String DESCRIPTION = 
    		"This driver enables the access to the variety of pins of the Raspberry Pi platform. " +
            "Devices represent the General-Purpose Inputs/Outputs (GPIOs) of the Raspberry Pi, " +
            "generic pins to be used either as input or output.";

    private List<GpioPinDigital> pins;

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
        pins = Collections.synchronizedList(new ArrayList<GpioPinDigital>());
        
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
	protected GpioPin newConnection(GpioConfigs configs) throws ArgumentSyntaxException, ConnectionException {
        logger.trace("Connect Raspberry Pi {} pin {}", configs.getPinMode().getName(), configs.getPin());
        try {
            GpioPin connection;
            
            Pin pin = RaspiPin.getPinByAddress(configs.getPin());
            if (pin == null) {
                throw new ConnectionException("Unable to configure GPIO pin: " + configs.getPin());
            }
            
            GpioPinDigital gpio = null;
            switch(configs.getPinMode()) {
                case DIGITAL_INPUT:
                    gpio = this.gpio.provisionDigitalInputPin(pin, configs.getPullResistance());
                    
                    if (configs.isCounter()) {
                        connection = new EdgeCounter(gpio, configs.getPullResistance(), configs.getBounceTime());
                    }
                    else {
                        connection = new InputPin(gpio);
                    }
                    break;
                case DIGITAL_OUTPUT:
                    gpio = this.gpio.provisionDigitalOutputPin(pin, configs.getDefaultState());
                    
                    connection = new OutputPin(gpio);
                    break;
                default:
                    throw new ArgumentSyntaxException("GPIO pins not supported for mode: " + configs.getPinMode());
            }
            gpio.setShutdownOptions(true, configs.getShutdownState(), configs.getShutdownPullResistance());
            
            pins.add(gpio);
            return connection;

        } catch (RuntimeException e) {
            throw new ArgumentSyntaxException("Unable to configure GPIO pin: " + e.getMessage());
        }
    }

    @Override
    public void onDisconnected(GpioConfigs configs) {
        synchronized(pins) {
            // Unprovision pins, to enable getPin() to provision them again later
            // GpioPinExistsException would be thrown otherwise
        	for (GpioPinDigital pin : pins) {
        		if (configs.getPin() == pin.getPin().getAddress()) {
                    gpio.unprovisionPin(pin);
                    pins.remove(pin);
        		}
        	}
        }
    }
}
