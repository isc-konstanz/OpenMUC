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
package org.openmuc.framework.datalogger.ascii.test;

import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.spi.LogChannel;

public class LogChannelTestImpl implements LogChannel {

    private final String id;
    private final String description;
    private final String address;
    private final String unit;
    private final ValueType valueType;
    private final Double scalingFactor;
    private final Double valueOffset;
    private final Boolean listening;
    private final Integer samplingInterval;
    private final Integer samplingTimeOffset;
    private final String samplingGroup;
    private final Integer loggingInterval;
    private final Integer loggingTimeOffset;
    private final Boolean isEventLogging;
    private final Boolean disabled;

    private Integer valueLength;

    public LogChannelTestImpl(String id, String address, String description, String unit, 
            ValueType valueType, Double scalingFactor, Double valueOffset, Boolean listening, 
            Integer samplingInterval, Integer samplingTimeOffset, String samplingGroup, 
            Integer loggingInterval, Integer loggingTimeOffset, Boolean isEventLogging, 
            Boolean disabled) {

        this.id = id;
        this.description = description;
        this.address = address;
        this.unit = unit;
        this.valueType = valueType;
        this.scalingFactor = scalingFactor;
        this.valueOffset = valueOffset;
        this.listening = listening;
        this.samplingInterval = samplingInterval;
        this.samplingTimeOffset = samplingTimeOffset;
        this.samplingGroup = samplingGroup;
        this.loggingInterval = loggingInterval;
        this.loggingTimeOffset = loggingTimeOffset;
        this.isEventLogging = isEventLogging;
        this.disabled = disabled;
    }

    public LogChannelTestImpl(String id, String address, String description, String unit, 
            ValueType valueType, Double scalingFactor, Double valueOffset, Boolean listening, 
            Integer samplingInterval, Integer samplingTimeOffset, String samplingGroup, 
            Integer loggingInterval, Integer loggingTimeOffset, Boolean isEventLogging, 
            Boolean disabled, int valueLength) {

        this(id, description, address, unit, valueType, scalingFactor, valueOffset, listening, samplingInterval,
                samplingTimeOffset, samplingGroup, loggingInterval, loggingTimeOffset, isEventLogging, disabled);
        this.valueLength = valueLength;
    }

    @Override
    public String getId() {

        return id;
    }

    @Override
    public String getAddress() {

        return address;
    }

    @Override
    public String getSettings() {

        return "default";
    }

    @Override
    public String getDescription() {

        return description;
    }

    @Override
    public String getUnit() {

        return unit;
    }

    @Override
    public ValueType getValueType() {

        return valueType;
    }

    @Override
    public Integer getValueTypeLength() {

        return valueLength;
    }

    @Override
    public Double getScalingFactor() {

        return scalingFactor;
    }

    @Override
    public Double getValueOffset() {

        return valueOffset;
    }

    @Override
    public Boolean isListening() {

        return listening;
    }

    @Override
    public Integer getSamplingInterval() {

        return samplingInterval;
    }

    @Override
    public Integer getSamplingTimeOffset() {

        return samplingTimeOffset;
    }

    @Override
    public String getSamplingGroup() {

        return samplingGroup;
    }

    @Override
    public Integer getLoggingInterval() {

        return loggingInterval;
    }

    @Override
    public Integer getLoggingDelayMaximum() {
        return -1;
    }

    @Override
    public Integer getLoggingTimeOffset() {

        return loggingTimeOffset;
    }

    @Override
    public String getLoggingSettings() {
        return "default";
    }

    @Override
    public Double getLoggingTolerance() {
        return 0.0;
    }

    @Override
    public Boolean isloggingAverage() {
        return false;
    }

    @Override
    public Boolean isLoggingEvent() {
        return isEventLogging;
    }

    @Override
    public Boolean isDisabled() {

        return disabled;
    }

}
