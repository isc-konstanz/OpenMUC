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

package org.the.ems.env.hp;

import org.openmuc.framework.lib.osgi.config.GenericSettings;
import org.openmuc.framework.lib.osgi.config.ServiceProperty;

public class HeatPumpEnvironmentSettings extends GenericSettings {

    static final String HEATING_ROD_TEMP_SETPOINT = "heatingRodTempSetpoint";
    static final String HEATING_ROD_TEMP_SETPOINT_DEFAULT = "10";

    static final String HEATING_ROD_TEMP_HYSTERESIS = "heatingRodTempHysteresis";
    static final String HEATING_ROD_TEMP_HYSTERESIS_DEFAULT = "1";

    static final String HEATING_ROD_2PH_STATE = "heatingRod2PhStateChannel";
    static final String HEATING_ROD_2PH_STATE_DEFAULT = "hp_source_hr_2ph_state";

    static final String HEATING_ROD_3PH_STATE = "heatingRod3PhStateChannel";
    static final String HEATING_ROD_3PH_STATE_DEFAULT = "hp_source_hr_3ph_state";

    static final String HEAT_PUMP_TEMP_INLET = "heatPumpTempInlet";
    static final String HEAT_PUMP_TEMP_INLET_DEFAULT = "hp_source_temp_in";

    static final String HEAT_PUMP_TEMP_OUTLET = "heatPumpTempOutlet";
    static final String HEAT_PUMP_TEMP_OUTLET_DEFAULT = "hp_source_temp_out";

    static final String HEAT_PUMP_CIRC_PUMP_STATE = "heatPumpCircPumpState";
    static final String HEAT_PUMP_CIRC_PUMP_STATE_DEFAULT = "hp_source_pump_state";


    public HeatPumpEnvironmentSettings() {
        super();
        properties.put(HEATING_ROD_TEMP_SETPOINT, new ServiceProperty(HEATING_ROD_TEMP_SETPOINT, "Temperature setpoint for the heating rod", HEATING_ROD_TEMP_SETPOINT_DEFAULT, true));
        properties.put(HEATING_ROD_TEMP_HYSTERESIS, new ServiceProperty(HEATING_ROD_TEMP_HYSTERESIS, "Temperature hysteresis for the heating rod", HEATING_ROD_TEMP_HYSTERESIS_DEFAULT, true));

        properties.put(HEATING_ROD_2PH_STATE, new ServiceProperty(HEATING_ROD_2PH_STATE, "Channel ID for the 2 Phase mode state of the heating rod", HEATING_ROD_2PH_STATE_DEFAULT, true));
        properties.put(HEATING_ROD_3PH_STATE, new ServiceProperty(HEATING_ROD_3PH_STATE, "Channel ID for the 3 Phase mode state of the heating rod", HEATING_ROD_3PH_STATE_DEFAULT, true));

        properties.put(HEAT_PUMP_TEMP_INLET, new ServiceProperty(HEAT_PUMP_TEMP_INLET, "Channel ID for the source inlet temperature of the heat pump", HEAT_PUMP_TEMP_INLET_DEFAULT, true));
        properties.put(HEAT_PUMP_TEMP_OUTLET, new ServiceProperty(HEAT_PUMP_TEMP_OUTLET, "Channel ID for the source outlet temperature of the heat pump", HEAT_PUMP_TEMP_OUTLET_DEFAULT, true));
        properties.put(HEAT_PUMP_CIRC_PUMP_STATE, new ServiceProperty(HEAT_PUMP_CIRC_PUMP_STATE, "Channel ID for the source circulation pump state of the heat pump", HEAT_PUMP_CIRC_PUMP_STATE_DEFAULT, true));
    }

}
