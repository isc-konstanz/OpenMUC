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

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DataAccessService;

public class RestChannelWrapper {

    private final ChannelConfig config;
    private final Channel channel;

    private RestChannelWrapper(ChannelConfig config, Channel channel) {
		this.config = config;
		this.channel = channel;
	}

    public String getId() {
    	return config.getId();
    }

    public RestChannelConfig getConfig() {
    	return RestChannelMapper.getRestChannelConfig(config);
    }

    public ValueType getValueType() {
    	if (config.getValueType() != null) {
    		return config.getValueType();
    	}
    	return ChannelConfig.VALUE_TYPE_DEFAULT;
    }

    public String getDriver() {
    	return channel.getDriverName();
    }

    public String getDevice() {
    	return channel.getDeviceName();
    }

    public ChannelState getState() {
        return channel.getChannelState();
    }

    public Record getLatestRecord() {
    	return channel.getLatestRecord();
    }

    public static RestChannelWrapper getChannel(ChannelConfig config, DataAccessService data) {
    	return new RestChannelWrapper(config, data.getChannel(config.getId()));
    }

}
