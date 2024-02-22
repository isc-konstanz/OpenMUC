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

import java.util.ArrayDeque;
import java.util.Deque;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    protected final double proportional;
    protected final double integral;
    protected final double derivative;
    protected final double maximum;
    protected final double minimum;
 
    protected final Deque<Record> errors = new ArrayDeque<Record>();

    protected double errorDragTolerance = 0;
    protected boolean errorDragging = false;


    protected double errorProportional = 0;
    protected double errorIntegral = 0;
    protected double errorDerivative = 0;
    protected double errorLast = 0;

    public Controller(double proportional, double integral, double derivative, double maximum, double minimum) {
        this.proportional = proportional;
        this.derivative = derivative;
        this.integral = integral;
        this.maximum = maximum;
        this.minimum = minimum;
    }

    public void enableErrorDragging(double tolerance) {
    	errorDragTolerance = tolerance;
    	errorDragging = true;
    }

    public void disableErrorDragging() {
    	errorDragTolerance = 0;
    	errorDragging = false;
    }

    public void setIntegal(double integral) {
    	this.errorIntegral = integral;
    }

    public double process(long timeDelta, double setpoint, double reference) {
        double error = setpoint - reference;
        
        if (errorDragging) {
            double errorLast = 0;
            if (!errors.isEmpty()) {
            	errorLast = errors.stream().mapToDouble(r -> r.getValue().asDouble()).sum();
                if (Math.abs(errorLast) < errorDragTolerance) {
                    logger.debug("Delta of last errors smaller than {}, removing past errors.", errorDragTolerance);
                    errorLast = 0;
                    errors.clear();
                }
            }
            
            errors.add(new Record(new DoubleValue(error), System.currentTimeMillis()));
            if (errors.size() > 20) {
                logger.trace("More than 20 errors in deque, starting clearing");
                errors.removeFirst();
                while (errors.getFirst().getValue().asDouble() < 0) {
                    errors.removeFirst();
                }
            }
            error += errorLast;
        }
        
        errorProportional = proportional * error;
        errorIntegral = lim(errorIntegral + integral * error * timeDelta);
        errorDerivative = derivative * (error - errorLast) / timeDelta;
        errorLast = error;

        double value = lim(errorProportional + errorIntegral + errorDerivative);

        logger.debug("Calculated P({}) + I({}) + D({}) = {}", 
        		String.format("%.2f", errorProportional), 
        		String.format("%.2f", errorIntegral), 
        		String.format("%.2f", errorDerivative),
        		value);

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
