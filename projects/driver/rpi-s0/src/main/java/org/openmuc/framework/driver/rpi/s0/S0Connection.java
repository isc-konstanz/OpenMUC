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
package org.openmuc.framework.driver.rpi.s0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.DriverPreferences;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.driver.rpi.s0.settings.ChannelSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinPullResistance;

@Component
public class S0Connection implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(S0Connection.class);
    private final DriverPreferences prefs = DriverInfoFactory.getPreferences(S0Connection.class);

    /**
     * Interface used by {@link S0Connection} to notify the {@link S0Driver} about events
     */
    public interface S0ConnectionCallbacks {
        
        public void onDisconnect(GpioPin pin);
    }

    /**
     * The Connections current callback object, which is used to notify of connection events
     */
    private final S0ConnectionCallbacks callbacks;

    private final GpioPin pin;
    private final S0Listener counter;
    private final Map<String, Record> counters = new HashMap<String, Record>();

    public S0Connection(S0ConnectionCallbacks callbacks, GpioPin pin, PinPullResistance pullResistance, int bounceTime) {
        this.callbacks = callbacks;
        this.pin = pin;
        
        counter = new S0Listener(pin, pullResistance, bounceTime);
        pin.addListener(counter);
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settingsStr)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {

        throw new UnsupportedOperationException();
//        logger.info("Scan for Channels of Raspberry Pi GPIO pin: {}", pin.getName());
        
//        Parameters settings = new RpiChannelOptions().parseScanSettings(settingsStr);
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        long samplingTime = System.currentTimeMillis();
        int newVal = counter.getValue();

        for (ChannelRecordContainer container : containers) {
            try {
                ChannelSettings settings = prefs.get(container.getChannelSettings(), ChannelSettings.class);
                
                Value value = null;
                if (settings.isDerivative() || settings.isCountInterval()) {
                	Channel channel = container.getChannel();
                    String channelId = channel.getId();
                    Record lastRecord = null;
                    int lastVal;
                    if (counters.containsKey(channelId)) {
                    	lastRecord = counters.get(channelId);
                        lastVal = lastRecord.getValue().asInt();
                    }
                    else {
                        lastVal = 0;
                    }
                    double counterDelta = (newVal - lastVal)/(double) settings.getImpulses();
                    
                    if (settings.isDerivative()) {
                    	if (lastRecord != null) {
                        	double timeDelta = (samplingTime - lastRecord.getTimestamp())/3600000.0;
                        	if (timeDelta > 0) {
                                value = new DoubleValue(counterDelta/timeDelta);
                        	}
                    	}
                    }
                    else {
                        value = new DoubleValue(counterDelta);
                    }
                    
                    counters.put(channelId, new Record(new IntValue(newVal), samplingTime));
                }
                else {
                    value = new DoubleValue(newVal/(double) settings.getImpulses());
                }
                if (value != null) {
                    container.setRecord(new Record(value, samplingTime, Flag.VALID));
                }
                else {
                    container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_CHANNEL_TEMPORARILY_NOT_ACCESSIBLE));
                }
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e);
                container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_CHANNEL_ADDRESS_SYNTAX_INVALID));
            }
        }
        return null;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener recordListener)
            throws UnsupportedOperationException, ConnectionException {

        counter.setRecordListener(containers, recordListener);
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        
        throw new UnsupportedOperationException("Writing to S0 input pins is not supported");
    }

    @Override
    public void disconnect() {
        
        callbacks.onDisconnect(pin);
    }
}
