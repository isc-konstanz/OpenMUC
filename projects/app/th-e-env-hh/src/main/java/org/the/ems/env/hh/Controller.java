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

public class Controller {

    protected double errorProportional = 0;
    protected double errorIntegral = 0;
    protected double errorDerivative = 0;
    protected double errorLast = 0;

    protected double proportional;
    protected double integral;
    protected double derivative;
    protected double maximum;
    protected double minimum;

    private double lastsetpoint = 0;
    private double deltavalue = 0;

    public Controller(double proportional,double integral, double derivative, double maximum,double minimum) {
        this.proportional = proportional;
        this.derivative = derivative;
        this.integral = integral;
        this.maximum = maximum;
        this.minimum = minimum;

    }

    public double process(long interval, double thPowerSetpoint, double thPowerSensor) {
        double timeDelta = interval/1000;
        deltavalue = lastsetpoint - thPowerSensor;
        lastsetpoint = thPowerSetpoint;
        double error = thPowerSetpoint - thPowerSensor + deltavalue;

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
        if (value < -maximum) {
            return -maximum;
        }
        return value;
    }
}