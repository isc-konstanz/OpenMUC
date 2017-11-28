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
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.s0.options.S0ChannelPreferences;
import org.openmuc.framework.driver.rpi.s0.options.S0DriverInfo;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@Component
public class S0Connection implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(S0Connection.class);
    private final S0DriverInfo info = S0DriverInfo.getInfo();

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

    private Map<String, Integer> lastCounters = new HashMap<String, Integer>();

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
                S0ChannelPreferences prefs = info.getChannelPreferences(container);
                
                Value value;
                if (prefs.isCountInterval()) {
                    String channelId = container.getChannel().getId();
                    
                    int lastVal;
                    if (lastCounters.containsKey(channelId)) {
                        lastVal = lastCounters.get(channelId);
                    }
                    else {
                        lastVal = 0;
                    }
                    lastCounters.put(channelId, newVal);
                    
                    value = new DoubleValue((newVal - lastVal)/(double) prefs.getImpulses());
                }
                else {
                    value = new DoubleValue(newVal/(double) prefs.getImpulses());
                }
                container.setRecord(new Record(value, samplingTime, Flag.VALID));
                
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e);
                container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
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

    private class S0Listener implements GpioPinListenerDigital {

        private List<ChannelRecordContainer> containers = null;
        private RecordsReceivedListener listener = null;

        private final int bounceTime;
        private final PinEdge edge;
        private final GpioPin pin;
        
        private long lastSamplingTime;
        private volatile Integer counter;

        public S0Listener(GpioPin pin, PinPullResistance pullResistance, int bounceTime) {
            this.pin = pin;
            this.bounceTime = bounceTime;
            if (pullResistance == PinPullResistance.PULL_UP) {
                edge = PinEdge.FALLING;
            }
            else edge = PinEdge.RISING;
            
            this.counter = 0;
            lastSamplingTime = System.currentTimeMillis();
        }

        public void setRecordListener(List<ChannelRecordContainer> containers, RecordsReceivedListener listener) {
            this.containers = containers;
            this.listener = listener;
        }

        public int getValue() {
            return this.counter;
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (event.getPin() == pin && event.getEdge() == edge) {
                long samplingTime = System.currentTimeMillis();
                
                if (samplingTime - lastSamplingTime > bounceTime) {
                    synchronized (this.counter) {
                        
                        this.counter += 1;
                        
                        if (listener != null && containers != null) {
                            for (ChannelRecordContainer container : containers) {
                                try {
                                    S0ChannelPreferences prefs = info.getChannelPreferences(container);
                                    
                                    Value value = new DoubleValue(this.counter/(double) prefs.getImpulses());
                                    container.setRecord(new Record(value, samplingTime, Flag.VALID));
                                    
                                } catch (ArgumentSyntaxException e) {
                                    logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e.getMessage());
                                    container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
                                }
                            }
                            listener.newRecords(containers);
                        }
                        lastSamplingTime = samplingTime;
                    }
                }
            }
        }
    }
}
