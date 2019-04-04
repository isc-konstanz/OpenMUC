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
package org.openmuc.framework.app.household;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.app.household.grid.EnergyListener;
import org.openmuc.framework.app.household.grid.PowerListener;
import org.openmuc.framework.app.household.grid.PowerListener.PowerCallbacks;
import org.openmuc.framework.app.household.grid.PowerType;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {})
public final class HouseholdApp implements PowerCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(HouseholdApp.class);

    @Reference
    private DataAccessService dataAccessService;

    private Map<PowerType, PowerListener> powers = new HashMap<PowerType, PowerListener>();

    private Channel grid;
    private Channel cons;

    @Activate
    protected void activate() {
        logger.info("Activating Household App");
        try {
            HouseholdConfig config = new HouseholdConfig();
            
            // TODO: Implement properties and verify PV availability
            initPowerListener(config, PowerType.SOLAR);
            
            initPowerListener(config, PowerType.GRID_EXPORT);
            initPowerListener(config, PowerType.GRID_IMPORT);
            
            grid = getChannel(config.get(PowerType.GRID));
            cons = getChannel(config.get(PowerType.CONSUMPTION));
            
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Error while applying household configuration: {}", e.getMessage());
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.info("Deactivating Household App");
    }

    @Override
    public void onPowerReceived(PowerType type, Record record) {
    	switch(type) {
		case GRID_EXPORT:
		case GRID_IMPORT:
			break;
		default:
			return;
    	}
        if (powers.get(PowerType.GRID_EXPORT).getTimestamp() != null && 
        		powers.get(PowerType.GRID_EXPORT).getTimestamp().equals(powers.get(PowerType.GRID_IMPORT).getTimestamp())) {
        	
            long time = record.getTimestamp();
            if (grid.getLatestRecord().getTimestamp() != null && grid.getLatestRecord().getTimestamp() > time) {
            	return;
            }
            double grid = powers.get(PowerType.GRID_IMPORT).getPower() - powers.get(PowerType.GRID_EXPORT).getPower();
            this.grid.setLatestRecord(new Record(new DoubleValue(grid), time, Flag.VALID));
            
            if (powers.get(PowerType.SOLAR).getFlag() != Flag.VALID) {
            	return;
            }
        	double solar = powers.get(PowerType.SOLAR).getPower();
            
            // TODO: Implement property to verify self-consumption
            double consumption = Math.abs(grid + solar);
            cons.setLatestRecord(new Record(new DoubleValue(consumption), time, Flag.VALID));
        }
    }

    private PowerListener initPowerListener(HouseholdConfig config, PowerType type) throws IllegalArgumentException {
    	PowerListener listener;
    	if (config.hasPower(type)) {
    		listener = new PowerListener(this, type);
            getChannel(config.getPower(type)).addListener(listener);
    	}
    	else {
    		listener = new EnergyListener(this, type);
            getChannel(config.get(type)).addListener(listener);
    	}
        powers.put(type, listener);
        return listener;
    }

    private Channel getChannel(String id) throws IllegalArgumentException {
        Channel channel = dataAccessService.getChannel(id);
        if (channel == null) {
            throw new IllegalArgumentException("Unable to find Channel for id: " + id);
        }
        return channel;
    }

}
