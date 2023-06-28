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
package org.the.ems.env.hh.vlv;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hh.vlv.Valve;


public class Valve implements Runnable, RecordListener {

    private static final Logger logger = LoggerFactory.getLogger(Valve.class);

    private enum Rotation {
        CW, CCW;
    }

    private final Channel rotateClockwise;

    private final Channel rotateCounterClockwise;

    private final int rotateDuration;

    Rotation rotateMin = Rotation.CCW;

    private long rotationTimeSetpoint = -1;


    private int positionAngleMin = 0;

    private final int positionAngleMax = 90;

    private final Channel positionAngleSetpoint;

    private final Channel positionAngle;

    private Record positionAngleRecord = new Record(Flag.NO_VALUE_RECEIVED_YET);
    private boolean positionAngleCalibrated = false;

    private Thread positionAngleWatcher = null;
    private int positionAngleWatchInterval;

    private volatile boolean deactivate;

    public Valve(int rotateDuration,
            Channel rotateCounterClockwise, 
            Channel rotateClockwise, 
            Channel positionAngleSetpoint, 
            Channel positionAngle) {
        this.rotateDuration = rotateDuration;
        this.rotateClockwise = rotateClockwise;
        this.rotateCounterClockwise = rotateCounterClockwise;
        this.positionAngleSetpoint = positionAngleSetpoint;
        this.positionAngle = positionAngle;

        int millisPerDegree = (int) Math.round(((double) rotateDuration)/(positionAngleMax*10.));
        if (millisPerDegree < 100) {
            millisPerDegree = 100;
        }
        positionAngleWatchInterval = millisPerDegree;
        activate();

        logger.info("Configured position angle watcher interval of {}ms", positionAngleWatchInterval);
    }

    public int getPositionAngleMinimum() {
        return positionAngleMin;
    }

    public int getPositionAngleMaximum() {
        return positionAngleMax;
    }

    public Record getPositionAngle() {
        return positionAngleRecord;
    }

    public Record getPositionAngleSetpoint() {
        return positionAngleSetpoint.getLatestRecord();
    }

    private boolean isRotating() {
        return isRotatingClockwise() || isRotatingCounterClockwise();
    }

    private boolean isRotatingClockwise() {
        Record rotationRecord = rotateClockwise.getLatestRecord();
        if (rotationRecord.getFlag() != Flag.VALID) {
            return false;
        }
        return rotationRecord.getValue().asBoolean();
    }

    private boolean isRotatingCounterClockwise() {
        Record rotationRecord = rotateCounterClockwise.getLatestRecord();
        if (rotationRecord.getFlag() != Flag.VALID) {
            return false;
        }
        return rotationRecord.getValue().asBoolean();
    }

    public void reset() {
        if (isRotatingClockwise()) {
            rotateClockwise.write(new BooleanValue(false));
        }
        if (isRotatingCounterClockwise()) {
            rotateCounterClockwise.write(new BooleanValue(false));
        }
    }

    public void setMin() {
        set(new IntValue(getPositionAngleMinimum()));
    }

    public void setMax() {
        set(new IntValue(getPositionAngleMaximum()));
    }

    protected void calibrate() {
        logger.info("Calibration initiated");
        
        long rotationTimeStart = System.currentTimeMillis();
        rotationTimeSetpoint = rotationTimeStart + rotateDuration;
        positionAngleRecord = new Record(new DoubleValue(0), rotationTimeStart);
        if (positionAngle != null) {
            positionAngle.setLatestRecord(positionAngleRecord);
        }
        positionAngleCalibrated = false;

        rotateMin();
        startWatcher();
    }

    private void rotateMin() {
        switch (rotateMin) {
        case CW:
            if (isRotatingCounterClockwise()) {
                rotateCounterClockwise.write(new BooleanValue(false));
            }
            rotateClockwise.write(new BooleanValue(true));
            break;
        case CCW:
        default:
            if (isRotatingClockwise()) {
                rotateClockwise.write(new BooleanValue(false));
            }
            rotateCounterClockwise.write(new BooleanValue(true));
            break;
        }
    }

    private void rotateMax() {
        switch (rotateMin) {
        case CW:
            if (isRotatingClockwise()) {
                rotateClockwise.write(new BooleanValue(false));
            }
            rotateCounterClockwise.write(new BooleanValue(true));
            break;
        case CCW:
        default:
            if (isRotatingCounterClockwise()) {
                rotateCounterClockwise.write(new BooleanValue(false));
            }
            rotateClockwise.write(new BooleanValue(true));
            break;
        }
    }

    public final void set(Value value) {
        if (positionAngleSetpoint.getLatestRecord().getFlag() != Flag.VALID || Math.abs(value.asDouble() - 
                positionAngleSetpoint.getLatestRecord().getValue().asDouble()) > .1) {
            this.positionAngleSetpoint.setLatestRecord(new Record(value, System.currentTimeMillis()));
            logger.warn("Invalid AngleSetpoint Flag: {} or the other condition",positionAngleSetpoint.getLatestRecord().getFlag());
            return;
        }
        if (!positionAngleCalibrated) {
            logger.info("Skipping setting of rotation angle setpoint while calibrating");
            return;
        }
        logger.info("Received position angle setpoint value: {}", value.asDouble());

        double positionAngleDelta = Math.abs(value.asDouble() - positionAngleRecord.getValue().asDouble());
        long rotationTimeDelta = Math.round(rotateDuration*(positionAngleDelta / (double) getPositionAngleMaximum()));
        long rotationTimeStart = System.currentTimeMillis();
        rotationTimeSetpoint = rotationTimeStart + rotationTimeDelta;

        // Set angle value, as timestamp will be used to calculate passed time
        double positionAngle = positionAngleRecord.getValue().asDouble();
        positionAngleRecord = new Record(new DoubleValue(positionAngle), rotationTimeStart);

        logger.info("Calculated necessary rotation time of {} seconds", rotationTimeDelta/1000);

        if (value.asDouble() < positionAngle) {
            rotateMin();
        }
        else if (value.asDouble() > positionAngle) {
            rotateMax();
        }
        startWatcher();
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() != Flag.VALID) {
            logger.warn("Invalid AngleSetpoint Flag: {}",record.getFlag());
            return;
        }
        set(record.getValue());
    }

    void activate() {
        positionAngleSetpoint.setLatestRecord(new Record(new DoubleValue(0), System.currentTimeMillis()));
        positionAngleSetpoint.addListener(this);
        calibrate();
    }

    void deactivate() {
        positionAngleSetpoint.removeListener(this);
        this.stopWatcher();
    }

    private void startWatcher() {
        if (positionAngleWatcher != null) {
            synchronized (positionAngleWatcher) {
                if (!positionAngleWatcher.isInterrupted() && 
                        positionAngleWatcher.isAlive()) {
                    return;
                }
            }
        }
        logger.info("Starting Watcher Thread");
        positionAngleWatcher = new Thread(this);
        positionAngleWatcher.setName("TH-E Enviroment: Heatpump position angle watcher");
        positionAngleWatcher.start();
    }

    private void stopWatcher() {
        deactivate = true;
        
        synchronized (positionAngleWatcher) {
            positionAngleWatcher.interrupt();
            try {
                positionAngleWatcher.join();
                
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void run() {
        deactivate = false;
        while (!deactivate) {
            try {
                long timestamp = System.currentTimeMillis();
                if (positionAngleCalibrated) {
                    if (isRotating()) {
                        if (positionAngleRecord.getFlag() != Flag.VALID) {
                            logger.warn("Position Angle invalid Flag : {}",positionAngleRecord.getFlag());
                            return;
                        }
                        double rotationTimeChange = (timestamp - positionAngleRecord.getTimestamp()) / (double) rotateDuration;
                        double positionAngle = positionAngleRecord.getValue().asDouble();
                        double positionAngleChange = (getPositionAngleMaximum() - getPositionAngleMinimum()) * 
                                rotationTimeChange;
                        
                        if (isRotatingClockwise())  {
                            positionAngle += positionAngleChange;
                        }
                        else if (isRotatingCounterClockwise()) {
                            positionAngle -= positionAngleChange;
                        }
                        positionAngle = Math.round(positionAngle*10)/10.;
                        if (positionAngle > getPositionAngleMaximum()) {
                            positionAngle = getPositionAngleMaximum();
                        }
                        if (positionAngle < getPositionAngleMinimum()) {
                            positionAngle = getPositionAngleMinimum();
                        }
                        positionAngleRecord = new Record(new DoubleValue(positionAngle), timestamp);
                        
                        if (timestamp >= rotationTimeSetpoint) {
                            reset();
                        }
                    }
                }
                else if (timestamp >= rotationTimeSetpoint) {
                    positionAngleCalibrated = true;
                    reset();
                    logger.info("Calibration complete");
                }
                if (positionAngle.getLatestRecord().getFlag() != Flag.VALID) {
                    logger.warn("Invalid angle position Flag : {}", positionAngle.getLatestRecord().getFlag());
                    return;
                }
                if (Double.isFinite(positionAngleRecord.getValue().asDouble()) && 
                        positionAngle != null &&
                        positionAngle.getLatestRecord().getValue().asDouble() != positionAngleRecord.getValue().asDouble()) {
                    positionAngle.setLatestRecord(positionAngleRecord);
                    logger.debug("Calculate new position angle: {}", positionAngleRecord.getValue().asDouble());
                }
                if (!isRotating()) {
                    if (Math.abs(positionAngleRecord.getValue().asDouble() -
                            positionAngleSetpoint.getLatestRecord().getValue().asDouble()) > .1) {
                        set(positionAngleSetpoint.getLatestRecord().getValue());
                    }
                    else {
                        deactivate = true;
                        logger.info("Deactivate position angle watcher for rotation task is complete");
                    }
                }
                if (!deactivate) {
                    long time = System.currentTimeMillis() - timestamp;
                    long sleep = positionAngleWatchInterval - time;
                    if (sleep > 0) {
                        Thread.sleep(sleep);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while watching valve position angle");
            }
        }
    }
}
