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

package org.the.ems.env.hp.hr;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.Controller;
import org.the.ems.env.PulseWidthModulator;
import org.the.ems.env.RecordAverageListener;
import org.the.ems.env.hp.HeatPumpEnvironmentProperties;
import org.the.ems.env.hp.HeatPumpEnvironmentSettings;

public class HeatingRodController extends PulseWidthModulator {
    private static final Logger logger = LoggerFactory.getLogger(HeatingRodController.class);

    private final Controller controller;

    private final Value controllerSetpointDefault;

    private final int controllerSetInterval;

    private volatile long controllerSetTimestamp = Long.MAX_VALUE;

    private final double tempSetpoint;
    private final double tempHysteresis;

    //private final Channel sourceTempOutlet;
    private final Channel sourceTempInlet;
    private final Channel sourcePumpState;

    private final RecordAverageListener sourceTempAvgListener;
    private final SourceTempListener sourceTempListener;
    private final SourcePumpStateListener sourcePumpStateListener;

    public HeatingRodController(HeatPumpEnvironmentProperties properties) {
        super(properties.getHeatingRodStateChannel(),
                properties.getHeatingRodPwmChannel(),
                properties.getPwmPeriod(),
                properties.getPwmDutyCycleMin(),
                properties.getPwmDutyCycleMax());
        logger.info("Activating TH-E Environment: Heating Rod Controller");

        controller = new Controller(.1, 1./period, 1.*period, 100, ratioMin*100);
        controllerSetpointDefault = new DoubleValue(properties.getHeatingRodPwmSetpointDefault());
        controllerSetInterval = properties.getInterval();

        tempSetpoint = properties.getHeatingRodTemperatureSetpoint();
        tempHysteresis = properties.getHeatingRodTemperatureHysteresis();

        //sourceTempOutlet = properties.getHeatPumpOutletTempChannel();
        sourceTempInlet = properties.getHeatPumpInletTempChannel();
        sourcePumpState = properties.getHeatPumpSourcePumpStateChannel();

        verifyPump();

        sourceTempAvgListener = new RecordAverageListener(period);
        sourceTempListener = new SourceTempListener();
        sourceTempInlet.addListener(sourceTempListener);
        sourceTempInlet.addListener(sourceTempAvgListener);

        sourcePumpStateListener = new SourcePumpStateListener();
        sourcePumpState.addListener(sourcePumpStateListener);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        logger.info("Deactivating TH-E Environment: Heating Rod Controller");

        reset();
        sourceTempInlet.removeListener(sourceTempListener);
        sourcePumpState.removeListener(sourcePumpStateListener);
    }

    @Override
    public void reset() {
        super.reset();
        controller.reset();
    }

    private boolean verifyPump() {
        if (sourcePumpState.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Skip switching heating rod. Source pump invalid flag: {}", sourcePumpState.getLatestRecord().getFlag());
            return false;
        }
        if (!sourcePumpState.getLatestRecord().getValue().asBoolean()) {
            reset();
            return false;
        }
        return true;
    }

    @Override
    protected void onPulse(PulseEdge edge) {
        if (edge == PulseEdge.FALLING && System.currentTimeMillis() - controllerSetTimestamp >= controllerSetInterval) {
            double controlSetpoint  = Math.round(controller.process(controllerSetInterval, tempSetpoint, sourceTempAvgListener.getMean()));

            if (Math.abs(controlSetpoint - percentSetpoint) > .5) {
            	controllerSetTimestamp = System.currentTimeMillis();
                set(controlSetpoint);

                logger.info("Updated Heating rod PWM duty cycle setpoint: {}", controlSetpoint);
            }
        }
    }

    private class SourceTempListener implements RecordListener {

        @Override
        public void newRecord(Record record) {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source inlet channel invalid flag: {}", record.getFlag());
                return;
            }
            logger.trace("Heat pump source intlet temperature : {} Â°C", String.format("%.1f", record.getValue().asDouble()));
            
//            // Check if heating rod needs to be switched off
//            if (record.getValue().asDouble() > tempSetpoint + tempHysteresis && isPulsing()) {
//                logger.info("Heat pump source inlet temperatur {} > {}°C. Switching heating rod off", 
//                        String.format("%.1f", record.getValue().asDouble()), String.format("%.1f", tempSetpoint + tempHysteresis));
//
//                reset();
//            }
            if (!verifyPump()) {
            	return;
            }
            
            // Check if heating rod needs to be switched on
            if (sourcePumpState.getLatestRecord().getFlag()!= Flag.VALID) {
                logger.warn("Skip switching heating rod. Source pump invalid flag: {}", sourcePumpState.getLatestRecord().getFlag());
                return;
            }
            if (!sourcePumpState.getLatestRecord().getValue().asBoolean()) {
                logger.debug("Skip switching heating rod off. Source pump is not running.");
                return;
            }
            if (record.getValue().asDouble() < tempSetpoint + tempHysteresis && !isPulsing()) {
                logger.info("Heat pump source inlet temperatur {} < {}°C. Switching heating rod on", 
                        String.format("%.1f", record.getValue().asDouble()), String.format("%.1f", tempSetpoint + tempHysteresis));
                
                double percentSetpoint = controllerSetpointDefault.asDouble();
                controller.setIntegal(percentSetpoint);
                controllerSetTimestamp = System.currentTimeMillis();
                set(percentSetpoint);
            }
        }
    }

    private class SourcePumpStateListener implements RecordListener {

        @Override
        public void newRecord(Record record) {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heat pump source pump channel invalid flag: {}", record.getFlag());
                return;
            }
            if (!record.getValue().asBoolean() && isPulsing()) {
                logger.info("Heat pump source pump is not running, heating rod will be switched off.");
                reset();
            }
        }
    }

}
