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

package org.the.ems.env.hp.htr;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeatingRodController {

    private static final Logger logger = LoggerFactory.getLogger(HeatingRodController.class);
    // Temperature boundaries of the heat pump brine [°C]
    double maxTemp = 9;
    double minTemp = 5;

    private static final String ID_HEATINGROD_STATE = "hp_source_hr_state";
    private static final String ID_TEMP_HEATPUMP_INLET = "hp_source_temp_in";
    private static final String ID_TEMP_HEATPUMP_OUTLET = "hp_source_temp_out";
    private static final String ID_BRINE_PUMP = "hp_source_pump_state";

    private Channel state;
    private Channel heatPumpInletTemp;
    private Channel heatPumpOutletTemp;
    private Channel pumpState;

    public HeatingRodController(DataAccessService dataAccessService) {
        this.state = dataAccessService.getChannel(ID_HEATINGROD_STATE);
        this.heatPumpOutletTemp = dataAccessService.getChannel(ID_TEMP_HEATPUMP_OUTLET);
        this.heatPumpInletTemp = dataAccessService.getChannel(ID_TEMP_HEATPUMP_INLET);
        this.pumpState = dataAccessService.getChannel(ID_BRINE_PUMP);
        checkPump();
        
        this.heatPumpInletTemp.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source inlet channel invalid flag: {}", record.getFlag());
                return;
            }
            logger.trace("Heat pump source intlet temperature : {} °C", String.format("%.1f", record.getValue().asDouble()));

            if (record.getValue().asDouble() >= maxTemp) {
            	if (state.getLatestRecord().getFlag() != Flag.VALID) {
                    logger.warn("Source pump channel invalid flag: {}", state.getLatestRecord().getFlag());
                    return;
                }
            	if (state.getLatestRecord().getValue().asBoolean()) {
            		logger.info("Heat pump source inlet temperatur {} > 20°C. Switching heating rod off", String.format("%.1f", record.getValue().asDouble()));
                    writeState(false);
            	} 
            }
        });

        this.heatPumpOutletTemp.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source outlet channel invalid flag!");
                return;
            }
            logger.trace("Heat pump source outlet temperature : {} °C", record.getValue().asDouble());

            if (pumpState.getLatestRecord().getFlag()!= Flag.VALID) {
                logger.warn("Skip switching heating rod. Source pump invalid flag: {}", pumpState.getLatestRecord().getFlag());
                return;
            }
            if (!pumpState.getLatestRecord().getValue().asBoolean()) {
                logger.debug("Skip switching heating rod off. Source pump is not running.");
                return;
            }
            if (record.getValue().asDouble() <= minTemp) {
            	if (state.getLatestRecord().getFlag() != Flag.VALID) {
                    logger.warn("Source pump channel invalid flag: {}", state.getLatestRecord().getFlag());
                    return;
                }
            	if (!state.getLatestRecord().getValue().asBoolean()) {
            		logger.info("Heat pump source inlet temperatur {} < 10°C. Switching heating rod on", String.format("%.1f", record.getValue().asDouble()));
                    writeState(true);
            	} 
            }
        });

        this.pumpState.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source pump channel invalid flag: {}", record.getFlag());
                return;
            }
            if (state.getLatestRecord().getFlag() != Flag.VALID) {
                logger.warn("Heating rod channel invalid flag: {}", record.getFlag());
                return;
            }
            if (!record.getValue().asBoolean() && state.getLatestRecord().getValue().asBoolean()) {
                logger.info("Heat pump source pump is not running, heating rod will be switched off.");
                writeState(false);
            }
        });
    }

    public void writeState(boolean enable) {
        if (state.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Heating rod Channel state flag invalid: {}", state.getLatestRecord().getFlag());
            return;
        }
        if (state.getLatestRecord().getValue().asBoolean() == enable) {
            return;
        }
        state.write(new BooleanValue(enable));
    }

    private void checkPump() {
        if (pumpState.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Skip switching heating rod. Source pump invalid flag: {}", pumpState.getLatestRecord().getFlag());
            return;
        }
        if (!pumpState.getLatestRecord().getValue().asBoolean()) {
            writeState(false);
        }
    }
}
