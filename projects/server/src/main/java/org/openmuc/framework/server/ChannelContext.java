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
package org.openmuc.framework.server;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FutureValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DataLoggerNotAvailableException;
import org.openmuc.framework.dataaccess.DeviceState;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.framework.options.Configurable;

public abstract class ChannelContext extends Configurable {

    Channel channel;

    ServerContext context;

    <C extends ServerContext> void doCreate(C context, Channel channel) throws ArgumentSyntaxException {
        this.channel = channel;
        this.context = context;
        this.onCreate(context);
        this.onCreate();
    }

    protected <C extends ServerContext> void onCreate(C context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onDestroy() {
        // Placeholder for the optional implementation
    }

    public final ServerContext getServer() {
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

    public final String getAddress() {
        return channel.getChannelAddress();
    }

    public final String getSettings() {
        return channel.getChannelSettings();
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

    public final void addListener(RecordListener listener) {
        channel.addListener(listener);
    }

    public final void removeListener(RecordListener listener) {
        channel.removeListener(listener);
    }

    public final Record read() {
        return channel.read();
    }

    public final Record getRecord() {
        return channel.getLatestRecord();
    }

    public final Record getRecord(long time) throws DataLoggerNotAvailableException, IOException {
        return channel.getLoggedRecord(time);
    }

    public final List<Record> getRecords(long startTime) throws DataLoggerNotAvailableException, IOException {
        return channel.getLoggedRecords(startTime);
    }

    public final List<Record> getRecords(long startTime, long endTime) throws DataLoggerNotAvailableException, IOException {
        return channel.getLoggedRecords(startTime, endTime);
    }

    public final void setRecord(Record record) {
        channel.setLatestRecord(record);
    }

    public final Flag write(Value value) {
        return channel.write(value);
    }

    public final void writeFuture(List<FutureValue> values) {
        channel.writeFuture(values);
    }

    @Override
    public String toString() {
        return getId()+" ("+getValueType().toString()+"): "+getRecord().toString();
    }

}
