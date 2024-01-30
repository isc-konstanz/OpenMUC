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
package org.the.ems.env.hh.hs;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.dataaccess.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hh.Flow;
import org.the.ems.env.hh.HouseholdEnvironmentProperties;
import org.the.ems.env.hh.Valve;

public class HeatExchangeValve implements HeatSink {
    private static final Logger logger = LoggerFactory.getLogger(HeatExchangeValve.class);

    private final double powerMax;
    private double powerSetpoint = 0;

    private final Flow flow;

    private final Valve valve;
    private double valveAngle = 0;

    private Channel pumpState;

    public HeatExchangeValve(HouseholdEnvironmentProperties properties) {
        valve = new Valve(properties.getThreeWayValveRotationDuration(),
        		properties.getThreeWayValveCounterClockwiseRotationStateChannel(),
        		properties.getThreeWayValveClockwiseRotationStateChannel(),
        		properties.getThreeWayValveAngleSetpointChannel(),
        		properties.getThreeWayValveAngleChannel());

        flow = new Flow(properties.getFlowRate(),
        		properties.getThreeWayValveHexTempInChannel(),
        		properties.getThreeWayValveHexTempOutChannel(),
        		properties.getThreeWayValveHexTempDeltaChannel(),
        		properties.getThreeWayValveHexPowerChannel(),
        		properties.getThreeWayValveHexEnergyChannel());

		powerMax = properties.getThreeWayValvePowerMax();

        pumpState = properties.getHeatPumpSourcePumpStateChannel();
    }

    @Override
    public void set(double setpoint) {
        if (valve.getPositionAngle().getFlag() != Flag.VALID) {
            logger.warn("Valve angle position invalid flag: {}", valve.getPositionAngle().getFlag());
            return;
        }
        if (pumpState.getLatestRecord().getFlag() != Flag.VALID) {
        	logger.warn("BrinePump Invalid Flag: {}", pumpState.getLatestRecord().getFlag());
            return;
        }
        
        if (setpoint < 300 || !pumpState.getLatestRecord().getValue().asBoolean() ) {
        	valve.set(new DoubleValue(0));
        }
        
        if (Math.abs(setpoint - setpoint) > 300 && pumpState.getLatestRecord().getValue().asBoolean()) {
            valveAngle = setpoint/powerMax * 90;
            if (valveAngle > 90) {
            	valveAngle = 90;
            }
            valve.set(new DoubleValue(valveAngle));
        }
        this.powerSetpoint = setpoint; 
    }

	@Override
	public double getSetpoint() {
		return powerSetpoint;
	}

	@Override
	public double getPower() {
		if (valveAngle < 10) {
			return 250;
		}
		if (valveAngle < 60 && valveAngle > 10) {
			return 1500;
		}
		if (valveAngle > 60) {
			return 2500;
		}
		return flow.getAveragePower();
	}

	@Override
	public double getPowerMax() {
		return powerMax;
	}
}
