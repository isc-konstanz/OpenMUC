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

import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hh.HouseholdEnvironmentProperties;


public class FlowPump extends HeatSinkPulse {
    private static final Logger logger = LoggerFactory.getLogger(FlowPump.class);

    // Max. fan cooling power in [W]
    private final double powerLowMax;
    private final double powerHighMax;
    private boolean powerHigh = false;

    private double ratioLast = 0;


	public FlowPump(HouseholdEnvironmentProperties properties) {
		super(properties.getFlowPumpStateChannel(),
				properties.getFlowPumpPwmChannel(),
				properties.getPwmPeriod(),
				properties.getPwmDutyCycleMin(),
				properties.getPwmDutyCycleMax());
		powerLowMax = properties.getFlowPowerLow();
		powerHighMax = properties.getFlowPowerHigh();
	}

	@Override
	public void set(double powerSetpoint) {
		this.powerSetpoint = powerSetpoint;
		
        logger.trace("Heat output to be dissipated via the pump: {}W", powerSetpoint);
        if (ratioLast < .3) {
        	ratio = powerSetpoint/powerHighMax;
        	powerHigh = true;
        	logger.trace("High power mode");
        }
        else {
        	ratio = powerSetpoint/powerLowMax;
        	powerHigh = false;
        	logger.trace("Low power mode");
        }
        if (ratio < ratioMin) {
        	ratio = 0;
        }
        if (ratio > ratioMax) {
        	ratio = 1;
        }
        if (pwmPercent != null) {
            pwmPercent.setLatestRecord(new Record(new IntValue((int) Math.round(ratio * 100)), System.currentTimeMillis()));
        }
        setUpdateTimer((int) Math.round(period*ratio));
	}

	@Override
	public double getPower() {
		return getPowerMax();
	}

	@Override
	public double getPowerMax() {
		if (powerHigh) {
			return powerHighMax;
		}
		else {
			return powerLowMax;
		}
	}
}
