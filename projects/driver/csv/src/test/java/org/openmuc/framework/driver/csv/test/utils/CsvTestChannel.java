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
package org.openmuc.framework.driver.csv.test.utils;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FutureValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DataLoggerNotAvailableException;
import org.openmuc.framework.dataaccess.DeviceState;
import org.openmuc.framework.dataaccess.ReadRecordContainer;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.framework.dataaccess.WriteValueContainer;

public class CsvTestChannel implements Channel {

    private final String address;

    CsvTestChannel(String address) {
        this.address = address;
    }

    @Override
    public String getId() {
        return address;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public String getSettings() {
        return "";
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueType getValueType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getValueTypeLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getValueOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getScalingFactor() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isListening() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getSamplingInterval() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSamplingTimeOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSamplingTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLoggingInterval() {
        // TODO Auto-generated method stub
        return 0;
    }

	@Override
	public int getLoggingDelayMaximum() {
        // TODO Auto-generated method stub
		return 0;
	}

    @Override
    public int getLoggingTimeOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getLoggingSettings() {
        // TODO Auto-generated method stub
        return "";
    }

	@Override
	public double getLoggingTolerance() {
        // TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public boolean isloggingAverage() {
        // TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean isLoggingEvent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDriverId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDeviceId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDeviceDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDeviceAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDeviceSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChannelState getChannelState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DeviceState getDeviceState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addListener(RecordListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeListener(RecordListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Record getLatestRecord() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLatestRecord(Record record) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Flag write(Value value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeFuture(List<FutureValue> values) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public WriteValueContainer getWriteContainer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Record read() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReadRecordContainer getReadContainer() {
        return new CsvTestContainer(this);
    }

    @Override
    public Record getLoggedRecord(long time) throws DataLoggerNotAvailableException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Record> getLoggedRecords(long startTime) throws DataLoggerNotAvailableException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Record> getLoggedRecords(long startTime, long endTime)
            throws DataLoggerNotAvailableException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
