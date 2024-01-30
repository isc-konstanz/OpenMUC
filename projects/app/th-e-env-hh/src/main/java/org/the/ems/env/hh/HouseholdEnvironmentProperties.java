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

import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.lib.osgi.config.PropertyHandler;


public class HouseholdEnvironmentProperties extends PropertyHandler {

	private final DataAccessService dataAccessService;

    public HouseholdEnvironmentProperties(HouseholdEnvironmentSettings settings, DataAccessService dataAccessService) {
        super(settings, HouseholdEnvironment.class.getName());
        this.dataAccessService = dataAccessService;
    }

    public boolean isEnabled() {
    	return getBoolean(HouseholdEnvironmentSettings.ENABLED);
    }

    public int getInterval() {
    	return getInt(HouseholdEnvironmentSettings.INTERVAL)*1000;
    }

    public int getPwmPeriod() {
    	return getInt(HouseholdEnvironmentSettings.PWM_PERIOD)*1000;
    }

    public int getPwmDutyCycleMin() {
    	return getInt(HouseholdEnvironmentSettings.PWM_DUTY_CYCLE_MIN);
    }

    public int getPwmDutyCycleMax() {
    	return getInt(HouseholdEnvironmentSettings.PWM_DUTY_CYCLE_MAX);
    }

    public Channel getThermalPowerChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.THERMAL_POWER));
    }

    public Channel getThermalPowerSetpointChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.THERMAL_POWER_SETPOINT));
    }

    public Channel getFlowRate() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.FLOW_RATE));
    }

    public double getFlowPowerHigh() {
    	return getDouble(HouseholdEnvironmentSettings.FLOW_POWER_HIGH);
    }

    public double getFlowPowerLow() {
    	return getDouble(HouseholdEnvironmentSettings.FLOW_POWER_LOW);
    }

    public Channel getFlowPumpStateChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.FLOW_PUMP_STATE));
    }

    public Channel getFlowPumpPwmChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.FLOW_PUMP_PWM));
    }

    public double getPulseValvePowerMax() {
    	return getDouble(HouseholdEnvironmentSettings.VALVE_PULSE_POWER_MAX);
    }

    public Channel getPulseValveStateChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_STATE));
    }

    public Channel getPulseValvePwmChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_PWM));
    }

    public Channel getPulseValveHexTempInChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_HEX_TEMP_IN));
    }

    public Channel getPulseValveHexTempOutChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_HEX_TEMP_OUT));
    }

    public Channel getPulseValveHexTempDeltaChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_HEX_TEMP_DELTA));
    }

    public Channel getPulseValveHexPowerChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_HEX_POWER));
    }

    public Channel getPulseValveHexEnergyChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_PULSE_HEX_ENERGY));
    }

    public double getThreeWayValvePowerMax() {
    	return getDouble(HouseholdEnvironmentSettings.VALVE_THREE_WAY_POWER_MAX);
    }

    public int getThreeWayValveRotationDuration() {
    	return getInt(HouseholdEnvironmentSettings.VALVE_THREE_WAY_ROT_DURATION)*1000;
    }

    public Channel getThreeWayValveCounterClockwiseRotationStateChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_ROT_CCW_STATE));
    }

    public Channel getThreeWayValveClockwiseRotationStateChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_ROT_CW_STATE));
    }

    public Channel getThreeWayValveAngleSetpointChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_ANGLE_SETPOINT));
    }

    public Channel getThreeWayValveAngleChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_ANGLE));
    }





    public Channel getThreeWayValveHexTempInChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_HEX_TEMP_IN));
    }

    public Channel getThreeWayValveHexTempOutChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_HEX_TEMP_OUT));
    }

    public Channel getThreeWayValveHexTempDeltaChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_HEX_TEMP_DELTA));
    }

    public Channel getThreeWayValveHexPowerChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_HEX_POWER));
    }

    public Channel getThreeWayValveHexEnergyChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.VALVE_THREE_WAY_HEX_ENERGY));
    }

    public Channel getHeatPumpSourcePumpStateChannel() {
    	return dataAccessService.getChannel(getString(HouseholdEnvironmentSettings.HEAT_PUMP_SOURCE_PUMP_STATE));
    }

}
