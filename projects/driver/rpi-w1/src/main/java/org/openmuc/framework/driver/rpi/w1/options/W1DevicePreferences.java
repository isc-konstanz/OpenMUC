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

public class W1DevicePreferences {

    public static final String ID_KEY = "id";

    public static final String TYPE_KEY = "type";

    private final Preferences address;
    private final Preferences settings;

    public W1DevicePreferences(Preferences address, Preferences settings) {
        this.address = address;
        this.settings = settings;
    }

    public String getId() {
        if (address.contains(ID_KEY)) {
            return address.getString(ID_KEY);
        }
        return null;
    }

    public W1Type getType() {
        if (settings.contains(TYPE_KEY)) {
            return W1Type.newType(settings.getString(TYPE_KEY));
        }
        return null;
    }

}
