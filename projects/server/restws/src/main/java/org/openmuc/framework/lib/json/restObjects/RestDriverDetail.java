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

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DriverConfig;

public class RestDriverDetail extends RestDriverConfig {

    protected List<String> devices = null;

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public static RestDriverDetail getRestDriverDetail(DriverConfig dc) {

        RestDriverDetail rdd = new RestDriverDetail();
        rdd.setId(dc.getId());
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
