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
package org.the.ems.env.hh.fan;

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
import org.the.ems.env.hh.flow.Flow;

public class Fan implements RecordListener, HeatSink {

    private static final String ID_FAN = "hh_flow_fan_state";
    private static final String ID_FAN_PWM = "hh_flow_fan_pwm";

    private static final String ID_TEMP_HEATEXCHANGER_IN = "hh_flow_fan_in_temp";
    private static final String ID_TEMP_HEATEXCHANGER_OUT = "hh_flow_fan_out_temp";
    private static final String ID_TEMP_HEATEXCHANGER_DELTA = "hh_flow_fan_delta_temp";
    private static final String ID_FLOW_VOLUME = "hh_flow_rate";
    private static final String ID_POWER_HEATEXCHANGER = "hh_flow_fan_power";
    private static final String ID_ENERGY_HEATEXCHANGER = "hh_flow_fan_energy";

    private static final Logger logger = LoggerFactory.getLogger(Fan.class);

    // Thermal power for fan to cool
    private double thPowerSetpoint;

    private Channel fanChannel;
    private Channel fanPWM;

    Flow flowFan;
    private Timer updateTimer;
    // Max. fan cooling power in [W]
    private double maxPower = 2500;

    // Interval time in [s], default 3 min
    private int intervalTime = 180;

    // Interval time for PWM in [s], varies from 60 to 180s 
    private int singleInterval = 180;

    // Number of intervals in one intervalTime
    private int intervalNumber = 3;

    // PWM percent [%]
    private int percentPWM = 0;

    // Active fan time per singleInterval [ms]
    private int timeOn;

    // Time boundaries for fan in [ms]
    private final int maxTime = 55 * 1000;
    private final int minTime = 15 * 1000;

    public Fan(DataAccessService dataAccessService) {
        this.fanChannel = dataAccessService.getChannel(ID_FAN);
        this.fanPWM = dataAccessService.getChannel(ID_FAN_PWM);
        this.fanPWM.addListener(this);
        
        flowFan = new Flow(dataAccessService.getChannel(ID_FLOW_VOLUME),
                dataAccessService.getChannel(ID_POWER_HEATEXCHANGER),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_IN),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_OUT),
                dataAccessService.getChannel(ID_TEMP_HEATEXCHANGER_DELTA),
                dataAccessService.getChannel(ID_ENERGY_HEATEXCHANGER));
    }
    @Override
    public void setSetPoint(double thpower) {
        thPowerSetpoint = thpower;
        logger.trace("Heat output to be dissipated via the fan: {}W",thPowerSetpoint);
        int percent = (int) ((thPowerSetpoint/maxPower) * 100);
        if (percent > 100) {
            logger.info("PWM Percent:{}% bigger than 100% setting it to 100%",percent);
            percent = 100;
        }
        if (percent < 0) {
            logger.info("PWM Percent:{}% negative, setting it 0%",percent);
            percent = 0;
        }
        fanPWM.setLatestRecord(new Record(new IntValue(percent), System.currentTimeMillis()));
    }

    @Override
    public double getPower() {
        return flowFan.getAveragePower();
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() != Flag.VALID) { 
            logger.warn("Invalid PWM Record flag state:{}", record.getFlag());
            return;
        }
        percentPWM = record.getValue().asInt();
        if (percentPWM > 100) {
            logger.info("PWM Percent:{}% bigger than 100% setting it to 100%",percentPWM);
            percentPWM = 100;
        }
        logger.trace("PWM percentage:{}%", percentPWM);
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
        updateTimer = new Timer("TH-E Environment: Fan PWM Timer");
        updateTimer.scheduleAtFixedRate(task, 0, interval);
    }

    public void writeState(boolean bol) {
        if (fanChannel.getLatestRecord().getFlag() != Flag.VALID) {
            logger.info("Invalid Fan Channel Flag : {}",fanChannel.getLatestRecord().getFlag());
            return;
        }
        if (fanChannel.getLatestRecord().getValue().asBoolean() != bol) {
            logger.info("Setting Ventilator {}", bol);
            fanChannel.write(new BooleanValue(bol));
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

            Record stateRecord = fanChannel.getLatestRecord();
            if (stateRecord.getFlag() != Flag.VALID) {
                logger.warn("Invalid Fan flag state:{}", stateRecord.getFlag());
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
