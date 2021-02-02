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

package org.openmuc.framework.config;

import org.openmuc.framework.data.ValueType;

/**
 * The <code>ChannelInfo</code> class is used to access a single data field of a communication device. 
 * A channel info instance can be used to get configuration information about this channel such as its unit.
 */
public interface ChannelInfo {

    String getId();

    String getDescription();

    String getUnit();

    ValueType getValueType();

    Integer getValueTypeLength();

    Double getValueOffset();

    Double getScalingFactor();

}
