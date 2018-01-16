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
package org.openmuc.framework.driver.rpi.s0.options;

import org.openmuc.framework.config.options.Preferences;

public class S0ChannelPreferences {

    private static final String IMPULSES_KEY = "impulses";

    private static final String DERIVATIVE_KEY = "derivative";
    private static final boolean DERIVATIVE_DEFAULT = false;

    private static final String COUNT_INTERVAL_KEY = "countInterval";
    private static final boolean COUNT_INTERVAL_DEFAULT = false;

    private final String settingsStr;
    private final Preferences settings;

    public S0ChannelPreferences(String settingsStr, Preferences settings) {
        this.settingsStr = settingsStr;
        this.settings = settings;
    }

    public boolean equals(String settingsStr) {
        return this.settingsStr.equals(settingsStr);
    }

    public Integer getImpulses() {
        if (settings.contains(IMPULSES_KEY)) {
            return settings.getInteger(IMPULSES_KEY);
        }
        return null;
    }

    public boolean isDerivative() {
        if (settings.contains(DERIVATIVE_KEY)) {
            return settings.getBoolean(DERIVATIVE_KEY);
        }
        return DERIVATIVE_DEFAULT;
    }

    public boolean isCountInterval() {
        if (settings.contains(COUNT_INTERVAL_KEY)) {
            return settings.getBoolean(COUNT_INTERVAL_KEY);
        }
        return COUNT_INTERVAL_DEFAULT;
    }

}
