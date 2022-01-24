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
package org.openmuc.framework.driver.aggregator;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;

public class ChannelRecordDeque extends ConcurrentLinkedDeque<Record> implements RecordListener {
    private static final long serialVersionUID = 1L;

    public static final class ChannelRecordDeques extends HashMap<String, ChannelRecordDeque> {
        private static final long serialVersionUID = 1L;

        public ChannelRecordDeque get(Channel channel) {
            return get(channel.getId());
        }

        public ChannelRecordDeque add(Channel channel, int timeWindow) {
            ChannelRecordDeque recordDeque = new ChannelRecordDeque(timeWindow);
            put(channel.getId(), recordDeque);
            
            channel.addListener(recordDeque);
            
            return recordDeque;
        }
    }

    private int timeWindow;

    public ChannelRecordDeque(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
        while (size() > 0 && getFirst().getTimestamp() < System.currentTimeMillis() - timeWindow) {
            removeFirst();
        }
    }

    public int getTimeWindow() {
        return this.timeWindow;
    }

    @Override
    public void newRecord(Record record) {
        // Check if the value is null or the flag isn't valid
        if (record != null && record.getValue() != null && record.getFlag().equals(Flag.VALID)) {
            addLast(record);
        }
        while (size() > 0 && getFirst().getTimestamp() < System.currentTimeMillis() - getTimeWindow()) {
            removeFirst();
        }
    }

}
