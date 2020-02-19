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

class ChannelHandler<C extends Channel> {
    private final static Logger logger = LoggerFactory.getLogger(ChannelHandler.class);

    final C channel;

    ChannelHandler(C channel) {
        this.channel = channel;
    }

    public String getChannelId() {
        return channel.getId();
    }

    public C getChannel() {
        return channel;
    }

    boolean isUpdate(Record update) {
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
        return true;
    }

    boolean update(Record update) {
        if (isUpdate(update)) {
            channel.record = update;
            return true;
        }
        return false;
    }

}
