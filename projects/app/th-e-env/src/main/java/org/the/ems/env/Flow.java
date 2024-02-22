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

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Flow {

    public enum FlowTemperature {
        IN,
        OUT;
    }

    private static final int SECONDS_PER_INTERVAL = 60*3;

    private final int interval = SECONDS_PER_INTERVAL*1000;
    // The specific heat capacity of the flow medium. Default is 4.1813 of water.
    private double flowSpecificHeat = 4.1813;

    // The density of the flow medium. Default is 1 of water.
    private double flowDensity = 1;

    private double flowVolumeSeconds = 60;

    // The channel key of the volume in liter hours
    private Channel flowVolume;

    private Channel flowPower;

    private Channel flowTempIn;

    private Channel flowTempOut;

    private Channel flowTempDelta;

    private List<Value> flowTempDeltaValues = new ArrayList<Value>();
    private List<Double> flowTempInValues = new ArrayList<Double>();
    private List<Double> flowTempOutValues = new ArrayList<Double>();

    private Channel flowEnergy;
    private Record energyLatest = null;

    RecordAverageListener flowPowerAverage;

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    public Flow(Channel flowVolume,
                Channel flowTempIn,
                Channel flowTempOut,
                Channel flowTempDelta,
                Channel flowPower,
                Channel flowEnergy) {
        this.flowVolume = flowVolume;
        this.flowTempIn = flowTempIn;
        this.flowTempOut = flowTempOut;
        this.flowTempDelta = flowTempDelta;
        
        this.flowEnergy = flowEnergy;
        this.flowEnergy.setLatestRecord(new Record(new DoubleValue(0),System.currentTimeMillis()));

        this.flowPower = flowPower;
        this.flowPowerAverage = new RecordAverageListener(interval);
        this.flowPower.addListener(flowPowerAverage);
        onActivate();
    }

    public double getAveragePower() {
        return flowPowerAverage.getMean();
    }

    public double getFlowInletTemperature() {
        return flowTempInValues.stream().mapToDouble(v -> v).average().orElse(
                flowTempIn.getLatestRecord().getValue().asDouble());
    }

    public double getFlowOutletTemperature() {
        return flowTempOutValues.stream().mapToDouble(v -> v).average().orElse(
                flowTempOut.getLatestRecord().getValue().asDouble());
    }

    public Value getFlowDeltaTemperature() {
        return flowTempDelta.getLatestRecord().getValue();
    }

    void onActivate() {

        flowVolume.addListener(new FlowVolumeListener());
        flowTempIn.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heatexchanger source inlet channel invalid flag: {}", record.getFlag());
                return;
            }
            onTemperatureReceived(FlowTemperature.IN,record);
        });
        flowTempOut.addListener(record -> {
            if (record.getFlag() != Flag.VALID) {
                logger.warn("Heatexchanger source outlet channel invalid flag: {}", record.getFlag());
                return;
            }
            onTemperatureReceived(FlowTemperature.OUT,record);
        });
    }

    public synchronized void onTemperatureReceived(FlowTemperature type, Record record) {
        
        if (record.getFlag() != Flag.VALID) {
            logger.warn("Temperature {} channel invalid flag: {}",type, record.getFlag());
            return;
        }
        double temperature = record.getValue().asDouble();
        if (!Double.isFinite(temperature)) {
            return;
        }
        switch(type) {
        case IN:
            flowTempInValues.add(temperature);
            break;
        case OUT:
            flowTempOutValues.add(temperature);
            break;
        default:
            return;
        }
        if (flowTempInValues.size() > 0 && flowTempOutValues.size() > 0) {
                double delta = getFlowInletTemperature() - getFlowOutletTemperature();

                synchronized (flowTempDeltaValues) {
                    flowTempDelta.write(new DoubleValue(delta));
                    flowTempDeltaValues.add(new DoubleValue(delta));
                }
                flowTempInValues.clear();
                flowTempOutValues.clear();
        }
    }

    private class FlowListener {
    
        protected void onLitersReceived(double flow, long timestamp) {
            
            double flowMass = flow*flowDensity;

            Double flowTempDeltaValue = Double.NaN;
            synchronized (flowTempDeltaValues) {
                if (flowTempDeltaValues.size() > 0) {
                    flowTempDeltaValue = flowTempDeltaValues.stream()
                            .mapToDouble(d -> d.asDouble())
                            .average().orElse(Double.NaN);
                    flowTempDeltaValues.clear();
                }
                else {
                    if (flowTempDelta.getLatestRecord().getFlag() !=Flag.VALID) {
                        logger.warn("Temperature delta channel invalid flag: {}", flowTempDelta.getLatestRecord().getFlag());
                        return;
                    }
                    flowTempDeltaValue = flowTempDelta.getLatestRecord().getValue().asDouble();
                }

                if (flowTempDeltaValue.isNaN()) {
                    flowTempDeltaValues.clear();
                    return;
                }
            }
            double flowEnergyValue = flowSpecificHeat*flowMass*flowTempDeltaValue;

            if (energyLatest != null && energyLatest.getFlag() == Flag.VALID) {
                long timeDelta = (timestamp - energyLatest.getTimestamp())/1000;
                double flowPowerValue = (flowEnergyValue/ (double) timeDelta) *1000;
                if (!Double.isNaN(flowPowerValue) && !Double.isInfinite(flowPowerValue)) {
                    flowPower.write(new DoubleValue(flowPowerValue));
                }

            }
            onEnergyReceived(new Record(new DoubleValue(flowEnergyValue/3600), timestamp));
        }

    }

    private class FlowVolumeListener extends FlowListener implements RecordListener {

        private long flowTimeLast = -1;

        @Override
        public void newRecord(Record record) {
            if (record.getFlag() != Flag.VALID) {
                return;
            }
            long flowTime = record.getTimestamp();
            if (flowTimeLast > 0) {
                long flowTimeDelta = flowTime - flowTimeLast;
                double flow = record.getValue().asDouble()*((double) flowTimeDelta/(flowVolumeSeconds*1000));
                onLitersReceived(flow, flowTime);
            }
            flowTimeLast = flowTime;

        }
    }

    protected void onEnergyReceived(Record energyValue) {
        if (energyValue.getFlag() != Flag.VALID) {
            logger.warn("Energy record invalid flag: {}",energyValue.getFlag() );
            return;
        }
        if (energyLatest == null) {
            energyLatest = energyValue;
        }
        else {
            energyLatest = new Record(new DoubleValue(energyLatest.getValue().asDouble() + energyValue.getValue().asDouble()),
                    energyValue.getTimestamp());
        }
        if (flowEnergy.getLatestRecord().getFlag() != Flag.VALID) {
            logger.warn("Energy channel invalid flag: {}",flowEnergy.getLatestRecord().getFlag());
            return;
        }
        flowEnergy.write(energyLatest.getValue());
    }

}

