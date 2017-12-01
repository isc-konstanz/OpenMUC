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
package org.openmuc.framework.driver.iec62056p21.options;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.options.Preferences;

public class Iec62056DriverInfo extends DriverInfo {

    private final static Iec62056DriverInfo info = new Iec62056DriverInfo();

    private Iec62056DriverInfo() {
        super(Iec62056DriverInfo.class.getResourceAsStream("options.xml"));
    }

    public static Iec62056DriverInfo getInfo() {
        return info;
    }

    public Iec62056DevicePreferences getDevicePreferences(String addressStr, String settingsStr) throws ArgumentSyntaxException {
        Preferences address = parseDeviceAddress(addressStr);
        Preferences settings = parseDeviceSettings(settingsStr);
        
        return new Iec62056DevicePreferences(address, settings);
    }

    public Iec62056DeviceScanPreferences getDeviceScanPreferences(String settingsStr) throws ArgumentSyntaxException {
        Preferences settings = parseDeviceScanSettings(settingsStr);
        
        return new Iec62056DeviceScanPreferences(settings);
    }
}
