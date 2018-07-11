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
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.gpio.settings.ChannelSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;

public class InputPin extends GpioConnection {
	private final static Logger logger = LoggerFactory.getLogger(InputPin.class);

    public InputPin(GpioConnectionCallbacks callbacks, GpioPinDigital pin) {
		super(callbacks, pin);
	}

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        long samplingTime = System.currentTimeMillis();

        for (ChannelRecordContainer container : containers) {
            try {
                ChannelSettings settings = prefs.get(container.getChannelSettings(), ChannelSettings.class);

                PinState state = pin.getState();
                Value value;
                if (!settings.isInverted()) {
                    value = new BooleanValue(state.isHigh());
                }
                else {
                    value = new BooleanValue(state.isLow());
                }
                container.setRecord(new Record(value, samplingTime, Flag.VALID));

            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e);
                container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
            }
        }
        
        return null;
    }

}
