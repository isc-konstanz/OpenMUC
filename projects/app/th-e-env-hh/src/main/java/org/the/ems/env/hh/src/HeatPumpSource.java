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

package org.the.ems.env.hh.src;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hh.HeatSink;
import org.the.ems.env.hh.flow.Flow;
import org.the.ems.env.hh.vlv.Valve;

public class HeatPumpSource implements HeatSink{

    private Valve valve;
    Flow flow;
    Flow flowBrine;

    private static final String ID_ROTATE_COUNTER_CLOCKWISE = "hp_source_valve_rot_ccw_state";
    private static final String ID_ROTATE_CLOCKWISE = "hp_source_valve_rot_cw_state";
    private static final String ID_POSITION_ANGLE_SETPOINT = "hp_source_rotate_angle_setpoint";
    private static final String ID_POSITION_ANGLE = "hp_source_rotate_angle";

    private static final String ID_TEMP_HEATEXCHANGER_IN ="hh_flow_tes_out_temp";
    private static final String ID_TEMP_HEATEXCHANGER_OUT ="hh_flow_fan_in_temp";
    private static final String ID_TEMP_HEATEXCHANGER_DELTA="hh_flow_hp_source_delta_temp";
    private static final String ID_FLOW_VOLUM="hh_flow_rate";
    private static final String ID_POWER_HEATEXCHANGER="hh_flow_hp_source_power";
    private static final String ID_ENERGY_HEATEXCHANGER = "hh_flow_hp_source_energy";
    
    private static final String ID_TEMP_HEATPUMP_INLET = "hp_source_temp_in";
    private static final String ID_TEMP_HEATPUMP_OUTLET = "hp_source_temp_out";
    private static final String ID_TEMP_HEATPUMP_BRINE_DELTA="hp_source_temp_delta";
    
    private static final String ID_POWER_HEATPUMP_BRINE = "hp_source_power";
    private static final String ID_ENERGY_HEATPUMP_BRINE = "hp_source_energy";
    private static final String ID_FLOW_RATE_HEATPUMP_BRINE = "hp_source_flow_rate";
    
    private static final String ID_BRINE_PUMP = "hp_source_pump_state";

    // Duration for full Rotation of Valve [ms]
    private int rotateDuration = 210*1000;

    private double lastSetpoint = 0;
    private double maxPower = 2500;
    private double calcAngle = 0;

    private static final Logger logger = LoggerFactory.getLogger(HeatPumpSource.class);
    
    private Channel brinePumpState;

    public HeatPumpSource(DataAccessService dataAccessService) {

        valve = new Valve(rotateDuration,
                dataAccessService.getChannel(ID_ROTATE_COUNTER_CLOCKWISE),
                dataAccessService.getChannel(ID_ROTATE_CLOCKWISE),
                dataAccessService.getChannel(ID_POSITION_ANGLE_SETPOINT),
                dataAccessService.getChannel(ID_POSITION_ANGLE));

        flow = new Flow(dataAccessService.getChannel(ID_FLOW_VOLUM),
                dataAccessService.getChannel(ID_POWER_HEATEXCHANGER),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_IN),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_OUT),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_DELTA),
                dataAccessService.getChannel(ID_ENERGY_HEATEXCHANGER));

        flowBrine = new Flow(dataAccessService.getChannel(ID_FLOW_RATE_HEATPUMP_BRINE),
                dataAccessService.getChannel(ID_POWER_HEATPUMP_BRINE),
                dataAccessService.getChannel(ID_TEMP_HEATPUMP_INLET),
                dataAccessService.getChannel(ID_TEMP_HEATPUMP_OUTLET),
                dataAccessService.getChannel(ID_TEMP_HEATPUMP_BRINE_DELTA),
                dataAccessService.getChannel(ID_ENERGY_HEATPUMP_BRINE));
        
        brinePumpState = dataAccessService.getChannel(ID_BRINE_PUMP);
    }
    
    @Override
    public void setSetPoint(double thPowerSetpoint) {
        if (valve.getPositionAngle().getFlag() != Flag.VALID) {
            logger.warn("Valve angle position invalid flag: {}", valve.getPositionAngle().getFlag());
            return;
        }
        if (brinePumpState.getLatestRecord().getFlag() != Flag.VALID) {
        	logger.warn("BrinePump Invalid Flag: {}", brinePumpState.getLatestRecord().getFlag());
            return;
        }
        
        if (thPowerSetpoint < 300 || !brinePumpState.getLatestRecord().getValue().asBoolean() ) {
        	valve.set(new DoubleValue(0));
        }
        
        if (Math.abs(thPowerSetpoint - lastSetpoint) > 300 && brinePumpState.getLatestRecord().getValue().asBoolean()) {
            calcAngle = thPowerSetpoint/maxPower * 90;
            if (calcAngle > 90) {
            	calcAngle = 90;
            }
            valve.set(new DoubleValue(calcAngle));
        }
        lastSetpoint = thPowerSetpoint; 
    }

	@Override
	public double getPower() {
		if (calcAngle < 10) {
			return 250;
		}
		if (calcAngle < 60 && calcAngle > 10) {
			return 1500;
		}
		if (calcAngle > 60) {
			return 2500;
		}
		return flow.getAveragePower();
	}
}
