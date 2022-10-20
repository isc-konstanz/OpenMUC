/*
 * Copyright 2011-2022 Fraunhofer ISE
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
import org.openmuc.framework.data.FutureValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DataLoggerNotAvailableException;
import org.openmuc.framework.dataaccess.DeviceState;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.framework.server.spi.ServerMappingContainer;

public class ServerChannel extends ChannelContainerWrapper {

    ServerChannelContext context;

    protected ServerChannel() {
    }

    void invokeConfigure(ServerChannelContext context, ServerMappingContainer container) 
            throws ArgumentSyntaxException {
        
        this.context = context;
        super.invokeConfigure(context, container);
    }

    public final ServerChannelContext getContext() {
        return context;
    }

    private final Channel getChannel() {
        return container.getChannel();
    }

    public final String getId() {
        return getChannel().getId();
    }

    public final String getDescription() {
        return getChannel().getDescription();
    }

    public final String getUnit() {
        return getChannel().getUnit();
    }

    public final ValueType getValueType() {
        return getChannel().getValueType();
    }

    public final int getValueTypeLength() {
        return getChannel().getValueTypeLength();
    }

    public final String getAddress() {
        return getChannel().getAddress();
    }

    public final String getSettings() {
        return getChannel().getSettings();
    }

    public final double getScalingFactor() {
        return getChannel().getScalingFactor();
    }

    public final int getSamplingInterval() {
        return getChannel().getSamplingInterval();
    }

    public final int getSamplingTimeOffset() {
        return getChannel().getSamplingTimeOffset();
    }

    public final int getLoggingInterval() {
        return getChannel().getLoggingInterval();
    }

    public final int getLoggingTimeOffset() {
        return getChannel().getLoggingTimeOffset();
    }

    public final String getLoggingSettings() {
        return getChannel().getLoggingSettings();
    }

    public final DeviceState getDeviceState() {
        return getChannel().getDeviceState();
    }

    public final ChannelState getState() {
        return getChannel().getChannelState();
    }

    public final boolean isConnected() {
        return getChannel().isConnected();
    }

    public final void addListener(RecordListener listener) {
        getChannel().addListener(listener);
    }

    public final void removeListener(RecordListener listener) {
        getChannel().removeListener(listener);
    }

    public final Record read() {
        return getChannel().read();
    }

    public final Record getRecord() {
        return getChannel().getLatestRecord();
    }

    public final Record getRecord(long time) throws DataLoggerNotAvailableException, IOException {
        return getChannel().getLoggedRecord(time);
    }

    public final List<Record> getRecords(long startTime) throws DataLoggerNotAvailableException, IOException {
        return getChannel().getLoggedRecords(startTime);
    }

    public final List<Record> getRecords(long startTime, long endTime) throws DataLoggerNotAvailableException, IOException {
        return getChannel().getLoggedRecords(startTime, endTime);
    }

    public final void setRecord(Record record) {
        getChannel().setLatestRecord(record);
    }

    public final void write(List<FutureValue> values) {
        getChannel().writeFuture(values);
    }

    @Override
    public String toString() {
        return getId()+" ("+getValueType().toString()+"): "+getRecord().toString();
    }

}
