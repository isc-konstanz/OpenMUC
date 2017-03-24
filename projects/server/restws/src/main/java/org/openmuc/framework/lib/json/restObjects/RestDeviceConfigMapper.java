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

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.IdCollisionException;
import org.openmuc.framework.lib.json.exceptions.RestConfigIsNotCorrectException;

public class RestDeviceConfigMapper {

    public static RestDeviceConfig getRestDeviceConfig(DeviceConfig dc) {

        RestDeviceConfig rdc = new RestDeviceConfig();
        rdc.setId(dc.getId());
        rdc.setDescription(dc.getDescription());
        rdc.setDeviceAddress(dc.getDeviceAddress());
        rdc.setSettings(dc.getSettings());
        rdc.setSamplingTimeout(dc.getSamplingTimeout());
        rdc.setConnectRetryInterval(dc.getConnectRetryInterval());
        rdc.isDisabled(dc.isDisabled());
        rdc.setDriver(dc.getDriver().getId());
        List<String> channelIds = new ArrayList<String>(dc.getChannels().size());
        for (ChannelConfig dvc : dc.getChannels()) {
            channelIds.add(dvc.getId());
        }
        rdc.setChannels(channelIds);
        return rdc;
    }

    public static void setDeviceConfig(DeviceConfig dc, RestDeviceConfig rdc, String idFromUrl)
            throws IdCollisionException, RestConfigIsNotCorrectException {

        if (dc == null) {
            throw new RestConfigIsNotCorrectException("DriverConfig is null!");
        }
        else {

            if (rdc != null) {
                if (rdc.getId() != null && !rdc.getId().equals("") && !idFromUrl.equals(rdc.getId())) {
                    dc.setId(rdc.getId());
                }
                dc.setDescription(rdc.getDescription());
                dc.setDeviceAddress(rdc.getDeviceAddress());
                dc.setSamplingTimeout(rdc.getSamplingTimeout());
                dc.setConnectRetryInterval(rdc.getConnectRetryInterval());
                dc.setSettings(rdc.getSettings());
                dc.setDisabled(rdc.getDisabled());
            }
            else {
                throw new RestConfigIsNotCorrectException();
            }
        }

    }
}
