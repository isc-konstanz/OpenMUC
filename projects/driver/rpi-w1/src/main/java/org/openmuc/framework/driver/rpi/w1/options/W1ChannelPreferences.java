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

import org.openmuc.framework.config.options.Preferences;

import com.pi4j.temperature.TemperatureScale;

public class W1ChannelPreferences {

    public static final String UNIT_KEY = "unit";
    public static final TemperatureScale UNIT_DEFAULT = TemperatureScale.CELSIUS;

    private final String settingsStr;
    private final Preferences settings;

    public W1ChannelPreferences(String settingsStr, Preferences settings) {
        this.settingsStr = settingsStr;
        this.settings = settings;
    }

    public boolean equals(String settingsStr) {
        return this.settingsStr.equals(settingsStr);
    }

    public TemperatureScale getUnit() {
        if (settings.contains(UNIT_KEY)) {
            return TemperatureScale.valueOf(settings.getString(UNIT_KEY).toUpperCase());
        }
        return UNIT_DEFAULT;
    }

}
