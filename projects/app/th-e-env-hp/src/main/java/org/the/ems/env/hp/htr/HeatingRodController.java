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

public class HeatingRodController {

    // Temperature boundaries of the heat pump brine [°C]
    double maxTemp = 15;
    double minTemp = 5;

    private static final String ID_HEATINGROD_STATE = "hr_brine_state";
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
                return;
            }
            if (record.getValue().asDouble() >= maxTemp) {
                writeState(false);
            }
        });
        
        this.heatPumpOutletTemp.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                return;
            }
            if (pumpState.getLatestRecord().getFlag()!= Flag.VALID ||
                    !pumpState.getLatestRecord().getValue().asBoolean()) {
                return;
            }
            if (record.getValue().asDouble() <= minTemp) {
                writeState(true);
            }
        });
        
        this.pumpState.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                return;
            }
            if (!record.getValue().asBoolean()) {
                writeState(false);
            }
        });
    }

    public void writeState(boolean enable) {
        if (state.getLatestRecord().getFlag() != Flag.VALID ||
        		state.getLatestRecord().getValue().asBoolean() == enable) {
            return;
        }
        state.write(new BooleanValue(enable));
    }

    private void checkPump() {
        if (pumpState.getLatestRecord().getFlag() != Flag.VALID) {
            return;
        }
        if (!pumpState.getLatestRecord().getValue().asBoolean()) {
            writeState(false);
        }
    }

}
