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
package org.openmuc.framework.app.gridservice.power;

import org.openmuc.framework.app.gridservice.GridServiceApp;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;

public class PowerListener implements RecordListener {

    /**
     * Interface used to notify the {@link GridServiceApp} 
     * implementation about changed power values
     */
    public interface PowerCallbacks {
        public void onPowerReceived(String id, Record value);
    }

    /**
     * The Listeners' current callback object, which is notified of changed power values
     */
    private final PowerCallbacks callbacks;

    private final Channel channel;

    public PowerListener(PowerCallbacks callbacks, Channel channel) {
        this.callbacks = callbacks;
        this.channel = channel;
    }

    public String getId() {
        return channel.getId();
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() == Flag.VALID && record.getValue() != null) {
            callbacks.onPowerReceived(channel.getId(), record);
        }
    }
}
