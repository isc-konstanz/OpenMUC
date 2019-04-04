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

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;

public class EnergyListener extends PowerListener {

    private Record last = new Record(Flag.NO_VALUE_RECEIVED_YET);

    public EnergyListener(PowerCallbacks callbacks, PowerType type) {
    	super(callbacks, type);
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() == Flag.VALID && record.getValue() != null) {
        	if (last.getFlag() == Flag.VALID && last.getValue() != null &&
        			record.getTimestamp() > last.getTimestamp()) {
        		
            	double energy = last.getValue().asDouble() - record.getValue().asDouble();
            	if (energy >= 0) {
            		Value value = new DoubleValue(energy/record.getTimestamp() - last.getTimestamp()/3600);
                    this.power = new Record(value, record.getTimestamp());
                    
                    callbacks.onPowerReceived(type, power);
            	}
        	}
        	last = record;
        }
    }

}
