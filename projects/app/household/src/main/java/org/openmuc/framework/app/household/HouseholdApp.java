/*
 * Copyright 2011-16 Fraunhofer ISE
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

import org.openmuc.framework.app.household.grid.PowerListener;
import org.openmuc.framework.app.household.grid.PowerListener.PowerCallbacks;
import org.openmuc.framework.app.household.grid.PowerType;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {})
public final class HouseholdApp implements PowerCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(HouseholdApp.class);

    private DataAccessService dataAccessService;

    private Channel pv;
    private Channel grid;
    private Channel consumption;

    private Double consumptionLast;
    private Record gridExportLast;
    private Record gridImportLast;

    @Activate
    protected void activate(ComponentContext context) {
        logger.info("Activating Household App");
        try {
            HouseholdConfig config = new HouseholdConfig();
            
            // TODO: Implement properties and verify PV availability
            pv = initializeChannel(config.getPvEnergy());
            grid = initializeChannel(config.getGridEnergy());
            consumption = initializeChannel(config.getConsumptionEnergy());
            consumptionLast = Double.NaN;
            
            gridExportLast = new Record(Flag.NO_VALUE_RECEIVED_YET);
            applyPowerListener(config.getGridExportEnergy(), PowerType.EXPORT);
            
            gridImportLast = new Record(Flag.NO_VALUE_RECEIVED_YET);
            applyPowerListener(config.getGridImportEnergy(), PowerType.IMPORT);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error while applying household configuration: {}", e.getMessage());
        }
    }

    protected Channel initializeChannel(String id) throws IllegalArgumentException {
        Channel channel = dataAccessService.getChannel(id);
        if (pv == null) {
            throw new IllegalArgumentException("Unable to find Channel for id: " + id);
        }
        return channel;
    }

    protected void applyPowerListener(String id, PowerType type) throws IllegalArgumentException {
        Channel channel = initializeChannel(id);
        channel.addListener(new PowerListener(this, type));
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        logger.info("Deactivating Household App");
    }

    @Reference
    protected void bindDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    protected void unbindDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = null;
    }

    @Override
    public void onPowerReceived(PowerType type, Record record) {
        switch(type) {
        case EXPORT:
            gridExportLast = record;
            break;
        case IMPORT:
            gridImportLast = record;
            break;
        }
        if (gridImportLast.getTimestamp() != null && gridImportLast.getTimestamp().equals(gridExportLast.getTimestamp())) {
            long timestamp = gridExportLast.getTimestamp();
            
            double gridEnergy = gridImportLast.getValue().asDouble() - gridExportLast.getValue().asDouble();
            Record gridRecord = new Record(new DoubleValue(gridEnergy), timestamp, Flag.VALID);
            grid.setLatestRecord(gridRecord);
            
            double pvEnergy = 0;
            Record pvRecord = pv.getLatestRecord();
            if (pvRecord.getFlag() == Flag.VALID && pvRecord.getValue() != null) {
                pvEnergy = pvRecord.getValue().asDouble();
            }

            // TODO: Implement property to verify self-consumption
            double consumptionEnergy = gridEnergy + pvEnergy;
            if (!consumptionLast.equals(Double.NaN) && consumptionLast <= consumptionEnergy) {
                Record consumptionRecord = new Record(new DoubleValue(consumptionEnergy), timestamp, Flag.VALID);
                consumption.setLatestRecord(consumptionRecord);
            }
            consumptionLast = consumptionEnergy;
        }
    }
}
