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

import org.openmuc.framework.config.ChannelInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.info.ChannelOptions;

public class RestChannelInfo {

    private String description;

    private RestOptionCollection address;
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

    public static RestChannelInfo getRestChannelInfo(DriverInfo driverInfo) {

        RestChannelInfo restChannelInfo = new RestChannelInfo();
        if (driverInfo.getChannelInfo() instanceof ChannelOptions) {
            ChannelOptions channelOptions = (ChannelOptions) driverInfo.getChannelInfo();
            
            restChannelInfo.setDescription(channelOptions.getDescription());
            restChannelInfo.setAddress(RestOptionCollection.setOptionCollection(channelOptions.getAddress()));
            restChannelInfo.setScanSettings(RestOptionCollection.setOptionCollection(channelOptions.getScanSettings()));
        }
        else {
            restChannelInfo.setAddress(RestOptionCollection.setOptionCollection(driverInfo.getChannelAddressSyntax()));
            restChannelInfo.setScanSettings(RestOptionCollection.setOptionCollection(driverInfo.getChannelScanSettingsSyntax()));
        }
        RestOptionCollection configs = RestOptionCollection.setOptionCollection(ChannelInfo.configs());
        configs.setSyntax(null);
        restChannelInfo.setConfigs(configs);
        
        return restChannelInfo;
    }

}
