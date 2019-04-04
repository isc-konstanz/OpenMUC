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
package org.openmuc.framework.app.household.grid;

import org.openmuc.framework.app.household.HouseholdApp;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;

public class PowerListener implements RecordListener {

    /**
     * Interface used to notify the {@link HouseholdApp} 
     * implementation about changed power values
     */
    public interface PowerCallbacks {
        public void onPowerReceived(PowerType type, Record record);
    }

    /**
     * The Listeners' current callback object, which is notified of changed power values
     */
    protected final PowerCallbacks callbacks;

    protected final PowerType type;

    protected Record power = new Record(Flag.NO_VALUE_RECEIVED_YET);

    public PowerListener(PowerCallbacks callbacks, PowerType type) {
        this.callbacks = callbacks;
        this.type = type;
    }

    public PowerType getType() {
        return type;
    }

    public Flag getFlag() {
        return power.getFlag();
    }

    public Long getTimestamp() {
    	return power.getTimestamp();
    }

    public Double getPower() {
    	return power.getValue().asDouble();
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() == Flag.VALID && record.getValue() != null) {
        	power = record;
        	
            callbacks.onPowerReceived(type, record);
        }
    }

}
