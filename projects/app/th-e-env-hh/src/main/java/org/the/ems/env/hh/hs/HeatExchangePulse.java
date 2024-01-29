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
package org.the.ems.env.hh.hs;

import org.openmuc.framework.dataaccess.DataAccessService;
import org.the.ems.env.hh.Flow;


public class HeatExchangePulse extends HeatSinkPulse {

    private static final String ID_FAN = "hh_flow_fan_state";
    private static final String ID_FAN_PWM = "hh_flow_fan_pwm";

    private static final String ID_TEMP_HEATEXCHANGER_IN = "hh_flow_fan_in_temp";
    private static final String ID_TEMP_HEATEXCHANGER_OUT = "hh_flow_fan_out_temp";
    private static final String ID_TEMP_HEATEXCHANGER_DELTA = "hh_flow_fan_delta_temp";
    private static final String ID_FLOW_VOLUME = "hh_flow_rate";
    private static final String ID_POWER_HEATEXCHANGER = "hh_flow_fan_power";
    private static final String ID_ENERGY_HEATEXCHANGER = "hh_flow_fan_energy";

    private Flow flow;

    // Max. heat sink power in [W]
    private final double powerMax = 2500;

    public HeatExchangePulse(DataAccessService dataAccessService) {
		super(dataAccessService.getChannel(ID_FAN),
				dataAccessService.getChannel(ID_FAN_PWM),
				60,
				55 * 1000,
				15 * 1000);
        
        flow = new Flow(dataAccessService.getChannel(ID_FLOW_VOLUME),
                dataAccessService.getChannel(ID_POWER_HEATEXCHANGER),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_IN),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_OUT),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_DELTA),
                dataAccessService.getChannel(ID_ENERGY_HEATEXCHANGER));
    }

    @Override
    public double getPower() {
        return flow.getAveragePower();
    }

    public double getPowerMax() {
    	return powerMax;
    }
}
