/*
 * Copyright 2011-18 Fraunhofer ISE
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
package org.openmuc.framework.lib.json.rest.objects;

import java.io.IOException;

import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.config.options.OptionCollection;

public class RestDeviceInfo {

    private String description = null;

    private RestOptionCollection address = null;
    private RestOptionCollection settings = null;
    private RestOptionCollection scanSettings = null;
    private RestOptionCollection configs = null;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public RestOptionCollection getAddress() {
        return address;
    }

    public void setAddress(RestOptionCollection address) {
        this.address = address;
    }

    public RestOptionCollection getSettings() {
        return settings;
    }

    public void setSettings(RestOptionCollection settings) {
        this.settings = settings;
    }

    public RestOptionCollection getScanSettings() {
        return scanSettings;
    }

    public void setScanSettings(RestOptionCollection scanSettings) {
        this.scanSettings = scanSettings;
    }

    public RestOptionCollection getConfigs() {
        return configs;
    }

    public void setConfigs(RestOptionCollection configs) {
        this.configs = configs;
    }

    public static RestDeviceInfo getRestDeviceInfo(DriverInfo driverInfo) throws ParseException, IOException {

        RestDeviceInfo restDeviceInfo = new RestDeviceInfo();
        if (driverInfo.getDeviceAddress() instanceof OptionCollection) {
            restDeviceInfo.setAddress(RestOptionCollection.parseOptionCollection((OptionCollection) driverInfo.getDeviceAddress()));
        }
        else if (driverInfo.getDeviceAddress() != null) {
            restDeviceInfo.setAddress(RestOptionCollection.parseOptionCollection(RestOptionCollection.ADDRESS, driverInfo.getDeviceAddress().getSyntax()));
        }
        if (driverInfo.getDeviceSettings() instanceof OptionCollection) {
            restDeviceInfo.setSettings(RestOptionCollection.parseOptionCollection((OptionCollection) driverInfo.getDeviceSettings()));
        }
        else if (driverInfo.getDeviceSettings() != null) {
            restDeviceInfo.setSettings(RestOptionCollection.parseOptionCollection(RestOptionCollection.SETTINGS, driverInfo.getDeviceSettings().getSyntax()));
        }
        if (driverInfo.getDeviceScanSettings() instanceof OptionCollection) {
            restDeviceInfo.setScanSettings(RestOptionCollection.parseOptionCollection((OptionCollection) driverInfo.getDeviceScanSettings()));
        }
        else if (driverInfo.getDeviceScanSettings() != null) {
            restDeviceInfo.setScanSettings(RestOptionCollection.parseOptionCollection(RestOptionCollection.SCAN_SETTINGS, driverInfo.getDeviceScanSettings().getSyntax()));
        }
        RestOptionCollection configs = RestOptionCollection.parseOptionCollection((OptionCollection) driverInfo.getDeviceConfig());
        configs.setSyntax(null);
        restDeviceInfo.setConfigs(configs);
        
        return restDeviceInfo;
    }

}