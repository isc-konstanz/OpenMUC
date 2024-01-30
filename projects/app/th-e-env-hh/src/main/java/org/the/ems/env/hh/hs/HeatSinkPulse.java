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

import java.util.Timer;
import java.util.TimerTask;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class HeatSinkPulse implements RecordListener, HeatSink {
    private static final Logger logger = LoggerFactory.getLogger(HeatSinkPulse.class);

    // Interval time in [ms]
    protected final int period;

    // Duty cycle ratio boundaries for PWM in
    protected final double ratioMin;
	protected final double ratioMax;

    protected final Channel pwmPercent;
    protected final Channel pwmState;

    protected PulseTimer pulseTimer;
    protected Timer updateTimer;

    // Thermal power setpoint to sink
    protected double powerSetpoint;

    // PWM ratio
    protected double ratio = 0;


	public HeatSinkPulse(Channel pwmState, Channel pwmPercent, int period, int percentMin, int percentMax) {
		super();
		this.period = period;
		this.ratioMin = ((double) percentMin)/100.;
		this.ratioMax = ((double) percentMax)/100.;

		this.pwmState = pwmState;
		this.pwmPercent = pwmPercent;
        if (pwmPercent != null) {
    		pwmPercent.addListener(this);
        }
	}

	@Override
	public void set(double powerSetpoint) {
		this.powerSetpoint = powerSetpoint;
    	this.ratio = powerSetpoint/getPowerMax();
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
	public double getSetpoint() {
		return powerSetpoint;
	}

	@Override
    public void newRecord(Record percentRecord) {
        if (percentRecord.getFlag() != Flag.VALID) {
            logger.warn("Invalid PWM Record flag state:{}", percentRecord.getFlag());
            return;
        }
        int percent = percentRecord.getValue().asInt();
        if (percent > 100) {
            percent = 100;
        }
        if (percent < 0) {
        	percent = 0;
        }
        ratio = percent/100;
        if (ratio < ratioMin) {
        	ratio = 0;
        }
        if (ratio > ratioMax) {
        	ratio = 1;
        }
        logger.info("PWM percentage:{}%", percent);
        
        setUpdateTimer((int) Math.round(period*ratio));
    }

	protected synchronized void setUpdateTimer(int timeActive) {
		if (pulseTimer != null) {
			if (pulseTimer.timeActive == timeActive && timeActive > 0) {
				return;
			}
			pulseTimer.cancel();
			pulseTimer = null;
		}
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge();
            updateTimer = null;
        }
        if (timeActive == 0) {
        	writeState(false);
            return;
        }
        pulseTimer = new PulseTimer(timeActive);
        updateTimer = new Timer("TH-E Environment: Pump PWM Timer");
        updateTimer.scheduleAtFixedRate(pulseTimer, 0, timeActive);
    }

    protected void writeState(boolean state) {
    	if (pwmState.getLatestRecord().getFlag() != Flag.VALID) {
    		logger.info("Invalid PWM state channel flag: {}", pwmState.getLatestRecord().getFlag());
    		return;
    	}
    	if (pwmState.getLatestRecord().getValue().asBoolean() != state) {
    		logger.debug("Setting PWM state {}", state);
            pwmState.write(new BooleanValue(state));
    	}  
    }

	class PulseTimer extends TimerTask {

        final int timeActive;

        public PulseTimer(int timeActive) {
            this.timeActive = timeActive;
        }

        @Override
        public void run() {
            logger.info("Starting PWM with active time: {}s", timeActive/1000);
            pulseState(timeActive);
        }

        public void pulseState(int timeActive) {
            writeState(true);
            wait(timeActive);
            
            writeState(false);
        }

        public void wait(int ms) {
            try {
                Thread.sleep(ms);
                
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
