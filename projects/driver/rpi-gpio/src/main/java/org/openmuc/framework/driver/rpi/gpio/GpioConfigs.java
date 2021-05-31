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
package org.openmuc.framework.driver.rpi.gpio;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.driver.DriverDevice;
import org.openmuc.framework.driver.annotation.Device;

import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;

@Device(channel = GpioChannel.class)
public class GpioConfigs extends DriverDevice {

    public static final String PIN = "pin";
    public static final String MODE = "mode";

    @Option(id = PIN,
            type = ADDRESS,
             name = "Pin",
             description = "The pin number, according to the <a href='http://pi4j.com/pin-numbering-scheme.html'>WiringPi Pin Numbering Scheme</a>."
    )
    private int pin;

    @Option(id = MODE,
            type = SETTING,
             name = "I/O mode",
             valueSelection = "DIGITAL_INPUT:Input,DIGITAL_OUTPUT:Output"
    )
    private PinMode mode;

    @Option(type = SETTING,
            name = "Default state",
             description = "The default state of the configured pin, immediately set at startup of the driver.",
             valueDefault = "false",
             mandatory = false
    )
    private boolean defaultState = false;

    @Option(type = SETTING,
            name = "Shutdown state",
             description = "The default state of the configured pin, set at shutdown of the driver.",
             valueDefault = "false",
             mandatory = false
    )
    private boolean shutdownState = false;

    @Option(type = SETTING,
            name = "Pull resistance",
             description = "The pull resistance of the configured pin, immediately set at startup of the driver.",
             valueSelection = "PULL_UP:Pull-up,PULL_DOWN:Pull-down,OFF:Off",
             valueDefault = "PULL_DOWN",
             mandatory = false
    )
    private PinPullResistance pullResistance = PinPullResistance.PULL_DOWN;

    @Option(type = SETTING,
            name = "Shutdown pull resistance",
             description = "The default pull resistance of the configured pin, set at shutdown of the driver.",
             valueSelection = "PULL_UP:Pull-up,PULL_DOWN:Pull-down,OFF:Off",
             valueDefault = "PULL_DOWN",
             mandatory = false
    )
    private PinPullResistance shutdownPullResistance = PinPullResistance.PULL_DOWN;

    @Option(type = SETTING,
            name = "Edge counter",
             description = "Enable the counting of detected edges.<br><br>" + 
                            "<i>This setting is only applicable for input pins</i>",
             valueDefault = "false",
             mandatory = false
    )
    private boolean counter = false;

    @Option(type = SETTING,
            name = "Bounce avoidance time",
             description = "The amount of miliseconds waited, until a new edge detection will be accepted as a received impulse, to avoid bouncing.<br><br>" +
                           "<i>This setting is only applicable for edge counting input pins</i>",
             valueDefault = "60",
             mandatory = false
    )
    private int bounceTime = 60;

    protected GpioConfigs() {
    }

    protected GpioConfigs(Address address, Settings settings) throws ArgumentSyntaxException {
        this.configure(address);
        this.configure(settings);
    }

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
