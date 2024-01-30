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

import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.lib.osgi.config.PropertyHandler;

public class HeatPumpEnvironmentProperties extends PropertyHandler {

	private final DataAccessService dataAccessService;

    public HeatPumpEnvironmentProperties(HeatPumpEnvironmentSettings settings, DataAccessService dataAccessService) {
        super(settings, HeatPumpEnvironment.class.getName());
        this.dataAccessService = dataAccessService;
    }

    public double getHeatingRodTemperatureSetpoint() {
    	return getDouble(HeatPumpEnvironmentSettings.HEATING_ROD_TEMP_SETPOINT);
    }

    public double getHeatingRodTemperatureHysteresis() {
    	return getDouble(HeatPumpEnvironmentSettings.HEATING_ROD_TEMP_HYSTERESIS);
    }

    public Channel getHeatingRodLowStateChannel() {
    	return dataAccessService.getChannel(getString(HeatPumpEnvironmentSettings.HEATING_ROD_2PH_STATE));
    }

    public Channel getHeatingRodHighStateChannel() {
    	return dataAccessService.getChannel(getString(HeatPumpEnvironmentSettings.HEATING_ROD_3PH_STATE));
    }

    public Channel getHeatPumpInletTempChannel() {
    	return dataAccessService.getChannel(getString(HeatPumpEnvironmentSettings.HEAT_PUMP_TEMP_INLET));
    }

    public Channel getHeatPumpOutletTempChannel() {
    	return dataAccessService.getChannel(getString(HeatPumpEnvironmentSettings.HEAT_PUMP_TEMP_OUTLET));
    }

    public Channel getHeatPumpSourcePumpStateChannel() {
    	return dataAccessService.getChannel(getString(HeatPumpEnvironmentSettings.HEAT_PUMP_SOURCE_PUMP_STATE));
    }

}
