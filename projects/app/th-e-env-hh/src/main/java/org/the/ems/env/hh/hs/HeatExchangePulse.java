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

import org.the.ems.env.Flow;
import org.the.ems.env.PulseWidthModulator;
import org.the.ems.env.hh.HouseholdEnvironmentProperties;


public class HeatExchangePulse extends PulseWidthModulator implements HeatSink {

    private Flow flow;

    // Max. heat sink power in [W]
    private final double powerMax;

    public HeatExchangePulse(HouseholdEnvironmentProperties properties) {
		super(properties.getPulseValveStateChannel(),
				properties.getPulseValvePwmChannel(),
				properties.getPwmPeriod(),
				properties.getPwmDutyCycleMin(),
				properties.getPwmDutyCycleMax());

        flow = new Flow(properties.getFlowRate(),
        		properties.getPulseValveHexTempInChannel(),
        		properties.getPulseValveHexTempOutChannel(),
        		properties.getPulseValveHexTempDeltaChannel(),
        		properties.getPulseValveHexPowerChannel(),
        		properties.getPulseValveHexEnergyChannel());

		powerMax = properties.getPulseValvePowerMax();
    }

    @Override
    public double getPower() {
        return flow.getAveragePower();
    }

    public double getPowerMax() {
    	return powerMax;
    }
}
