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
package org.the.ems.env.hh.pump;

import java.util.Timer;
import java.util.TimerTask;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hh.HeatSink;

public class Pump implements RecordListener,HeatSink {
	
	private static final String ID_PUMP_STATE ="hh_flow_pump_state";
	private static final String ID_PUMP_PWM ="hh_flow_pump_pwm";
	
    Channel pumpState;
    Channel pumpPWM;
    
    private Timer updateTimer;
    
    // Thermal power for fan to cool
    private double thPowerSetpoint;
    
    // Max. fan cooling power in [W]
    private double maxPowerlow = 1000;
    private double maxPowerhigh = 3500;
    private boolean highPower = false;

    // Interval time in [s], default 3 min
    private int intervalTime = 180;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
    // Interval time for PWM in [s], varies from 60 to 180s 
    private int singleInterval = 180;

    // Number of intervals in one intervalTime
    private int intervalNumber = 3;

    // PWM percent [%]
    private int percentLastPWM = 0;

    // Active fan time per singleInterval [ms]
    private int timeOn = 0;
    private int percent = 0;

    // Time boundaries for fan in [ms]
    private final int maxTime = 55 * 1000;
    private final int minTime = 15 * 1000;
    
    private static final Logger logger = LoggerFactory.getLogger(Pump.class);

	public Pump(DataAccessService dataAccessService) {
		this.pumpState = dataAccessService.getChannel(ID_PUMP_STATE);
		this.pumpPWM = dataAccessService.getChannel(ID_PUMP_PWM);
		this.pumpPWM.addListener(this);
	}

	@Override
	public void setSetPoint(double thPower) {
		thPowerSetpoint = thPower;
        logger.trace("Heat output to be dissipated via the pump: {}W",thPowerSetpoint);
        if (percentLastPWM < 30) {
        	percent = (int) ((thPowerSetpoint/maxPowerhigh) * 100);
        	logger.trace("Highpower");
        	highPower = true;
        }
        else {
        	percent = (int) ((thPowerSetpoint/maxPowerlow) * 100);
        	logger.trace("Lowpower");
        	highPower = false;
        }
        if (percent > 100) {
            logger.info("PWM Percent:{}% bigger than 100% setting it to 100%",percent);
            percent = 100;
        }
        if (percent < 0) {
            logger.info("PWM Percent:{}% negative, setting it 0%",percent);
            percent = 0;
        }
        pumpPWM.setLatestRecord(new Record(new IntValue(percent), System.currentTimeMillis()));
	}
	
	@Override
	public double getPower() {
		if (highPower) {
			return maxPowerhigh;
		}
		else {
			return maxPowerlow;
		}
	}
	
	@Override
    public void newRecord(Record record) {
        if (record.getFlag() != Flag.VALID) { 
            logger.warn("Invalid PWM Record flag state:{}", record.getFlag());
            return;
        }
        int percentPWM = record.getValue().asInt();
        if (percentPWM > 100) {
            logger.info("PWM Percent:{}% bigger than 100% setting it to 100%",percentPWM);
            percentPWM = 100;
        }
        logger.info("PWM percentage:{}%", percentPWM);
        percentLastPWM = percentPWM;
        setPWM(percentPWM);
    }
	
	public void setPWM(int percent) {
        int takt = 1;
        int timeToRun = (intervalTime*percent/100) / intervalNumber;
        if (timeToRun >= minTime/1000) {
            takt = 3;
            singleInterval = intervalTime / takt;
            timeOn = timeToRun * intervalNumber / takt * 1000;
        }
        if (timeToRun < minTime/1000 & timeToRun >= 10) {
            takt = 2;
            singleInterval = intervalTime / takt;
            timeOn = timeToRun * intervalNumber / takt * 1000;
        }
        if (timeToRun < 10 & timeToRun >= 5) {
            takt = 1;
            singleInterval = intervalTime / takt;
            timeOn = timeToRun * intervalNumber / takt * 1000;
        }
        if (timeToRun < 5) {
        	logger.info("No Fan PWM needed");
        	timeOn = 0;
        	singleInterval = 0;
        }

        setUpdateTimer(singleInterval);
    }
	
	public synchronized void setUpdateTimer(int singleInterval) {
        final long interval = (long) singleInterval * 1000;

        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge();
            updateTimer = null;
        }
        if (interval == 0) {
        	writeState(false);
            return;
        }
        TimerTask task = new PwmTimer(timeOn);
        updateTimer = new Timer("TH-E Environment: Pump PWM Timer");
        updateTimer.scheduleAtFixedRate(task, 0, interval);
    }

    public void writeState(boolean state) {
		logger.info("WriteState Pump:{}",state);
    	if (pumpState.getLatestRecord().getFlag() != Flag.VALID) {
    		logger.info("Invalid Pump Channel Flag : {}",pumpState.getLatestRecord().getFlag());
    		return;
    	}
    	if (pumpState.getLatestRecord().getValue().asBoolean() != state) {
    		logger.info("Setting Pump {}", state);
            pumpState.write(new BooleanValue(state));
    	}  
    }
    
	class PwmTimer extends TimerTask {

        private final int timeActive;

        public PwmTimer(int timeOn) {
            this.timeActive = timeOn;
        }
        
        @Override
        public void run() {
            logger.info("Starting PWM with timeActive:{}s", timeActive/1000);
            pulseRealais(timeActive);
        }

        public void pulseRealais(int timeActive) {

            Record stateRecord = pumpState.getLatestRecord();
            if (stateRecord.getFlag() != Flag.VALID) {
                logger.warn("Invalid Pump flag state:{}", stateRecord.getFlag());
                return;
            }
            if (timeActive >= maxTime) {
                if (!stateRecord.getValue().asBoolean()) {
                    writeState(true);
                }
            }
            else if (timeActive >= minTime) {
                if (!stateRecord.getValue().asBoolean()) {
                    writeState(true);
                }
                wait(timeActive);
                writeState(false);
            }
        }
        public void wait(int ms){
            try {
                Thread.sleep(ms);
                
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
