/*
 * Copyright 2011-2022 Fraunhofer ISE
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

import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverNotAvailableException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.DeviceState;

public class RestDeviceWrapper {

    private final DeviceConfig config;
    private final DeviceState state;
    private final DriverInfo driver;
    private final List<RestChannel> channels;

    private RestDeviceWrapper(DeviceConfig config, DeviceState state, DriverInfo driver, List<RestChannel> channels) {
        this.config = config;
        this.state = state;
        this.driver = driver;
        this.channels = channels;
    }

    public String getId() {
        return config.getId();
    }

    public RestDeviceConfig getConfig() {
        return RestDeviceMapper.getRestDeviceConfig(config);
    }

    public RestDriverInfo getDriver() {
        return RestDriverMapper.getRestDriverDescription(driver);
    }

    public DeviceState getState() {
        return state;
    }

    public List<RestChannel> getChannels() {
        return channels;
    }

    public static RestDeviceWrapper getDevice(DeviceConfig config, ConfigService configService, DataAccessService data) {
        String driverId = config.getDriver().getId();
        DriverInfo driver;
        try {
            driver = configService.getDriverInfo(driverId);
            
        } catch (DriverNotAvailableException e) {
            driver = new DriverInfo(driverId, null, null, null, null, null);
        }
        
        List<RestChannel> channels = new LinkedList<RestChannel>();
        for (ChannelConfig channelConfig : config.getChannels()) {
            channels.add(RestChannelMapper.getRestChannel(data.getChannel(channelConfig.getId())));
        }
        return new RestDeviceWrapper(config, configService.getDeviceState(config.getId()), driver, channels);
    }

}
