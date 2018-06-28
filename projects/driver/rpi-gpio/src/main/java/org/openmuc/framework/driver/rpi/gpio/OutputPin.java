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

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.gpio.settings.ChannelSettings;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class OutputPin extends InputPin {
	private final static Logger logger = LoggerFactory.getLogger(OutputPin.class);

    public OutputPin(GpioConnectionCallbacks callbacks, GpioPinDigital pin) {
		super(callbacks, pin);
	}

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        
        for (ChannelValueContainer container : containers) {
            Value value = container.getValue();
            try {
                ChannelSettings settings = prefs.get(container.getChannelSettings(), ChannelSettings.class);
                
                if (value != null) {
                    logger.debug("Write value to output pin {}: {}", pin.getName(), value);

                    PinState state;
                    if (!settings.isInverted()) {
                        state = PinState.getState(value.asBoolean());
                    }
                    else {
                        state = PinState.getState(!value.asBoolean());
                    }
                    ((GpioPinDigitalOutput) pin).setState(state);
                    
                    container.setFlag(Flag.VALID);
                }
                else {
                    logger.warn("No value received to write to GPIO pin {}", pin.getName());
                }
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e.getMessage());
            }
        }
        
        return null;
    }

}
