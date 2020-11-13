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
package org.openmuc.framework.driver;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DeviceState;

public abstract class ChannelContext extends Configurable {

    Channel channel;

    DeviceContext context;

    <C extends DeviceContext> void doCreate(C context, Channel channel) throws ArgumentSyntaxException {
        this.channel = channel;
        this.context = context;
        this.onCreate(context);
        this.onCreate();
    }

    protected <C extends DeviceContext> void onCreate(C context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onDestroy() {
        // Placeholder for the optional implementation
    }

    public final DeviceContext getDevice() {
        return context;
    }

    public final String getId() {
        return channel.getId();
    }

    public final String getDescription() {
        return channel.getDescription();
    }

    public final String getUnit() {
        return channel.getUnit();
    }

    public final ValueType getValueType() {
        return channel.getValueType();
    }

    public final int getValueTypeLength() {
        return channel.getValueTypeLength();
    }

    public final double getScalingFactor() {
        return channel.getScalingFactor();
    }

    public final int getSamplingInterval() {
        return channel.getSamplingInterval();
    }

    public final int getSamplingTimeOffset() {
        return channel.getSamplingTimeOffset();
    }

    public final int getLoggingInterval() {
        return channel.getLoggingInterval();
    }

    public final int getLoggingTimeOffset() {
        return channel.getLoggingTimeOffset();
    }

    public final String getLoggingSettings() {
        return channel.getLoggingSettings();
    }

    public final String getDriverId() {
        return channel.getDriverId();
    }

    public final String getDeviceId() {
        return channel.getDeviceId();
    }

    public final String getDeviceDescription() {
        return channel.getDeviceDescription();
    }

    public final String getDeviceAddress() {
        return channel.getDeviceAddress();
    }

    public final String getDeviceSettings() {
        return channel.getDeviceSettings();
    }

    public final DeviceState getDeviceState() {
        return channel.getDeviceState();
    }

    public final ChannelState getState() {
        return channel.getChannelState();
    }

    public final boolean isConnected() {
        return channel.isConnected();
    }

    public abstract Record getRecord();

    @Override
    public String toString() {
        return getId()+" ("+getValueType().toString()+"): "+getRecord().toString();
    }

}
