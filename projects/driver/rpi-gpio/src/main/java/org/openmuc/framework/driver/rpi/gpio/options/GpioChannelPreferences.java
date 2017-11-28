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
package org.openmuc.framework.driver.rpi.gpio.options;

import org.openmuc.framework.config.options.Preferences;

public class GpioChannelPreferences {

    private static final String INVERTED_KEY = "inverted";
    private static final boolean INVERTED_DEFAULT = false;

    private final String settingsStr;
    private final Preferences settings;

    public GpioChannelPreferences(String settingsStr, Preferences settings) {
        this.settingsStr = settingsStr;
        this.settings = settings;
    }

    public boolean equals(String settingsStr) {
        return this.settingsStr.equals(settingsStr);
    }

    public boolean isInverted() {
        
        if (settings.contains(GpioChannelPreferences.INVERTED_KEY)) {
            return settings.getBoolean(GpioChannelPreferences.INVERTED_KEY);
        }
        return GpioChannelPreferences.INVERTED_DEFAULT;
    }

}
