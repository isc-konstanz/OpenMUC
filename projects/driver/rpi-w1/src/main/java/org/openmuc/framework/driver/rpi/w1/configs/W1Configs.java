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
package org.openmuc.framework.driver.rpi.w1.configs;

import org.openmuc.framework.driver.DeviceConfigs;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;

public class W1Configs extends DeviceConfigs<W1Channel> {

    public static final String TYPE = "type";

    @Address(id = "id",
             name = "Identifier",
             description = "The device ID, retrievable through scanning."
    )
    private String id;

    @Setting(id = TYPE,
             name = "Type",
             description = "The type of the 1-Wire device, e.g. a temperature or humidity sensor.",
             valueSelection = "SENSOR_TEMPERATURE:Temperature sensor"
    )
    private W1Type type;

    @Setting(id = "maximum",
             name = "Maximum sensor value",
             description = "The maximum value the sensor can read.<br>" +
                           "Used e.g. in error detection of temperature sensors.",
             mandatory = false
    )
    private Double maximum = Double.NaN;

    public String getId() {
        return id.trim().replace("\n", "").replace("\r", "");
    }

    public W1Type getType() {
        return type;
    }

    public Double getMaximum() {
        return maximum;
    }

}
