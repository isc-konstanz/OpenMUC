/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.driver.rpi.w1.options;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;

public enum W1Type {

    TEMPERATURE_SENSOR("TemperatureSensor", TemperatureSensor.class);

    private final String name;
    private final Class<?> type;

    private W1Type(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public static W1Type newType(W1Device device) throws IllegalArgumentException {
        
        if (device instanceof TemperatureSensor) {
            return W1Type.TEMPERATURE_SENSOR;
        }
        else {
            throw new IllegalArgumentException("Unknown 1-Wire device type: " + device.getClass().getSimpleName());
        }
    }

    public static W1Type newType(String type) throws IllegalArgumentException {
        
        switch(type.trim()) {
        case "TemperatureSensor":
            return W1Type.TEMPERATURE_SENSOR;
        default:
            throw new IllegalArgumentException("Unknown 1-Wire device type: " + type);
        }
    }
}