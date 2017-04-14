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
import org.openmuc.framework.dataaccess.DeviceState;

public class RestDeviceDetail extends RestDeviceConfig {

	public RestDeviceDetail() {
		super();
	}
	
	protected String driver = null;
	protected DeviceState state = null;
	protected List<String> channels = null;

    public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
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

	public static RestDeviceDetail getRestDeviceDetail(DeviceState state, DeviceConfig dc) {

    	RestDeviceDetail rdd = new RestDeviceDetail();
        rdd.setId(dc.getId());
        rdd.setDescription(dc.getDescription());
        rdd.setDeviceAddress(dc.getDeviceAddress());
        rdd.setSettings(dc.getSettings());
        rdd.setSamplingTimeout(dc.getSamplingTimeout());
        rdd.setConnectRetryInterval(dc.getConnectRetryInterval());
        rdd.isDisabled(dc.isDisabled());
        
        rdd.setDriver(dc.getDriver().getId());
        rdd.setState(state);
        
        List<String> channelIds = new ArrayList<String>();
        for (ChannelConfig channelConfig : dc.getChannels()) {
        	channelIds.add(channelConfig.getId());
        }
        rdd.setChannels(channelIds);
        
        return rdd;
    }

}
