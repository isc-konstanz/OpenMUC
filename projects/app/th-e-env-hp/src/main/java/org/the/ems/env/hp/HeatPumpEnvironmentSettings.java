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

	static final String PWM_PERIOD = "pwmPeriod";
	static final String PWM_DUTY_CYCLE_MIN = "pwmDutyCycleMin";
	static final String PWM_DUTY_CYCLE_MAX = "pwmDutyCycleMax";

    static final String HEATING_ROD_TEMP_SETPOINT = "heatingRodTempSetpoint";
    static final String HEATING_ROD_TEMP_HYSTERESIS = "heatingRodTempHysteresis";
    static final String HEATING_ROD_STATE = "heatingRodStateChannel";
    static final String HEATING_ROD_PWM = "heatingRodPwmChannel";
    static final String HEATING_ROD_PWM_SETPOINT_DEFAULT = "heatingRodPwmSetpointDefault";

    static final String HEAT_PUMP_TEMP_INLET = "heatPumpTempInlet";
    static final String HEAT_PUMP_TEMP_OUTLET = "heatPumpTempOutlet";
    static final String HEAT_PUMP_SOURCE_PUMP_STATE = "heatPumpSourcePumpState";


    public HeatPumpEnvironmentSettings() {
        super();
        properties.put(PWM_PERIOD, new ServiceProperty(
        		PWM_PERIOD, "PWM interval or period in seconds", "20", false)
        );
        properties.put(PWM_DUTY_CYCLE_MIN, new ServiceProperty(
        		PWM_DUTY_CYCLE_MIN, "Minium PWM duty cycle in %", "10", false)
        );
        properties.put(PWM_DUTY_CYCLE_MAX, new ServiceProperty(
        		PWM_DUTY_CYCLE_MAX, "Maximum PWM duty cycle in %", "90", false)
        );


        properties.put(HEATING_ROD_PWM_SETPOINT_DEFAULT, new ServiceProperty(
        		HEATING_ROD_PWM_SETPOINT_DEFAULT, "Default PWM duty cycle setpoint for the heating rod to start with", "66", true)
        );
        properties.put(HEATING_ROD_TEMP_SETPOINT, new ServiceProperty(
        		HEATING_ROD_TEMP_SETPOINT, "Temperature setpoint for the heating rod", "10", true)
        );
        properties.put(HEATING_ROD_TEMP_HYSTERESIS, new ServiceProperty(
        		HEATING_ROD_TEMP_HYSTERESIS, "Temperature hysteresis for the heating rod", "1", true)
        );
        properties.put(HEATING_ROD_STATE, new ServiceProperty(
        		HEATING_ROD_STATE, "Channel ID for the state of the heating rod", "hp_source_hr_state", true)
        );
        properties.put(HEATING_ROD_PWM, new ServiceProperty(
        		HEATING_ROD_PWM, "Channel ID for the PWM duty cycle of the heating rod", "hp_source_hr_pwm", true)
        );


        properties.put(HEAT_PUMP_TEMP_INLET, new ServiceProperty(
        		HEAT_PUMP_TEMP_INLET, "Channel ID for the source inlet temperature of the heat pump", "hp_source_temp_in", true)
        );
        properties.put(HEAT_PUMP_TEMP_OUTLET, new ServiceProperty(
        		HEAT_PUMP_TEMP_OUTLET, "Channel ID for the source outlet temperature of the heat pump", "hp_source_temp_out", true)
        );
        properties.put(HEAT_PUMP_SOURCE_PUMP_STATE, new ServiceProperty(
        		HEAT_PUMP_SOURCE_PUMP_STATE, "Channel ID for the source circulation pump state of the heat pump", "hp_source_pump_state", true)
        );
    }

}
