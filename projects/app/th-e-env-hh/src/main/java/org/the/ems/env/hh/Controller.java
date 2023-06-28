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
package org.the.ems.env.hh;

import java.util.ArrayDeque;
import java.util.Deque;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {

    protected double errorProportional = 0;
    protected double errorIntegral = 0;
    protected double errorDerivative = 0;
    protected double errorLast = 0;
    protected double lastErrors = 0;
    protected double lastMeasurements = 0;

    protected double proportional;
    protected double integral;
    protected double derivative;
    protected double maximum;
    protected double minimum;
    protected boolean delta;
 
    private final Deque<Record> errors = new ArrayDeque<Record>();
    private final Deque<Record> measurement = new ArrayDeque<Record>();
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(double proportional,double integral, double derivative, double maximum,double minimum, boolean delta) {
        this.proportional = proportional;
        this.derivative = derivative;
        this.integral = integral;
        this.maximum = maximum;
        this.minimum = minimum;
        this.delta = delta;
    }

    public double process(long interval, double thPowerSetpoint, double thPowerSensor) {
        double timeDelta = interval;
        double error = thPowerSetpoint - thPowerSensor;
        if(delta) {
            if (!errors.isEmpty()) {
                if (errors.getLast().getFlag() == Flag.VALID) {
                    lastErrors = errors.stream().mapToDouble(r -> r.getValue().asDouble()).average().orElse(0);
                    lastMeasurements = measurement.stream().mapToDouble(r -> r.getValue().asDouble()).average().orElse(0);
                    lastErrors = lastErrors - lastMeasurements;
                    if (Math.abs(lastErrors) < 200) {
                        logger.debug("Delta of Lasterrors smaller than 200 W, removing past errors!");
                        lastErrors = 0;
                        lastMeasurements = 0;
                        errors.clear();
                        measurement.clear();
                    }
                }
            }

            errors.add(new Record(new DoubleValue(error),System.currentTimeMillis()));
            measurement.add(new Record(new DoubleValue(thPowerSensor),System.currentTimeMillis()));
            error = error + lastErrors;
            if (errors.size() > 20) {
                logger.trace("Bigger thatn 20 items in errors deque, starting clearing");
                errors.removeFirst();
                measurement.removeFirst();
                while (errors.getFirst().getValue().asDouble() < 0) {
                    errors.removeFirst();
                    measurement.removeFirst();
                }
            }
        }

        errorProportional = proportional * error;
        errorIntegral = lim(errorIntegral + integral * error * timeDelta);
        errorDerivative = derivative * (error - errorLast) / timeDelta;
        errorLast = error;

        double value = lim(errorProportional + errorIntegral + errorDerivative);

        return value;
    }

    public void reset() {
        errorProportional = 0;
        errorIntegral = 0;
        errorDerivative = 0;
        errorLast = 0;
    }

    private double lim(double value) {
        if (value > maximum ) {
            return maximum;
        }
        if (value <= minimum) {
            return minimum;
        }
        return value;
    }
}
