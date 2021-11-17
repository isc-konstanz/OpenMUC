/*
 * Copyright 2011-2021 Fraunhofer ISE
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

package org.openmuc.framework.datalogger.spi;

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.parser.spi.SerializationContainer;

public class LoggingRecord implements SerializationContainer {

    private final String channelId;
    private final String channelAddress;
    private final String channelSettings;

    private final ValueType valueType;
    private final Integer valueTypeLength;
    
    private final Record record;

    public LoggingRecord(LogChannel channel, Record record) {
        this(channel.getId(), 
                channel.getAddress(), channel.getSettings(), 
                channel.getValueType(), channel.getValueTypeLength(), record);
    }

    public LoggingRecord(String channelId, Record record) {
        this(channelId,
                ChannelConfig.ADDRESS_DEFAULT,
                ChannelConfig.SETTINGS_DEFAULT,
                ChannelConfig.VALUE_TYPE_DEFAULT, null, record);
    }

    public LoggingRecord(String channelId, 
            String channelAddress, String ChannelSettings, 
            ValueType valueType, Integer ValueTypeLength, 
            Record record) {

        this.channelId = channelId;
        this.channelAddress = channelAddress;
        this.channelSettings = ChannelSettings;
        
        this.valueType = valueType;
        this.valueTypeLength = ValueTypeLength;
        
        this.record = record;
    }

    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getChannelAddress() {
        return channelAddress;
    }

    @Override
    public String getChannelSettings() {
        return channelSettings;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public Integer getValueTypeLength() {
        return valueTypeLength;
    }

    public Record getRecord() {
        return record;
    }

}