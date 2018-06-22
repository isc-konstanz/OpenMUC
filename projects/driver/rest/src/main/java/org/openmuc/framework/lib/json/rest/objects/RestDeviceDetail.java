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

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.dataaccess.DeviceState;

public class RestDeviceDetail {

    private String id;
    private String description = null;
    private String deviceAddress = null;
    private String settings = null;
    private Integer samplingTimeout = null;
    private Integer connectRetryInterval = null;
    private Boolean disabled = null;

    private String driver = null;
    private String driverName = null;
    private DeviceState state = null;
    private List<String> channels = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public Integer getSamplingTimeout() {
        return samplingTimeout;
    }

    public void setSamplingTimeout(Integer samplingTimeout) {
        this.samplingTimeout = samplingTimeout;
    }

    public Integer getConnectRetryInterval() {
        return connectRetryInterval;
    }

    public void setConnectRetryInterval(Integer connectRetryInterval) {
        this.connectRetryInterval = connectRetryInterval;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void isDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public static RestDeviceDetail getRestDeviceDetail(DeviceState state, DeviceConfig dc, DriverInfo info) {

        RestDeviceDetail rdd = new RestDeviceDetail();
        rdd.setId(dc.getId());
        rdd.setDescription(dc.getDescription());
        rdd.setDeviceAddress(dc.getDeviceAddress());
        rdd.setSettings(dc.getSettings());
        rdd.setSamplingTimeout(dc.getSamplingTimeout());
        rdd.setConnectRetryInterval(dc.getConnectRetryInterval());
        rdd.isDisabled(dc.isDisabled());

        rdd.setDriver(info.getId());
        rdd.setDriverName(info.getName());
        rdd.setState(state);
        
        List<String> channelIds = new ArrayList<String>();
        for (ChannelConfig channelConfig : dc.getChannels()) {
            channelIds.add(channelConfig.getId());
        }
        rdd.setChannels(channelIds);
        
        return rdd;
    }

}
