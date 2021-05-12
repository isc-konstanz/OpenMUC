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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingChannel extends ChannelWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LoggingChannel.class);

    LoggingChannelContext context;

    Record record = new Record(Flag.DATA_LOGGING_NOT_ACTIVE);

    final List<Record> records = new ArrayList<Record>();

    final void doCreate(LoggingChannelContext context) throws ArgumentSyntaxException {
        this.context = context;
        this.onCreate(context);
        this.onCreate();
    }

    protected void onCreate(LoggingChannelContext context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onDestroy() {
        // Placeholder for the optional implementation
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
        if (intervalMax < 0) {
            throw new ArgumentSyntaxException("Invalid maximum logging interval for channel: " + getId());
        }
        else {
            this.intervalMax = Math.max(getLoggingInterval(), intervalMax);
        }
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
    }

    final List<Record> doRead(long startTime, long endTime) throws IOException {
        return onRead(startTime, endTime);
    }

    protected List<Record> onRead(long startTime, long endTime) throws IOException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    final void doWrite(long timestamp) throws IOException {
        onWrite(getRecord(), timestamp);
    }

    protected void onWrite(Record record, long timestamp) throws IOException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    boolean isUpdate(Record record) {
    	if (Flag.VALID != record.getFlag()) {
            logger.trace("Skipped logging value for unchanged flag: {}", record.getFlag());
            return false;
        }
        if (getRecord() == null) {
            return true;
        }
        if (getRecord().getFlag() != record.getFlag()) {
            return true;
        }
        if (getRecord().getTimestamp() >= record.getTimestamp()) {
            logger.trace("Skipped logging value with invalid timestamp: {}", record.getTimestamp());
            return false;
        }
        if (isLoggingDynamic()) {
            switch(channel.getValueType()) {
            case INTEGER:
            case SHORT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                double delta = Math.abs(record.getValue().asDouble() - getRecord().getValue().asDouble());
                if (getLoggingTolerance() >= delta && 
                        (record.getTimestamp() - getRecord().getTimestamp()) < getLoggingIntervalMax()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Skipped logging value inside tolerance: {} -> {} <= {}",
                                getRecord().getValue().asDouble(), record.getValue(), getLoggingTolerance());
                    }
                    return false;
                }
            default:
                break;
            }
        }
        return true;
    }

    void updateRecord(Record record) {
    	if (isAveraging()) {
    		double average = records.stream().mapToDouble(c -> c.getValue().asDouble())
    	            .average().getAsDouble();
    		
    		long timestamp = record.getTimestamp();
    		switch (getValueType()) {
			case SHORT:
				record = new Record(new ShortValue((short) Math.round(average)), timestamp);
				break;
			case INTEGER:
				record = new Record(new IntValue((int) Math.round(average)), timestamp);
				break;
			case LONG:
				record = new Record(new LongValue((long) Math.round(average)), timestamp);
				break;
			case FLOAT:
				record = new Record(new FloatValue((float) average), timestamp);
				break;
			case DOUBLE:
				record = new Record(new DoubleValue(average), timestamp);
				break;
			default:
				break;
    		}
            logger.trace("Average of {} values for channel \"{}\": {}", records.size(), channel.getId(), average);
            records.clear();
    	}
    	this.record = record;
    }

    boolean update(Record record) {
    	if (isAveraging()) {
    		records.add(record);
    	}
        if (isUpdate(record)) {
            updateRecord(record);
            return true;
        }
        return false;
    }

    public final Record getRecord() {
        return record;
    }

    public boolean isValid() {
        if (record != null && record.getFlag() == Flag.VALID && record.getValue() != null) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getId()+" ("+getValueType().toString()+"): "+getRecord().toString();
    }

}
