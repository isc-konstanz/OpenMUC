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

import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.options.Preferences;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;

public class S0DriverInfo extends DriverInfo {

    private final static S0DriverInfo info = new S0DriverInfo();

    private final Map<String, S0ChannelPreferences> channels = new HashMap<String, S0ChannelPreferences>();

    private S0DriverInfo() {
        super(S0DriverInfo.class.getResourceAsStream("options.xml"));
    }

    public static S0DriverInfo getInfo() {
        return info;
    }

    public S0DevicePreferences getDevicePreferences(String addressStr, String settingsStr) throws ArgumentSyntaxException {
        Preferences address = parseDeviceAddress(addressStr);
        Preferences settings = parseDeviceSettings(settingsStr);
        
        return new S0DevicePreferences(address, settings);
    }

    public S0DeviceScanPreferences getDeviceScanPreferences(String settingsStr) throws ArgumentSyntaxException {
        Preferences settings = parseDeviceScanSettings(settingsStr);
        
        return new S0DeviceScanPreferences(settings);
    }

    public S0ChannelPreferences getChannelPreferences(ChannelValueContainer container) throws ArgumentSyntaxException {
        String settings = container.getChannelSettings();
        
        return new S0ChannelPreferences(settings, parseChannelSettings(settings));
    }

    public S0ChannelPreferences getChannelPreferences(ChannelRecordContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        String settings = container.getChannelSettings();
        if (channels.containsKey(id)) {
            S0ChannelPreferences prefs = channels.get(id);
            if (prefs.equals(settings)) {
                return prefs;
            }
        }
        return new S0ChannelPreferences(settings, parseChannelSettings(settings));
    }
}
