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

public class S0DeviceScanPreferences {

    private static final String BROADCOM_SCHEME_KEY = "broadcomScheme";
    private static final boolean BROADCOM_SCHEME_DEFAULT = false;

    private final Preferences settings;

    public S0DeviceScanPreferences(Preferences settings) {
        this.settings = settings;
    }

    public boolean useBroadcomScheme() {
        
        if (settings.contains(BROADCOM_SCHEME_KEY)) {
            return settings.getBoolean(BROADCOM_SCHEME_KEY);
        }
        return BROADCOM_SCHEME_DEFAULT;
    }

}
