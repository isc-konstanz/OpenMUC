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
package org.openmuc.framework.datalogger;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ChannelHandlerDynamic<C extends Channel> extends ChannelHandler<C> {
	private final static Logger logger = LoggerFactory.getLogger(ChannelHandlerDynamic.class);

    ChannelHandlerDynamic(C channel) {
       super(channel);
    }

	@Override
	public boolean isUpdate(Record update) {
		if (channel.record == null) {
			return true;
		}
		if (channel.record.getFlag() != update.getFlag()) {
			return true;
		}
		else if (Flag.VALID != update.getFlag()) {
			logger.trace("Skipped logging value for unchanged flag: {}", update.getFlag());
			return false;
		}
		if (channel.record.getTimestamp() >= update.getTimestamp()) {
			logger.trace("Skipped logging value with invalid timestamp: {}", update.getTimestamp());
			return false;
		}
		else {
			switch(channel.getValueType()) {
			case INTEGER:
			case SHORT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				double delta = Math.abs(update.getValue().asDouble() - channel.record.getValue().asDouble());
				if (channel.getLoggingTolerance() >= delta && 
						(update.getTimestamp() - channel.record.getTimestamp()) < channel.getLoggingIntervalMax()) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipped logging value inside tolerance: {} -> {} <= {}",
								channel.record.getValue().asDouble(), update.getValue(), channel.getLoggingTolerance());
					}
					return false;
				}
			default:
				break;
			}
		}
		return true;
	}
}
