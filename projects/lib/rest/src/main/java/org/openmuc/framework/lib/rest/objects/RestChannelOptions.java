/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.lib.rest.objects;

import java.io.IOException;

import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.config.option.ChannelOptions;
import org.openmuc.framework.config.option.DriverOptions;

public class RestChannelOptions {

    private String description = null;

    private RestOptions address = null;
    private RestOptions settings = null;
    private RestOptions scanSettings = null;
    private RestOptions configs = null;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public RestOptions getAddress() {
        return address;
    }

    public void setAddress(RestOptions address) {
        this.address = address;
    }
    
    public RestOptions getSettings() {
        return settings;
    }

    public void setSettings(RestOptions settings) {
        this.settings = settings;
    }

    public RestOptions getScanSettings() {
        return scanSettings;
    }

    public void setScanSettings(RestOptions scanSettings) {
        this.scanSettings = scanSettings;
    }

    public RestOptions getConfigs() {
        return configs;
    }

    public void setConfigs(RestOptions configs) {
        this.configs = configs;
    }

    public static RestChannelOptions getRestChannelInfo(DriverInfo driverInfo) 
            throws ParseException, IOException {
        
        if (driverInfo instanceof DriverOptions) {
            return getRestChannelDetail((DriverOptions) driverInfo);
        }
        
        RestChannelOptions restChannelInfo = new RestChannelOptions();
        restChannelInfo.setAddress(RestOptions.parseOptions(RestOptions.ADDRESS, driverInfo.getChannel().getAddressSyntax()));
        restChannelInfo.setSettings(RestOptions.parseOptions(RestOptions.SETTINGS, driverInfo.getChannel().getSettingsSyntax()));
        restChannelInfo.setScanSettings(RestOptions.parseOptions(RestOptions.SCAN_SETTINGS, driverInfo.getChannel().getScanSettingsSyntax()));
        
        RestOptions configs = RestOptions.parseOptions(ChannelOptions.readConfigOptions());
        configs.setSyntax(null);
        restChannelInfo.setConfigs(configs);
        
        return restChannelInfo;
    }

    private static RestChannelOptions getRestChannelDetail(DriverOptions driverDetail) 
            throws ParseException, IOException {

        RestChannelOptions restChannelInfo = new RestChannelOptions();
        restChannelInfo.setAddress(RestOptions.parseOptions(driverDetail.getChannel().getAddressOptions()));
        restChannelInfo.setSettings(RestOptions.parseOptions(driverDetail.getChannel().getSettingsOptions()));
        restChannelInfo.setScanSettings(RestOptions.parseOptions(driverDetail.getChannel().getScanSettingsOptions()));

        RestOptions configs = RestOptions.parseOptions(ChannelOptions.readConfigOptions());
        configs.setSyntax(null);
        restChannelInfo.setConfigs(configs);
        
        return restChannelInfo;
    }

}
