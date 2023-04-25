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
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverNotAvailableException;
import org.openmuc.framework.dataaccess.DataAccessService;

public class RestDriverWrapper {

    private final DriverConfig config;
    private final DriverInfo driver;
    private final List<RestDevice> devices;

    private final boolean running;

    private RestDriverWrapper(DriverConfig config, DriverInfo driver, List<RestDevice> devices, boolean running) {
        this.config = config;
        this.driver = driver;
        this.devices = devices;
        this.running = running;
    }

    public String getId() {
        return config.getId();
    }

    public RestDriverConfig getConfig() {
        return RestDriverMapper.getRestDriverConfig(config);
    }

    public RestDriverInfo getDriver() {
        return RestDriverMapper.getRestDriverDescription(driver);
    }

    public List<RestDevice> getDevices() {
        return devices;
    }

    public boolean isRunning() {
        return running;
    }

    public static RestDriverWrapper getDriver(DriverConfig config, ConfigService configService, DataAccessService data) {
        boolean running;
        DriverInfo driver;
        try {
            driver = configService.getDriverInfo(config.getId());
            running = true;
            
        } catch (DriverNotAvailableException e) {
            driver = new DriverInfo(config.getId(), null, null, null, null, null);
            running = false;
        }

        List<RestDevice> devices = new LinkedList<RestDevice>();
        for (DeviceConfig device : config.getDevices()) {
            List<RestChannel> channels = new LinkedList<RestChannel>();
            for (ChannelConfig channel : device.getChannels()) {
                channels.add(RestChannelMapper.getRestChannel(data.getChannel(channel.getId())));
            }
            devices.add(RestDeviceMapper.getRestDevice(device, configService.getDeviceState(device.getId()), channels));
        }
        return new RestDriverWrapper(config, driver, devices, running);
    }

}
