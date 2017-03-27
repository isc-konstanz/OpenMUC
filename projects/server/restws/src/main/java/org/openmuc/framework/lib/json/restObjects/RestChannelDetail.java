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

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;

public class RestChannelDetail extends RestChannelConfig {

	protected String device = null;
	protected Flag flag = null;
	protected ChannelState state = null;

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

	public ChannelState getState() {
		return state;
	}

	public void setState(ChannelState state) {
		this.state = state;
	}

    public static RestChannelDetail getRestChannelDetail(Channel c, ChannelConfig cc) {
    	
        RestChannelDetail rcd = new RestChannelDetail();
        rcd.setId(cc.getId());
        rcd.setDescription(cc.getDescription());
        rcd.setChannelAddress(cc.getChannelAddress());
        rcd.setUnit(cc.getUnit());
        rcd.setValueType(cc.getValueType());
        rcd.setValueTypeLength(cc.getValueTypeLength());
        rcd.setScalingFactor(cc.getScalingFactor());
        rcd.setValueOffset(cc.getValueOffset());
        rcd.setListening(cc.isListening());
        rcd.setSamplingInterval(cc.getSamplingInterval());
        rcd.setSamplingTimeOffset(cc.getSamplingTimeOffset());
        rcd.setSamplingGroup(cc.getSamplingGroup());
        rcd.setLoggingInterval(cc.getLoggingInterval());
        rcd.setLoggingTimeOffset(cc.getLoggingTimeOffset());
        rcd.setLoggingSettings(cc.getLoggingSettings());
        rcd.setDisabled(cc.isDisabled());
        
        rcd.setDevice(c.getDeviceName());
        rcd.setFlag(c.getLatestRecord().getFlag());
        rcd.setState(c.getChannelState());
        return rcd;
    }

}
