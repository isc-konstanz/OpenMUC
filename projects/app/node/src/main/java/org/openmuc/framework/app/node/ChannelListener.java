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
package org.openmuc.framework.app.node;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;

public class ChannelListener implements RecordListener {

    private static final int INTERVAL = 900000;

    /**
     * Interface used to notify the {@link NodeApp} 
     * implementation about changed values
     */
    public interface NodeCallbacks {
        public void onNodeUpdate();
    }

    /**
     * The Listeners' current callback object, which is notified of changed values
     */
    private final NodeCallbacks callbacks;

    private final Channel channel;
    private final double scale;

    private Record record;

    public ChannelListener(NodeCallbacks callbacks, Channel channel, double scale) {
        this.callbacks = callbacks;
        this.channel = channel;
        this.scale = scale;
        this.record = new Record(new DoubleValue(0), System.currentTimeMillis());
    }

    public String getId() {
        return channel.getId();
    }

    public double getValue() {
    	if (System.currentTimeMillis() - record.getTimestamp() > INTERVAL) {
    		return 0;
    	}
    	return record.getValue().asDouble()*scale;
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() != Flag.VALID || record.getValue() == null) {
        	return;
        }
    	this.record = record;
    	
        callbacks.onNodeUpdate();
    }

}
