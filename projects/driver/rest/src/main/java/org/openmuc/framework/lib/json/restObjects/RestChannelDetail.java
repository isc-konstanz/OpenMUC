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

import java.util.List;

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ServerMapping;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;

public class RestChannelDetail {

    private String id = null;
    private String channelAddress = null;
    private String channelSettings = null;
    private String description = null;
    private String unit = null;
    private ValueType valueType = null;
    private Integer valueTypeLength = null;
    private Double scalingFactor = null;
    private Double valueOffset = null;
    private Boolean listening = null;
    private Integer samplingInterval = null;
    private Integer samplingTimeOffset = null;
    private String samplingGroup = null;
    private Integer loggingInterval = null;
    private Integer loggingTimeOffset = null;
    private String loggingSettings = null;
    private Boolean disabled = null;
    private List<ServerMapping> serverMappings = null;

    private String driver = null;
    private String device = null;
    private Long timestamp = null;
    private Object value = null;
    private Flag flag = null;
    private ChannelState state = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelAddress() {
        return channelAddress;
    }

    public void setChannelAddress(String channelAddress) {
        this.channelAddress = channelAddress;
    }

    public String getChannelSettings() {
        return channelSettings;
    }

    public void setChannelSettings(String settings) {
        channelSettings = settings;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public Integer getValueTypeLength() {
        return valueTypeLength;
    }

    public void setValueTypeLength(Integer valueTypeLength) {
        this.valueTypeLength = valueTypeLength;
    }

    public Double getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(Double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public Double getValueOffset() {
        return valueOffset;
    }

    public void setValueOffset(Double valueOffset) {
        this.valueOffset = valueOffset;
    }

    public Boolean isListening() {
        return listening;
    }

    public void setListening(Boolean listening) {
        this.listening = listening;
    }

    public Integer getSamplingInterval() {
        return samplingInterval;
    }

    public void setSamplingInterval(Integer samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public Integer getSamplingTimeOffset() {
        return samplingTimeOffset;
    }

    public void setSamplingTimeOffset(Integer samplingTimeOffset) {
        this.samplingTimeOffset = samplingTimeOffset;
    }

    public String getSamplingGroup() {
        return samplingGroup;
    }

    public void setSamplingGroup(String samplingGroup) {
        this.samplingGroup = samplingGroup;
    }

    public Integer getLoggingInterval() {
        return loggingInterval;
    }

    public void setLoggingInterval(Integer loggingInterval) {
        this.loggingInterval = loggingInterval;
    }

    public Integer getLoggingTimeOffset() {
        return loggingTimeOffset;
    }

    public void setLoggingTimeOffset(Integer loggingTimeOffset) {
        this.loggingTimeOffset = loggingTimeOffset;
    }

    public String getLoggingSettings() {
        return loggingSettings;
    }

    public void setLoggingSettings(String settings) {
        this.loggingSettings = settings;
    }

    public Boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public List<ServerMapping> getServerMappings() {
        return serverMappings;
    }

    public void setServerMappings(List<ServerMapping> serverMappings) {
        this.serverMappings = serverMappings;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
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
        rcd.setChannelSettings(cc.getChannelSettings());
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

        rcd.setDriver(c.getDriverName());
        rcd.setDevice(c.getDeviceName());
        if (c.getLatestRecord() != null) {
        	Record rc = c.getLatestRecord();
        	
        	rcd.setTimestamp(rc.getTimestamp());
        	
            Flag flag = rc.getFlag();
            Value value = rc.getValue();
            if (value != null) {
                switch (c.getValueType()) {
                case FLOAT:
                    if (Float.isInfinite(value.asFloat())) {
                    	flag = Flag.VALUE_IS_INFINITY;
                    }
                    else if (Float.isNaN(value.asFloat())) {
                    	flag = Flag.VALUE_IS_NAN;
                    }
                	rcd.setValue(value.asFloat());
                    break;
                case DOUBLE:
                    if (Double.isInfinite(value.asDouble())) {
                    	flag = Flag.VALUE_IS_INFINITY;
                    }
                    else if (Double.isNaN(value.asDouble())) {
                    	flag = Flag.VALUE_IS_NAN;
                    }
                	rcd.setValue(value.asDouble());
                    break;
                case SHORT:
                	rcd.setValue(value.asShort());
                    break;
                case INTEGER:
                	rcd.setValue(value.asInt());
                    break;
                case LONG:
                	rcd.setValue(value.asLong());
                    break;
                case BYTE:
                	rcd.setValue(value.asByte());
                    break;
                case BOOLEAN:
                	rcd.setValue(value.asBoolean());
                    break;
                case BYTE_ARRAY:
                	rcd.setValue(value.asByteArray());
                    break;
                case STRING:
                	rcd.setValue(value.asString());
                    break;
                default:
                	rcd.setValue(null);
                    break;
                }
            }
            else {
            	rcd.setValue(null);
            }
        	rcd.setFlag(flag);
        }
        rcd.setState(c.getChannelState());
        
        return rcd;
    }

}
