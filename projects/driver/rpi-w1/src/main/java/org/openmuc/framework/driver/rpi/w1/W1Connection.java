/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.driver.rpi.w1;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.DriverPreferences;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.w1.options.ChannelSettings;
import org.openmuc.framework.driver.rpi.w1.options.W1Type;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;


@Component
public class W1Connection implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(W1Connection.class);
    private final DriverPreferences prefs = DriverInfoFactory.getPreferences(W1Connection.class);

    /**
     * Interface used by {@link W1Connection} to notify the {@link W1Driver} about events
     */
    public interface W1ConnectionCallbacks {
        
        public void onDisconnect(String id);
    }

    /**
     * The Connections current callback object, which is used to notify of connection events
     */
    private final W1ConnectionCallbacks callbacks;

    private final W1Device device;
    private final W1Type type;

    public W1Connection(W1ConnectionCallbacks callbacks, W1Device device, W1Type type) {
        this.callbacks = callbacks;
        this.device = device;
        this.type = type;
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settingsStr)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {

        throw new UnsupportedOperationException();
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        long samplingTime = System.currentTimeMillis();

        for (ChannelRecordContainer container : containers) {
            try {
                ChannelSettings settings = prefs.get(container.getChannelSettings(), ChannelSettings.class);
                
                Value value = null;
                switch(type) {
                case TEMPERATURE_SENSOR:
                    TemperatureSensor sensor = (TemperatureSensor) device;
                    Double temperature = sensor.getTemperature(settings.getUnit());
                    
                    if (temperature != null) {
                        // Skip temperature readings of exactly 85, as they are commonly missreadings
                    	if (temperature < 85) {
                            value = new DoubleValue(temperature);
                    	}
                    	else {
                    		// Don't skip the reading, if the latest value read was longer than 15 minutes ago or above 80
                        	Record lastRecord = container.getChannel().getLatestRecord();
                        	if (lastRecord != null && lastRecord.getFlag() == Flag.VALID) {
                        		if (samplingTime - lastRecord.getTimestamp() >= 900000 || 
                        				lastRecord.getValue().asDouble() >= 80) {
                        			
                                    value = new DoubleValue(temperature);
                        		}
                        	}
                    	}
                    }
                    break;
                default:
                    break;
//                    throw new UnsupportedOperationException("Reading 1-Wire devices not supported for type: " + type);
                }
                
                if (value != null) {
                    container.setRecord(new Record(value, samplingTime, Flag.VALID));
                }
                else {
                    logger.warn("Unknown error occurred while reading {} device: {}", type.getName(), device.getId());
                    container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
                }
                
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e.getMessage());
                container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
            }
        }
        
        return null;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        throw new UnsupportedOperationException("Listening for 1-Wire devices not supported for type: " + type.getName());
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
    	
        throw new UnsupportedOperationException("Writing to 1-Wire devices not supported for type: " + type.getName());
    }

    @Override
    public void disconnect() {
        callbacks.onDisconnect(device.getId().trim().replace("\n", "").replace("\r", ""));
    }
}
