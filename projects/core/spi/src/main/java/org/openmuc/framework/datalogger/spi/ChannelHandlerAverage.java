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

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ChannelHandlerAverage<C extends Channel> extends ChannelHandlerDynamic<C> implements RecordListener {
	private final static Logger logger = LoggerFactory.getLogger(ChannelHandlerAverage.class);

	volatile Long lastTime = null;
	volatile Double valueSum = 0.0;
	volatile int valueCount = 0;

	Boolean listening = false;

    ChannelHandlerAverage(C channel) {
       super(channel);
    }

	boolean isListening() {
		return listening;
	}

	void setListening(boolean listening) {
		this.listening = listening;
	}

	@Override
	boolean update(Record update) {
		if (isUpdate(update)) {
			synchronized (listening) {
				if (valueCount > 1) {
					double average = valueSum/valueCount;
					logger.trace("Average of {} values for channel \"{}\": {}", valueCount, channel.getId(), average);
					
					valueSum = 0.0;
					valueCount = 0;
					channel.record = new Record(new DoubleValue(average), update.getTimestamp());
					return true;
				}
			}
			channel.record = update;
			return true;
		}
		return false;
	}

	@Override
	public void newRecord(Record record) {
		if (record == null) {
			logger.trace("Failed to log an empty record for channel \"{}\"", channel.getId());
			return;
		}
		if (record.getFlag() != Flag.VALID) {
			logger.debug("Listener received invalid or empty value for channel \"{}\": {}",
					channel.getId(), record.getFlag().toString());
		}
		logger.trace("Listener received new record for channel \"{}\": {}", 
				channel.getId(), record.toString());
		
		Long time = record.getTimestamp();
		Value value = record.getValue();
		if (value != null && time != null && (this.lastTime == null || this.lastTime < time)) {
			synchronized (listening) {
				valueSum += value.asDouble();
				valueCount++;
			}
			lastTime = time;
		}
	}
}
