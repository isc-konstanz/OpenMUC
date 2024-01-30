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
 */

package org.the.ems.env.hp.hr;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hp.HeatPumpEnvironmentProperties;

public class HeatingRodController {
    private static final Logger logger = LoggerFactory.getLogger(HeatingRodController.class);

    private final double tempSetpoint;
    private final double tempHysteresis;

    private final Channel stateLow;
    private final Channel stateHigh;

    private final Channel sourceTempInletTemp;
    private final Channel sourceTempOutletTemp;
    private final Channel sourcePumpState;

    private final SourceTempListener sourceTempListener;
    private final SourcePumpStateListener sourcePumpStateListener;

    public HeatingRodController(HeatPumpEnvironmentProperties properties) {
        logger.info("Activating TH-E Environment: Heating Rod Controller");

        tempSetpoint = properties.getHeatingRodTemperatureSetpoint();
        tempHysteresis = properties.getHeatingRodTemperatureHysteresis();

        stateLow = properties.getHeatingRodLowStateChannel();
        stateHigh = properties.getHeatingRodHighStateChannel();

        sourceTempInletTemp = properties.getHeatPumpInletTempChannel();
        sourceTempOutletTemp = properties.getHeatPumpOutletTempChannel();
        sourcePumpState = properties.getHeatPumpSourcePumpStateChannel();

        verifyPump();

        sourceTempListener = new SourceTempListener();
        sourceTempInletTemp.addListener(sourceTempListener);

        sourcePumpStateListener = new SourcePumpStateListener();
        sourcePumpState.addListener(sourcePumpStateListener);
    }

    public void shutdown() {
        logger.info("Deactivating TH-E Environment: Heating Rod Controller");
        
    	sourceTempInletTemp.removeListener(sourceTempListener);
        sourcePumpState.removeListener(sourcePumpStateListener);
    }

    private void writeState(Channel state, boolean enable) {
        if (state.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Heating rod channel state flag invalid: {}", state.getLatestRecord().getFlag());
            return;
        }
        if (state.getLatestRecord().getValue().asBoolean() == enable) {
            return;
        }
        state.write(new BooleanValue(enable));
    }

    private boolean checkState(Channel state) {
        if (state.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Heating rod state channel \"{}\" invalid flag: {}", state.getId(), state.getLatestRecord().getFlag());
            return false;
        }
        return state.getLatestRecord().getValue().asBoolean();
    }

    private void verifyPump() {
        if (sourcePumpState.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Skip switching heating rod. Source pump invalid flag: {}", sourcePumpState.getLatestRecord().getFlag());
            return;
        }
        if (!sourcePumpState.getLatestRecord().getValue().asBoolean()) {
            writeState(stateLow, false);
            writeState(stateHigh, false);
        }
    }

    private class SourceTempListener implements RecordListener {

        @Override
        public void newRecord(Record record) {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source inlet channel invalid flag: {}", record.getFlag());
                return;
            }
            logger.trace("Heat pump source intlet temperature : {} Â°C", String.format("%.1f", record.getValue().asDouble()));
            
            
            // Check if heating rod needs to be switched off
            if (record.getValue().asDouble() > tempSetpoint + tempHysteresis && (checkState(stateLow) || checkState(stateHigh))) {
                logger.info("Heat pump source inlet temperatur {} > {}°C. Switching heating rod off", 
                        String.format("%.1f", record.getValue().asDouble()), String.format("%.1f", tempSetpoint + tempHysteresis));

                writeState(stateLow, false);
                writeState(stateHigh, false);
            }
            if (record.getValue().asDouble() > tempSetpoint && checkState(stateHigh)) {
                logger.info("Heat pump source inlet temperatur {} > {}°C. Switching heating rod 3. phase off", 
                        String.format("%.1f", record.getValue().asDouble()), String.format("%.1f", tempSetpoint));
                writeState(stateHigh, false);
            }
            
            
            // Check if heating rod needs to be switched on
            if (sourcePumpState.getLatestRecord().getFlag()!= Flag.VALID) {
                logger.warn("Skip switching heating rod. Source pump invalid flag: {}", sourcePumpState.getLatestRecord().getFlag());
                return;
            }
            if (!sourcePumpState.getLatestRecord().getValue().asBoolean()) {
                logger.debug("Skip switching heating rod off. Source pump is not running.");
                return;
            }
            if (record.getValue().asDouble() <= tempSetpoint && !checkState(stateLow)) {
                logger.info("Heat pump source inlet temperatur {} < {}°C. Switching heating rod on", 
                        String.format("%.1f", record.getValue().asDouble(), String.format("%.1f", tempSetpoint)));
                writeState(stateLow, true);
            }
            if (record.getValue().asDouble() <= tempSetpoint - tempHysteresis && !checkState(stateHigh)) {
                logger.info("Heat pump source inlet temperatur {} < {}°C. Switching heating rod 3. phase on", 
                        String.format("%.1f", record.getValue().asDouble()), String.format("%.1f", tempSetpoint - tempHysteresis));
                writeState(stateHigh, true);
            }
        }
    }

    private class SourcePumpStateListener implements RecordListener {

        @Override
        public void newRecord(Record record) {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source pump channel invalid flag: {}", record.getFlag());
                return;
            }
            if (!record.getValue().asBoolean() && (checkState(stateLow) || checkState(stateHigh))) {
                logger.info("Heat pump source pump is not running, heating rod will be switched off.");
                writeState(stateLow, false);
                writeState(stateHigh, false);
            }
        }
    }
}
