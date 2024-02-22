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
package org.the.ems.env;

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


public abstract class PulseWidthModulator implements Controllable {
    private static final Logger logger = LoggerFactory.getLogger(PulseWidthModulator.class);

	public static enum PulseEdge {
		RISING,
		FALLING;
	}

    // Interval time in [ms]
    protected final int period;

    // Duty cycle ratio boundaries for PWM in
    protected final double ratioMin;
	protected final double ratioMax;

    protected final Channel state;
    protected final Channel percent;

    protected double percentSetpoint;

    protected PulseWidthSetpointListener percentSetpointListener;

    protected PulseTask pulseTask;
    protected Timer pulseTimer;

    // PWM ratio
    protected double ratio = 0;


	public PulseWidthModulator(Channel pwmState, Channel pwmPercent, int period, int percentMin, int percentMax) {
		super();
		this.period = period;
		this.ratioMin = ((double) percentMin)/100.;
		this.ratioMax = ((double) percentMax)/100.;

		this.state = pwmState;
		this.percent = pwmPercent;
        if (percent != null) {
        	percentSetpointListener = new PulseWidthSetpointListener();
        	percent.addListener(percentSetpointListener);
        }
	}

    public void shutdown() {
        if (percent != null) {
        	percent.removeListener(percentSetpointListener);
        }
    }

	public void reset() {
		if (pulseTask != null) {
			pulseTask.cancel();
			pulseTask = null;
		}
        if (pulseTimer != null) {
            pulseTimer.cancel();
            pulseTimer.purge();
            pulseTimer = null;
        }
        set(0);
	}

	@Override
	public void set(double percent) {
		this.percentSetpoint = percent;
        if (percentSetpoint > 100) {
        	percentSetpoint = 100;
        }
        if (percentSetpoint < 0) {
        	percentSetpoint = 0;
        }
    	this.ratio = percentSetpoint / 100.;
        if (ratio < ratioMin) {
        	ratio = 0;
        }
        if (ratio > ratioMax) {
        	ratio = 1;
        }
        if (this.percent != null) {
            this.percent.setLatestRecord(new Record(new IntValue((int) Math.round(percentSetpoint)), System.currentTimeMillis()));
        }
        else {
            setUpdateTimer((int) Math.round(period*ratio));
        }
	}

	@Override
	public double getSetpoint() {
		return percentSetpoint;
	}

	protected synchronized void setUpdateTimer(int timeActive) {
		if (pulseTask != null) {
			if (pulseTask.timeActive == timeActive && timeActive > 0) {
				return;
			}
			pulseTask.cancel();
			pulseTask = null;
		}
        if (pulseTimer != null) {
            pulseTimer.cancel();
            pulseTimer.purge();
            pulseTimer = null;
        }
        if (timeActive == 0) {
        	writeState(false);
            return;
        }
        pulseTask = new PulseTask(timeActive);
        pulseTimer = new Timer("TH-E Environment: Pump PWM Timer");
        pulseTimer.scheduleAtFixedRate(pulseTask, 0, period);
    }

    protected void writeState(boolean enable) {
    	if (state.getLatestRecord().getFlag() != Flag.VALID) {
    		logger.warn("Invalid PWM state channel flag: {}", state.getLatestRecord().getFlag());
    		return;
    	}
    	if (state.getLatestRecord().getValue().asBoolean() != enable) {
    		logger.debug("Setting PWM state {}", enable);
            state.write(new BooleanValue(enable));
    	}  
    }

    protected boolean checkState() {
        if (state.getLatestRecord().getFlag() != Flag.VALID) {
    		logger.warn("Invalid PWM state channel flag: {}", state.getLatestRecord().getFlag());
            return false;
        }
        return state.getLatestRecord().getValue().asBoolean();
    }

    protected boolean isPulsing() {
		if (pulseTask == null || pulseTask.timeActive == 0 || pulseTimer == null) {
			return false;
		}
		return true;
    }

    protected void onPulse(PulseEdge edge) {
		// Default implementation to be overridden
    }

    private class PulseWidthSetpointListener implements RecordListener {

    	@Override
        public void newRecord(Record percentRecord) {
            if (percentRecord.getFlag() != Flag.VALID) {
                logger.warn("Invalid PWM Record flag state: {}", percentRecord.getFlag());
                return;
            }
            percentSetpoint = percentRecord.getValue().asInt();
            if (percentSetpoint > 100) {
            	percentSetpoint = 100;
            }
            if (percentSetpoint < 0) {
            	percentSetpoint = 0;
            }
            ratio = percentSetpoint/100.;
            if (ratio < ratioMin) {
            	ratio = 0;
            }
            if (ratio > ratioMax) {
            	ratio = 1;
            }
            logger.info("PWM percentage: {}%", percentSetpoint);
            
            setUpdateTimer((int) Math.round(period*ratio));
        }
    }

	class PulseTask extends TimerTask {

        final int timeActive;

        public PulseTask(int timeActive) {
            this.timeActive = timeActive;
        }

        @Override
        public void run() {
            logger.debug("Starting PWM with active time: {}s", timeActive/1000);
            pulseState(timeActive);
        }

        public void pulseState(int timeActive) {
            writeState(true);
        	long pulseStart = System.currentTimeMillis();
            try {
            	onPulse(PulseEdge.RISING);
            	
            } catch (Exception e) {
            	logger.warn("Error while notifying of pulse rising edge");
            }
            long pulseDuration = System.currentTimeMillis() - pulseStart;
            int pulseTime = (int) Math.max(timeActive - pulseDuration, 0);
            wait(pulseTime);
            
            writeState(false);
            try {
            	onPulse(PulseEdge.FALLING);
            	
            } catch (Exception e) {
            	logger.warn("Error while notifying of pulse falling edge");
            }
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
