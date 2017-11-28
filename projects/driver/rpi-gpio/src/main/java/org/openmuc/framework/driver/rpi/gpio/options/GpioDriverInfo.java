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

import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.options.Preferences;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;

public class GpioDriverInfo extends DriverInfo {

    private final static GpioDriverInfo info = new GpioDriverInfo();

    private final Map<String, GpioChannelPreferences> channels = new HashMap<String, GpioChannelPreferences>();

    private GpioDriverInfo() {
        super(GpioDriverInfo.class.getResourceAsStream("options.xml"));
    }

    public static GpioDriverInfo getInfo() {
        return info;
    }

    public GpioDevicePreferences getDevicePreferences(String addressStr, String settingsStr) throws ArgumentSyntaxException {
        Preferences address = parseDeviceAddress(addressStr);
        Preferences settings = parseDeviceSettings(settingsStr);
        
        return new GpioDevicePreferences(address, settings);
    }

    public GpioDeviceScanPreferences getDeviceScanPreferences(String settingsStr) throws ArgumentSyntaxException {
        Preferences settings = parseDeviceScanSettings(settingsStr);
        
        return new GpioDeviceScanPreferences(settings);
    }

    public GpioChannelPreferences getChannelPreferences(ChannelValueContainer container) throws ArgumentSyntaxException {
        String settings = container.getChannelSettings();
        
        return new GpioChannelPreferences(settings, parseChannelSettings(settings));
    }

    public GpioChannelPreferences getChannelPreferences(ChannelRecordContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        String settings = container.getChannelSettings();
        if (channels.containsKey(id)) {
            GpioChannelPreferences prefs = channels.get(id);
            if (prefs.equals(settings)) {
                return prefs;
            }
        }
        return new GpioChannelPreferences(settings, parseChannelSettings(settings));
    }
}
