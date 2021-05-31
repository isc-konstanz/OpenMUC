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
package org.openmuc.framework.datalogger;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.annotation.Configure;
import org.openmuc.framework.datalogger.spi.LogChannel;

public abstract class ChannelWrapper extends Reflectable {

    LogChannel channel;

    String settings = "";

    protected ChannelWrapper() {
    }

    void invokeConfigure(LoggingChannelContext context, LogChannel channel, Settings settings) 
            throws ArgumentSyntaxException {
        
    	// TODO: verify equality only for logger specific settings
        if (!equals(channel)) {
            configure(settings);
            
            this.settings = channel.getLoggingSettings();
            this.channel = channel;
            
            if (isAveraging()) {
                switch (getValueType()) {
                case BOOLEAN:
                case BYTE:
                case BYTE_ARRAY:
                case STRING:
                    throw new ArgumentSyntaxException("Invalid value type \"" + getValueType() + "\" to calculate average of channel: " + getId());
                default:
                    break;
                }
            }
            invokeMethod(Configure.class, this, context, settings);
            invokeMethod(Configure.class, this, context);
            invokeMethod(Configure.class, this);
            return;
        }
        this.settings = channel.getLoggingSettings();
        this.channel = channel;
    }

    public final String getId() {
        return channel.getId();
    }

    public final String getDescription() {
        return channel.getDescription();
    }

    public String getAddress() {
        return channel.getAddress();
    }

    public String getSettings() {
        return channel.getSettings();
    }

    public final String getUnit() {
        return channel.getUnit();
    }

    public final ValueType getValueType() {
        return channel.getValueType();
    }

    public final Integer getValueTypeLength() {
        return channel.getValueTypeLength();
    }

    public final Double getValueOffset() {
        return channel.getValueOffset();
    }

    public final Double getScalingFactor() {
        return channel.getScalingFactor();
    }

    public final int getLoggingInterval() {
        return channel.getLoggingInterval();
    }

    public final int getLoggingTimeMax() {
        return channel.getLoggingDelayMaximum();
    }

    public final int getLoggingTimeOffset() {
        return channel.getLoggingTimeOffset();
    }

    public final double getLoggingTolerance() {
        return channel.getLoggingTolerance();
    }

    public final boolean isLoggingEvent() {
        return channel.isLoggingEvent();
    }

    final boolean isLoggingDynamic() {
        return channel.getLoggingDelayMaximum() > getLoggingInterval();
    }

    public final boolean isAveraging() {
        return channel.isLoggingAveraging();
    }

    public boolean equals(LogChannel channel) {
        return this.channel != null && channel != null && 
                this.channel.getId().equals(channel.getId()) && 
                this.settings.equals(channel.getLoggingSettings());
    }

}
