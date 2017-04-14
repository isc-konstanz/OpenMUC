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
package org.openmuc.framework.lib.json.restObjects;

import org.openmuc.framework.config.DeviceInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.info.DeviceOptions;

public class RestDeviceInfo {

    private String description;

    private RestOptionCollection address;
    private RestOptionCollection settings;
    private RestOptionCollection scanSettings;
    private RestOptionCollection configs;

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

    public static RestDeviceInfo getRestDeviceInfo(DriverInfo driverInfo) {

        RestDeviceInfo restDeviceInfo = new RestDeviceInfo();
        if (driverInfo.getDeviceInfo() instanceof DeviceOptions) {
            DeviceOptions deviceOptions = (DeviceOptions) driverInfo.getDeviceInfo();
            
            restDeviceInfo.setDescription(deviceOptions.getDescription());
            restDeviceInfo.setAddress(RestOptionCollection.setOptionCollection(deviceOptions.getAddress()));
            restDeviceInfo.setSettings(RestOptionCollection.setOptionCollection(deviceOptions.getSettings()));
            restDeviceInfo.setScanSettings(RestOptionCollection.setOptionCollection(deviceOptions.getScanSettings()));
        }
        else {
            restDeviceInfo.setAddress(RestOptionCollection.setOptionCollection(driverInfo.getDeviceAddressSyntax()));
            restDeviceInfo.setSettings(RestOptionCollection.setOptionCollection(driverInfo.getDeviceSettingsSyntax()));
            restDeviceInfo.setScanSettings(RestOptionCollection.setOptionCollection(driverInfo.getDeviceScanSettingsSyntax()));
        }
        RestOptionCollection configs = RestOptionCollection.setOptionCollection(DeviceInfo.configs());
        configs.setSyntax(null);
        restDeviceInfo.setConfigs(configs);
        
        return restDeviceInfo;
    }

}
