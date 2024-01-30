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
package org.openmuc.framework.driver.clipper;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.util.ArrayList;
import java.util.Arrays;

import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.framework.driver.DriverChannel;
import org.openmuc.framework.driver.annotation.Listen;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClipperChannel extends DriverChannel implements RecordListener {

    private static final Logger logger = LoggerFactory.getLogger(ClipperChannel.class);

    public static final String ID = "channelID";

    @Option(id = ID,
    		type = ADDRESS,
            name = "Channel ID",
            description = "The ID of the source channel to clip."
    )
    private String channelId;

    private Channel channel;

    @Option(type = SETTING,
            name = "Mode",
            description = "The clipping mode to be applied.",
            valueSelection = "DROP:Drop,LIMIT:Limit",
            valueDefault = "DROP",
            mandatory = false
    )
    private ClippingMode mode = ClippingMode.DROP;

    @Option(type = SETTING,
            name = "Scope",
            description = "The clipping scope to be applied.",
            valueSelection = "GREATER_EQUALS:>=,GREATER:>,LESS_EQUALS:<=,LESS:<",
            valueDefault = "GREATER_EQUALS",
            mandatory = false
    )
    private ClippingScope scope = ClippingScope.GREATER_EQUALS;

    @Option(type = SETTING,
            name = "Limit",
            description = "Limit above which values will be clipped."
    )
    private double limit;

    @Option(type = SETTING,
            name = "Delta value",
            description = "The delta value, indicating the clipping of the value to be skipped. " +
                          "Values where the difference to the previous value was smaller " +
                          "than the specified value will not be clipped!",
            valueDefault = "0",
            mandatory = false
    )
    private double deltaValue = 0;

    @Option(type = SETTING,
            name = "Delta time",
            description = "The time delta in seconds, indicating the value to be clipped. Deltas smaller than specified will not be clipped!",
            mandatory = false
    )
    private Integer deltaTime = null;

    private RecordsReceivedListener listener;


    public ClipperChannel(Channel channel) {
        this.channel = channel;
    }

    @Listen
    public void startListening(RecordsReceivedListener listener) {
    	logger.debug("Start clipping channel \"{}\"", this.channelId);
        this.listener = listener;
        this.channel.addListener(this);
    }

    public void stopListening() {
    	logger.debug("Stop clipping channel \"{}\"", this.channelId);
        this.listener = null;
        this.channel.removeListener(this);
    }

    @Override
    public void newRecord(Record record) {
    	logger.trace("Record received: {}", record);
    	
        if (listener == null) {
            logger.warn("Record received but listener already stopped");
            return;
        }
        if (record.getFlag() != Flag.VALID) {
            logger.debug("Skipping to clip record with invalid flag: ", record.getFlag());
            return;
        }
        Value value = record.getValue();
        if (isBelowLimit(value, limit)) {
            notifyRecord(value, record.getTimestamp());
            return;
        }
        switch (mode) {
		case DROP:
	        Record latestRecord = getRecord();
	        if (latestRecord.getFlag() != Flag.VALID) {
	            logger.warn("Unable to verify received but listener already stopped");
	            return;
	        }
	        if (Math.abs(record.getValue().asDouble() - latestRecord.getValue().asDouble()) <= deltaValue) {
	            logger.warn("Skipping to clip record of channel \"{}\", as {} - {} <= {} ", channelId,
	            		record.getValue().asDouble(), latestRecord.getValue().asDouble(), deltaValue);
		        notifyRecord(value, record.getTimestamp());
	        }
	        else if (deltaTime != null && record.getTimestamp() - latestRecord.getTimestamp() >= deltaTime*1000) {
	            logger.warn("Skipping to clip record of channel \"{}\" after {} seconds", channelId, deltaTime);
		        notifyRecord(value, record.getTimestamp());
	        }
			break;
		case LIMIT:
            notifyRecord(new DoubleValue(limit), record.getTimestamp());
            break;
        }
    }

    public void notifyRecord(Value value, long timestamp) {
        this.setRecord(new Record(value, timestamp));
        if (listener != null) {
        	listener.newRecords(new ArrayList<>(Arrays.asList((ChannelRecordContainer) getTaskContainer())));
        }
    }

    private boolean isBelowLimit(Value value, double limit) {
        switch (scope) {
        case GREATER_EQUALS:
            if (value.asDouble() < limit) {
                return true;
            }
            break;
        case GREATER:
            if (value.asDouble() <= limit) {
                return true;
            }
            break;
        case LESS_EQUALS:
            if (value.asDouble() > limit) {
                return true;
            }
            break;
        case LESS:
            if (value.asDouble() >= limit) {
                return true;
            }
            break;
        }
        return false;
    }
}
