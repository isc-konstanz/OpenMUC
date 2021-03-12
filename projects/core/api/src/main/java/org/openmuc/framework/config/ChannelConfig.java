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

package org.openmuc.framework.config;

import java.util.List;

import org.openmuc.framework.data.ValueType;

public interface ChannelConfig {

    Boolean DISABLED_DEFAULT = false;
    String DESCRIPTION_DEFAULT = "";
    String ADDRESS_DEFAULT = "";
    String SETTINGS_DEFAULT = "";
    String UNIT_DEFAULT = "";
    ValueType VALUE_TYPE_DEFAULT = ValueType.DOUBLE;
    int BYTE_ARRAY_SIZE_DEFAULT = 10;
    int STRING_SIZE_DEFAULT = 10;
    boolean LISTENING_DEFAULT = false;
    int SAMPLING_INTERVAL_DEFAULT = -1;
    int SAMPLING_TIME_OFFSET_DEFAULT = 0;
    String SAMPLING_GROUP_DEFAULT = "";
    int LOGGING_INTERVAL_DEFAULT = -1;
    int LOGGING_TIME_OFFSET_DEFAULT = 0;
    boolean LOGGING_EVENT_DEFAULT = false;
    String LOGGING_SETTINGS_DEFAULT = "";
    String LOGGING_READER_DEFAULT = "";

    String getId();

    void setId(String id) throws IdCollisionException;

    String getDescription();

    void setDescription(String description);

    String getAddress();

    void setAddress(String address);

    String getSettings();

    void setSettings(String settings);

    String getUnit();

    void setUnit(String unit);

    ValueType getValueType();

    void setValueType(ValueType type);

    Integer getValueTypeLength();

    void setValueTypeLength(Integer maxLength);

    Double getValueOffset();

    void setValueOffset(Double offset);

    Double getScalingFactor();

    void setScalingFactor(Double factor);

    Boolean isListening();

    void setListening(Boolean listening);

    Integer getSamplingInterval();

    void setSamplingInterval(Integer interval);

    Integer getSamplingTimeOffset();

    void setSamplingTimeOffset(Integer offset);

    String getSamplingGroup();

    void setSamplingGroup(String group);

    Integer getLoggingInterval();

    void setLoggingInterval(Integer interval);

    Integer getLoggingTimeOffset();

    void setLoggingTimeOffset(Integer offset);

    String getLoggingSettings();

    void setLoggingSettings(String settings);

    Boolean isLoggingEvent();

    void setLoggingEvent(Boolean loggingEvent);

    String getReader();

    void setReader(String reader);

    Boolean isDisabled();

    void setDisabled(Boolean disabled);

    void delete();

    DeviceConfig getDevice();

    List<ServerMapping> getServerMappings();

    void addServerMapping(ServerMapping serverMapping);

    void deleteServerMappings(String id);
}
