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
package org.openmuc.framework.datalogger.spi;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.options.Setting;

public class Channel extends ChannelContext implements LogChannel, LogRecordContainer {

    protected String id;
    protected String desc;
    protected String unit;
    protected ValueType type;
    protected Integer typeLength;

    @Setting(id={"intervalMax", "loggingMaxInterval"}, mandatory = false)
    protected int intervalMax = 0;
    protected int interval;
    protected int timeOffset;

    @Setting(id= {"tolerance", "loggingTolerance"}, mandatory = false)
    protected double tolerance = 0;

    @Setting(mandatory = false)
    protected boolean average = false;

    private String settings = null;

    Record record = new Record(Flag.DATA_LOGGING_NOT_ACTIVE);

	protected Channel() {
    }

    protected Channel(LogChannel configs) throws ArgumentSyntaxException {
        doConfigure(configs);
    }

    void doConfigure(LogChannel configs) throws ArgumentSyntaxException {
        this.id = configs.getId();
        this.desc = configs.getDescription();
        this.unit = configs.getUnit();
        this.type = configs.getValueType();
        this.typeLength = configs.getValueTypeLength();
        
        if (this.settings == null || !settings.equals(configs.getLoggingSettings())) {
            configureSettings(configs.getLoggingSettings());
        }
        if (intervalMax < 0) {
            throw new ArgumentSyntaxException("Invalid maximum logging interval for channel: " + id);
        }
        else {
            this.intervalMax = Math.max(interval, intervalMax);
        }
        this.interval = configs.getLoggingInterval();
        this.timeOffset = configs.getLoggingTimeOffset();
        
        this.settings = configs.getLoggingSettings();
        onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public final ChannelContext getContext() {
        return this;
    }

	@Override
	@Deprecated
	public String getChannelId() {
		return id;
	}

    public final String getId() {
        return id;
    }

    public final String getDescription() {
        return desc;
    }

    public final String getUnit() {
        return unit;
    }

    public final ValueType getValueType() {
        return type;
    }

    @Override
    public Integer getValueTypeLength() {
        return typeLength;
    }

    @Override
    public Integer getLoggingInterval() {
        return interval;
    }

    public Integer getLoggingIntervalMax() {
    	return intervalMax;
    }

    @Override
    public Integer getLoggingTimeOffset() {
        return timeOffset;
    }

    public double getLoggingTolerance() {
        return tolerance;
    }

    @Override
    @Deprecated
    public String getLoggingSettings() {
        return settings;
    }

    public boolean isAveraging() {
    	return average;
    }

    public Value getValue() {
        if (record == null) {
            return null;
        }
        return record.getValue();
    }

    public Long getTime() {
        if (record == null) {
            return null;
        }
        return record.getTimestamp();
    }

    public Flag getFlag() {
        if (record == null) {
            return null;
        }
        return record.getFlag();
    }

	@Override
    public Record getRecord() {
        return record;
    }

    final List<Record> doRead(long startTime, long endTime) throws IOException {
        return onRead(startTime, endTime);
    }

    protected List<Record> onRead(long startTime, long endTime) throws IOException {
        // Placeholder for the optional implementation
    	throw new UnsupportedOperationException();
    }

    final void doWrite(long timestamp) throws IOException {
        onWrite(record, timestamp);
    }

    protected void onWrite(Record record, long timestamp) throws IOException {
        // Placeholder for the optional implementation
    	throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return id+" ("+type.toString()+"): "+record.toString();
    }

}
