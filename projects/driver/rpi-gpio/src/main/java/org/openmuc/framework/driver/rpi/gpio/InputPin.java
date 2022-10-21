/*
 * Copyright 2011-2022 Fraunhofer ISE
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

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.spi.ConnectionException;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;

public class InputPin extends GpioPin {

    public InputPin(GpioPinDigital pin) {
        super(pin);
    }

    @Read
    public void read(List<GpioChannel> channels, String samplingGroup)
            throws ConnectionException {
        
        long samplingTime = System.currentTimeMillis();
        
        for (GpioChannel channel : channels) {
            PinState state = pin.getState();
            Value value;
            if (!channel.isInverted()) {
                value = new BooleanValue(state.isHigh());
            }
            else {
                value = new BooleanValue(state.isLow());
            }
            channel.setRecord(new Record(value, samplingTime, Flag.VALID));
        }
    }

}
