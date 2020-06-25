/*
 * Copyright 2011-2020 Fraunhofer ISE
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

import org.openmuc.framework.data.ValueType;

public interface LogChannel {

    public String getId();

    public String getDescription();

    public String getChannelAddress();

    public String getUnit();

    public ValueType getValueType();

    public Integer getValueTypeLength();

    public Double getScalingFactor();

    public Double getValueOffset();

    public Boolean isListening();

    public Integer getSamplingInterval();

    public Integer getSamplingTimeOffset();

    public String getSamplingGroup();

    public Integer getLoggingInterval();

    public Integer getLoggingTimeOffset();

    public Boolean isDisabled();

    public Boolean isLoggingEvent();

}
