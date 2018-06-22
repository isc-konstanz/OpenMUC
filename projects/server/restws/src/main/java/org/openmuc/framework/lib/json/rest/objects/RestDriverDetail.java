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

import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.DriverInfo;

public class RestDriverDetail {

    private String id;
    private String name = null;
    private Integer samplingTimeout = null;
    private Integer connectRetryInterval = null;
    private Boolean disabled = null;

    private List<String> devices = null;

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

    public Boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public static RestDriverDetail getRestDriverDetail(DriverInfo di, DriverConfig dc) {

        RestDriverDetail rdd = new RestDriverDetail();
        rdd.setId(di.getId());
        rdd.setName(di.getName());
        rdd.setConnectRetryInterval(dc.getConnectRetryInterval());
        rdd.setDisabled(dc.isDisabled());
        rdd.setSamplingTimeout(dc.getSamplingTimeout());
        
        List<String> deviceIds = new ArrayList<String>();
        for (DeviceConfig deviceConfig : dc.getDevices()) {
            deviceIds.add(deviceConfig.getId());
        }
        rdd.setDevices(deviceIds);
        
        return rdd;
    }

}
