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
package org.the.ems.env.hh;

import java.util.ArrayDeque;
import java.util.Deque;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;


public class RecordAverageListener implements RecordListener {

    private final int interval;

    private final Deque<Record> records = new ArrayDeque<Record>();

    private long lastTimestamp;

    public RecordAverageListener(int interval) {
        this.interval = interval;
    }

    public double getMean() {
    	if (!records.isEmpty()) {
    		lastTimestamp = records.getLast().getTimestamp();
    		while(records.getFirst().getTimestamp() < lastTimestamp - interval) {
                records.removeFirst();
            }
    	}        
        return records.stream().mapToDouble(r -> r.getValue().asDouble()).average().orElse(0);
    }

    public double getLatestDouble() {
    	if (records.isEmpty()) {
    		return 0;
    	}
    	return records.getLast().getValue().asDouble();
    }

    public boolean getLatestState() {
    	if (records.isEmpty()) {
    		return false;
    	}
    	return records.getLast().getValue().asBoolean();
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() != Flag.VALID) {
            return;
        }
        records.add(record);
    }

}
