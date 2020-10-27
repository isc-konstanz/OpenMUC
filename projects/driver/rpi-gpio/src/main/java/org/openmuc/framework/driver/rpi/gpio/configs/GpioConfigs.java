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
package org.openmuc.framework.driver.rpi.gpio.configs;

import org.openmuc.framework.driver.DeviceConfigs;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.AddressSyntax;
import org.openmuc.framework.options.Setting;

import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;

@AddressSyntax(separator = ",",
               assignmentOperator = ":",
               keyValuePairs = true)
public class GpioConfigs extends DeviceConfigs<GpioChannel> {

    public static final String PIN = "pin";
    public static final String MODE = "mode";

    @Address(id = PIN,
             name = "Pin",
             description = "The pin number, according to the <a href='http://pi4j.com/pin-numbering-scheme.html'>WiringPi Pin Numbering Scheme</a>."
    )
    private int pin;

    @Setting(id = MODE,
             name = "I/O mode",
             valueSelection = "DIGITAL_INPUT:Input,DIGITAL_OUTPUT:Output"
    )
    private PinMode mode;

    @Setting(id = "defaultState",
             name = "Default state",
             description = "The default state of the configured pin, immediately set at startup of the driver.",
             valueDefault = "false",
             mandatory = false
    )
    private boolean defaultState = false;

    @Setting(id = "shutdownState",
             name = "Shutdown state",
             description = "The default state of the configured pin, set at shutdown of the driver.",
             valueDefault = "false",
             mandatory = false
    )
    private boolean shutdownState = false;

    @Setting(id = "pullResistance",
             name = "Pull resistance",
             description = "The pull resistance of the configured pin, immediately set at startup of the driver.",
             valueSelection = "PULL_UP:Pull-up,PULL_DOWN:Pull-down,OFF:Off",
             valueDefault = "PULL_DOWN",
             mandatory = false
    )
    private PinPullResistance pullResistance = PinPullResistance.PULL_DOWN;

    @Setting(id = "shutdownPullResistance",
             name = "Shutdown pull resistance",
             description = "The default pull resistance of the configured pin, set at shutdown of the driver.",
             valueSelection = "PULL_UP:Pull-up,PULL_DOWN:Pull-down,OFF:Off",
             valueDefault = "PULL_DOWN",
             mandatory = false
    )
    private PinPullResistance shutdownPullResistance = PinPullResistance.PULL_DOWN;

    @Setting(id = "counter",
             name = "Edge counter",
             description = "Enable the counting of detected edges.<br><br>" + 
             		       "<i>This setting is only applicable for input pins</i>",
             valueDefault = "false",
             mandatory = false
    )
    private boolean counter = false;

    @Setting(id = "bounceTime",
             name = "Bounce avoidance time",
             description = "The amount of miliseconds waited, until a new edge detection will be accepted as a received impulse, to avoid bouncing.<br><br>" +
                           "<i>This setting is only applicable for edge counting input pins</i>",
             valueDefault = "60",
             mandatory = false
    )
    private int bounceTime = 60;

    public int getPin() {
        return pin;
    }

    public PinMode getPinMode() {
        return mode;
    }

    public PinState getDefaultState() {
    	if (defaultState) {
            return PinState.HIGH;
    	}
        return PinState.LOW;
    }

    public PinState getShutdownState() {
    	if (shutdownState) {
            return PinState.HIGH;
    	}
        return PinState.LOW;
    }

    public PinPullResistance getPullResistance() {
        return pullResistance;
    }

    public PinPullResistance getShutdownPullResistance() {
        return shutdownPullResistance;
    }

    public boolean isCounter() {
    	return counter;
    }

    public int getBounceTime() {
    	return bounceTime;
    }

}
