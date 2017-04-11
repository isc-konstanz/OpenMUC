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

import org.openmuc.framework.config.DriverInfo;

public class RestDriverInfo {

    private String id;
    private String name;
    private String description;

    private RestDeviceInfo device;
    private RestChannelInfo channel;
    private RestOptionCollection configs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RestDeviceInfo getDevice() {
        return device;
    }

    public void setDevice(RestDeviceInfo device) {
        this.device = device;
    }

    public RestChannelInfo getChannel() {
        return channel;
    }

    public void setChannel(RestChannelInfo channel) {
        this.channel = channel;
    }

    public RestOptionCollection getConfigs() {
        return configs;
    }

    public void setConfigs(RestOptionCollection configs) {
        this.configs = configs;
    }

    public static RestDriverInfo setDriverInfo(DriverInfo driverInfo, boolean detail) {

        RestDriverInfo restDriverInfo = new RestDriverInfo();
        restDriverInfo.setId(driverInfo.getId());
        restDriverInfo.setName(driverInfo.getName());
        restDriverInfo.setDescription(driverInfo.getDescription());
        
        if (detail) {
            restDriverInfo.setDevice(RestDeviceInfo.setDeviceInfo(driverInfo));
            restDriverInfo.setChannel(RestChannelInfo.setChannelInfo(driverInfo));
        }
        RestOptionCollection configs = RestOptionCollection.setOptionCollection(DriverInfo.configs());
        configs.setSyntax(null);
        restDriverInfo.setConfigs(configs);
        
        return restDriverInfo;
    }

}
