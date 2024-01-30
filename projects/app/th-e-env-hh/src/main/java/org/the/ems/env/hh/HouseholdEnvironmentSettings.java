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

package org.the.ems.env.hh;

import org.openmuc.framework.lib.osgi.config.GenericSettings;
import org.openmuc.framework.lib.osgi.config.ServiceProperty;

public class HouseholdEnvironmentSettings extends GenericSettings {

    static final String ENABLED = "enabled";
    static final String INTERVAL = "interval";
    static final int INTERVAL_DEFAULT = 3*60;

	static final String PWM_PERIOD = "pwmPeriod";
	static final String PWM_DUTY_CYCLE_MIN = "pwmDutyCycleMin";
	static final String PWM_DUTY_CYCLE_MAX = "pwmDutyCycleMax";

    static final String THERMAL_POWER = "thermalPower";
    static final String THERMAL_POWER_SETPOINT = "thermalPowerSetpoint";

	static final String FLOW_RATE = "flowRate";

	static final String FLOW_POWER_HIGH = "flowPowerHigh";
	static final String FLOW_POWER_LOW = "flowPowerLow";
	static final String FLOW_PUMP_STATE = "flowPumpState";
	static final String FLOW_PUMP_PWM = "flowPumpPWM";

	static final String VALVE_PULSE_POWER_MAX = "pulseValvePowerMax";
	static final String VALVE_PULSE_STATE = "pulseValveState";
	static final String VALVE_PULSE_PWM = "pulseValvePWM";
	static final String VALVE_PULSE_HEX_TEMP_IN = "pulseValveHexTempIn";
	static final String VALVE_PULSE_HEX_TEMP_OUT = "pulseValveHexTempOut";
	static final String VALVE_PULSE_HEX_TEMP_DELTA = "pulseValveHexTempDelta";
	static final String VALVE_PULSE_HEX_POWER = "pulseValveHexPower";
	static final String VALVE_PULSE_HEX_ENERGY = "pulseValveHexEnergy";

	static final String VALVE_THREE_WAY_POWER_MAX = "threeWayValvePowerMax";
	static final String VALVE_THREE_WAY_ROT_DURATION = "threeWayValveClockwiseRotationDuration";
	static final String VALVE_THREE_WAY_ROT_CW_STATE = "threeWayValveClockwiseRotationState";
	static final String VALVE_THREE_WAY_ROT_CCW_STATE = "threeWayValveCounterClockwiseRotationState";
	static final String VALVE_THREE_WAY_ANGLE = "threeWayValvePositionAngle";
	static final String VALVE_THREE_WAY_ANGLE_SETPOINT = "threeWayValvePositionAngleSetpoint";
	static final String VALVE_THREE_WAY_HEX_TEMP_IN = "threeWayValveHexTempIn";
	static final String VALVE_THREE_WAY_HEX_TEMP_OUT = "threeWayValveHexTempOut";
	static final String VALVE_THREE_WAY_HEX_TEMP_DELTA = "threeWayValveHexTempDelta";
	static final String VALVE_THREE_WAY_HEX_POWER = "threeWayValveHexPower";
	static final String VALVE_THREE_WAY_HEX_ENERGY = "threeWayValveHexEnergy";

    static final String HEAT_PUMP_SOURCE_PUMP_STATE = "heatPumpSourcePumpState";

    public HouseholdEnvironmentSettings() {
        super();
        properties.put(ENABLED, new ServiceProperty(
        		ENABLED, "Emulation control enabled flag", "true", false)
        );
        properties.put(INTERVAL, new ServiceProperty(
        		INTERVAL, "Control interval of the emulation in seconds", String.valueOf(INTERVAL_DEFAULT), false)
        );


        properties.put(PWM_PERIOD, new ServiceProperty(
        		PWM_PERIOD, "PWM interval or period in seconds", "60", false)
        );
        properties.put(PWM_DUTY_CYCLE_MIN, new ServiceProperty(
        		PWM_DUTY_CYCLE_MIN, "Minium PWM duty cycle in %", "5", false)
        );
        properties.put(PWM_DUTY_CYCLE_MAX, new ServiceProperty(
        		PWM_DUTY_CYCLE_MAX, "Maximum PWM duty cycle in %", "55", false)
        );


        properties.put(THERMAL_POWER, new ServiceProperty(
        		THERMAL_POWER, "Channel ID for the thermal power of the household", "hh_flow_power", true)
        );
        properties.put(THERMAL_POWER_SETPOINT, new ServiceProperty(
        		THERMAL_POWER_SETPOINT, "Channel ID for the thermal power setpoint of the household", "hh_th_power", true)
        );


        properties.put(FLOW_RATE, new ServiceProperty(
        		FLOW_RATE, "Flow rate of the circulation flow of the household", "hh_flow_rate", true)
        );


        properties.put(FLOW_POWER_HIGH, new ServiceProperty(
        		FLOW_POWER_HIGH, "Passive thermal power dissipation of the circulation flow when cold", "3500", true)
        );
        properties.put(FLOW_POWER_LOW, new ServiceProperty(
        		FLOW_POWER_LOW, "Passive thermal power dissipation of the circulation flow when warm", "1000", true)
        );
        properties.put(FLOW_PUMP_STATE, new ServiceProperty(
        		FLOW_PUMP_STATE, "Channel ID for the flow pump state of the household", "hh_flow_pump_state", true)
        );
        properties.put(FLOW_PUMP_PWM, new ServiceProperty(
        		FLOW_PUMP_PWM, "Channel ID for the flow pump PWM of the household", "hh_flow_pump_pwm", true)
        );


        properties.put(VALVE_PULSE_POWER_MAX, new ServiceProperty(
        		VALVE_PULSE_POWER_MAX, "Thermal power dissipation of the heat exchange when the pulse valve is open", "10000", true)
        );
        properties.put(VALVE_PULSE_STATE, new ServiceProperty(
        		VALVE_PULSE_STATE, "Channel ID for the pulse valve state of the household consumption emulation", "hh_flow_valve_state", true)
        );
        properties.put(VALVE_PULSE_PWM, new ServiceProperty(
        		VALVE_PULSE_PWM, "Channel ID for the pulse valve PWM of the household consumption emulation", "hh_flow_valve_pwm", true)
        );
        properties.put(VALVE_PULSE_HEX_TEMP_IN, new ServiceProperty(
        		VALVE_PULSE_HEX_TEMP_IN, "Temperature of the heat exchange inlet of the PWM valve", "hh_flow_valve_in_temp", true)
        );
        properties.put(VALVE_PULSE_HEX_TEMP_OUT, new ServiceProperty(
        		VALVE_PULSE_HEX_TEMP_OUT, "Temperature of the heat exchange outlet of the PWM valve", "hh_flow_valve_out_temp", true)
        );
        properties.put(VALVE_PULSE_HEX_TEMP_DELTA, new ServiceProperty(
        		VALVE_PULSE_HEX_TEMP_DELTA, "Temperature delta of the heat exchange the PWM valve", "hh_flow_valve_delta_temp", true)
        );
        properties.put(VALVE_PULSE_HEX_POWER, new ServiceProperty(
        		VALVE_PULSE_HEX_POWER, "Thermal power dissipation of the heat exchange of the PWM valve", "hh_flow_valve_power", true)
        );
        properties.put(VALVE_PULSE_HEX_ENERGY, new ServiceProperty(
        		VALVE_PULSE_HEX_ENERGY, "Thermal energy dissipation of the heat exchange of the PWM valve", "hh_flow_fan_energy", true)
        );


        properties.put(VALVE_THREE_WAY_POWER_MAX, new ServiceProperty(
        		VALVE_THREE_WAY_POWER_MAX, "Thermal power dissipation of the heat exchange when the three-way valve is fully open", "2500", true)
        );
        properties.put(VALVE_THREE_WAY_ROT_DURATION, new ServiceProperty(
        		VALVE_THREE_WAY_ROT_DURATION, "Rotation duration of the three-way valve", "210", true)
        );
        properties.put(VALVE_THREE_WAY_ROT_CCW_STATE, new ServiceProperty(
        		VALVE_THREE_WAY_ROT_CCW_STATE, "State channel for the counterclockwise rotation of the three-way valve", "hp_source_valve_rot_ccw_state", true)
        );
        properties.put(VALVE_THREE_WAY_ROT_CW_STATE, new ServiceProperty(
        		VALVE_THREE_WAY_ROT_CW_STATE, "State channel for the clockwise rotation of the three-way valve", "hp_source_valve_rot_cw_state", true)
        );
        properties.put(VALVE_THREE_WAY_ANGLE_SETPOINT, new ServiceProperty(
        		VALVE_THREE_WAY_ANGLE_SETPOINT, "Angle setpoint channel of the three-way valve", "hp_source_rotate_angle_setpoint", true)
        );
        properties.put(VALVE_THREE_WAY_ANGLE, new ServiceProperty(
        		VALVE_THREE_WAY_ANGLE, "Angle channel of the three-way valve", "hp_source_rotate_angle", true)
        );
        properties.put(VALVE_THREE_WAY_HEX_TEMP_IN, new ServiceProperty(
        		VALVE_THREE_WAY_HEX_TEMP_IN, "Temperature of the heat exchange inlet of the three-way valve valve", "hh_flow_tes_out_temp", true)
        );
        properties.put(VALVE_THREE_WAY_HEX_TEMP_OUT, new ServiceProperty(
        		VALVE_THREE_WAY_HEX_TEMP_OUT, "Temperature of the heat exchange outlet of the three-way valve valve", "hh_flow_valve_out_temp", true)
        );
        properties.put(VALVE_THREE_WAY_HEX_TEMP_DELTA, new ServiceProperty(
        		VALVE_THREE_WAY_HEX_TEMP_DELTA, "Temperature delta of the heat exchange the three-way valve valve", "hh_flow_hp_source_delta_temp", true)
        );
        properties.put(VALVE_THREE_WAY_HEX_POWER, new ServiceProperty(
        		VALVE_THREE_WAY_HEX_POWER, "Thermal power dissipation of the heat exchange of the three-way valve", "hh_flow_hp_source_power", true)
        );
        properties.put(VALVE_THREE_WAY_HEX_ENERGY, new ServiceProperty(
        		VALVE_THREE_WAY_HEX_ENERGY, "Thermal energy dissipation of the heat exchange of the three-way valve", "hh_flow_hp_source_energy", true)
        );
        

        properties.put(HEAT_PUMP_SOURCE_PUMP_STATE, new ServiceProperty(
        		HEAT_PUMP_SOURCE_PUMP_STATE, "Channel ID for the source circulation pump state of the heat pump", "hp_source_pump_state", true)
        );
    }

}
