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

import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.driver.DriverChannel;

public class GpioChannel extends DriverChannel {

    @Option(type = SETTING,
            name = "Inverted state logic",
            description = "Use an inverted pin state logic.",
            valueDefault = "false",
            mandatory = false
    )
    private boolean inverted = false;

    @Option(type = SETTING,
            name = "imp./unit",
            description = "he amount of impulses corresponding to one unit.<br><br>" +
                          "<i>This setting is only applicable for edge counting input pins</i>",
            valueDefault = "1",
            mandatory = false
    )
    private int impulses = 1;

    @Option(type = SETTING,
            id = "countInterval",
            name = "Count pulses per interval",
            description = "Count the amount of pulses during a sampling interval, instead of a global counter.<br><br>" +
                          "<i>This setting is only applicable for edge counting input pins</i>",
            valueDefault = "false",
            mandatory = false
    )
    private boolean intervalCount = false;

    @Option(type = SETTING,
            name = "Time derivative",
            description = "Calculate the time derivative of counted pulses by the time unit.<br><br>" +
                          "<i>This setting is only applicable for edge counting input pins</i>",
            valueSelection = "1:Millisecond,1000:Second,60000:Minute,3600000:Hour",
            mandatory = false
    )
    private Integer derivativeTime = null;

    public boolean isInverted() {
        return inverted;
    }

    public double getImpulses() {
        return (double) impulses;
    }

    public boolean isIntervalCount() {
        return intervalCount;
    }

    public boolean isDerivative() {
        if (derivativeTime != null) {
            return true;
        }
        return false;
    }

    public double getDerivativeTime() {
        return derivativeTime.doubleValue();
    }

}
