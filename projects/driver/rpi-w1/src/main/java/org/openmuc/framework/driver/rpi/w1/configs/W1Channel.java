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
package org.openmuc.framework.driver.rpi.w1.configs;

import org.openmuc.framework.driver.spi.Channel;
import org.openmuc.framework.options.Setting;

import com.pi4j.temperature.TemperatureScale;

public class W1Channel extends Channel {

    @Setting(id = "unit",
            name = "Unit",
            description = "The unit of the value, read from e.g. a 1-Wire temperature sensor.",
            valueSelection = 
            		"CELSIUS:Celsius," +
            		"KELVIN:Kelvin," +
            		"FARENHEIT:Farenheit," +
            		"RANKINE:Rankine",
            valueDefault = "CELSIUS"
    )
    private TemperatureScale unit = TemperatureScale.CELSIUS;

    public TemperatureScale getScale() {
        return unit;
    }

}
