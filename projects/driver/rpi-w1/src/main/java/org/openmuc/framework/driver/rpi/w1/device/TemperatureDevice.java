/*
 * Copyright 2011-18 Fraunhofer ISE
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

import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.w1.W1Connection;
import org.openmuc.framework.driver.rpi.w1.configs.W1Channel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.component.temperature.TemperatureSensor;

public class TemperatureDevice extends W1Connection {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureDevice.class);

    private final TemperatureSensor sensor;

    public TemperatureDevice(String id, TemperatureSensor sensor) {
    	super(id);
        this.sensor = sensor;
    }

    @Override
    public Object onRead(List<W1Channel> channels, Object containerListHandle, String samplingGroup) throws ConnectionException {
        long samplingTime = System.currentTimeMillis();
        
        for (W1Channel channel : channels) {
            Value value = null;
            Double temperature = sensor.getTemperature(channel.getScale());
            
            if (temperature != null) {
                // Skip temperature readings of exactly 85, as they are commonly missreadings
                if (temperature < 85) {
                    value = new DoubleValue(temperature);
                }
                else {
                    // Don't skip the reading, if the latest value read was longer than 15 minutes ago or above 80
                    Record lastRecord = channel.getChannel().getLatestRecord();
                    if (lastRecord != null && lastRecord.getFlag() == Flag.VALID) {
                        if (samplingTime - lastRecord.getTimestamp() >= 900000 || 
                                lastRecord.getValue().asDouble() >= 80) {
                            
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
        return null;
    }

}
