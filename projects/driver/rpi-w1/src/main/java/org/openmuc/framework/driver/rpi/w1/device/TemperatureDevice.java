/*
 * Copyright 2011-2020 Fraunhofer ISE
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
package org.openmuc.framework.driver.rpi.w1.device;

import java.text.MessageFormat;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.w1.W1Channel;
import org.openmuc.framework.driver.rpi.w1.W1Device;
import org.openmuc.framework.driver.rpi.w1.W1Type;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.component.temperature.TemperatureSensor;


public class TemperatureDevice extends W1Device {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureDevice.class);

    private final TemperatureSensor sensor;

    public TemperatureDevice(com.pi4j.io.w1.W1Device device) throws ArgumentSyntaxException {
        super();
        if (W1Type.valueOf(device) != type) {
            throw new ArgumentSyntaxException(MessageFormat.format("1-Wire device \"{0}\" not the expected type: {1}", 
                    id, type));
        }
        this.sensor = (TemperatureSensor) device;
        this.maximum = maximum.isNaN() ? 127 : maximum;
    }

    @Override
    public void onRead(List<W1Channel> channels, String samplingGroup) throws ConnectionException {
        long samplingTime = System.currentTimeMillis();
        
        for (W1Channel channel : channels) {
            Value value = null;
            Double temperature = sensor.getTemperature(channel.getScale());
            
            if (temperature != null) {
                // Skip temperature readings of exactly 85, as they are commonly missreadings
                if (temperature < maximum) {
                    value = new DoubleValue(temperature);
                }
                else {
                    // Don't skip the reading, if the latest value read was longer than 15 minutes ago 
                    // or above 90% of the maximum configured value of the sensor
                    Record lastRecord = channel.getRecord();
                    if (lastRecord != null && lastRecord.getFlag() == Flag.VALID) {
                        if (samplingTime - lastRecord.getTimestamp() >= 900000 || 
                                lastRecord.getValue().asDouble() >= maximum*0.9) {
                            
                            value = new DoubleValue(temperature);
                        }
                    }
                }
            }
            
            if (value != null) {
                channel.setRecord(new Record(value, samplingTime, Flag.VALID));
            }
            else {
                logger.warn("Unknown error occurred while reading temperature sensor: {}", sensor.getName());
                channel.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
            }
        }
    }

	@Override
	protected void onWrite(List<W1Channel> channels) throws UnsupportedOperationException, ConnectionException {
		throw new UnsupportedOperationException("Unable to write to 1-Wire temperature sensors");
	}

}
