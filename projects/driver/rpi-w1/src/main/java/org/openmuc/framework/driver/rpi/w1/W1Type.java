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
package org.openmuc.framework.driver.rpi.w1;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;

public enum W1Type {

    SENSOR_TEMPERATURE("TemperatureSensor");

    private final String name;

    private W1Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static W1Type valueOf(W1Device device) throws IllegalArgumentException {
        
        if (device instanceof TemperatureSensor) {
            return W1Type.SENSOR_TEMPERATURE;
        }
        else {
            throw new IllegalArgumentException("Unknown 1-Wire device type: " + device.getClass().getSimpleName());
        }
    }
}
