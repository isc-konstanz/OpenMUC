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
package org.openmuc.framework.driver.rpi.gpio.count;

import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.gpio.configs.GpioChannel;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class EdgeListener implements GpioPinListenerDigital {
    private final static Logger logger = LoggerFactory.getLogger(EdgeListener.class);

    private List<GpioChannel> channels = null;
    private RecordsReceivedListener listener = null;

    private final int bounceTime;
    private final PinEdge edge;
    private final GpioPin pin;

    private Long lastSamplingTime = null;
    private volatile Integer counter;

    public EdgeListener(GpioPin pin, PinPullResistance pullResistance, int bounceTime) {
        this.pin = pin;
        this.bounceTime = bounceTime;
        if (pullResistance == PinPullResistance.PULL_UP) {
            edge = PinEdge.FALLING;
        }
        else edge = PinEdge.RISING;
        
        this.counter = 0;
    	logger.debug("Registered edge listener for {}", pin.getName());
    }

    public void setRecordListener(List<GpioChannel> channels, RecordsReceivedListener listener) {
        this.channels = channels;
        this.listener = listener;
    }

    public int getValue() {
        return this.counter;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getPin() == pin && event.getEdge() == edge) {
            long samplingTime = System.currentTimeMillis();
            
            if (lastSamplingTime == null || samplingTime - lastSamplingTime > bounceTime) {
                synchronized (counter) {
                    counter++;
                    
                    if (listener != null && channels.size() > 0) {
                        List<ChannelRecordContainer> containers = new LinkedList<ChannelRecordContainer>();
                        for (GpioChannel channel : channels) {
                            Value value = null;
                            if (channel.isDerivative()) {
                                if (lastSamplingTime != null) {
                                    double counterDelta = 1.0/channel.getImpulses();
                                    double timeDelta = (samplingTime - lastSamplingTime)/channel.getDerivativeTime();
                                    if (timeDelta > 0) {
                                        value = new DoubleValue(counterDelta/timeDelta);
                                    }
                                }
                            }
                            else {
                                value = new DoubleValue(counter/channel.getImpulses());
                            }
                            if (value != null) {
                            	logger.debug("Registered {}. edge for {}: {}", counter, event.getPin().getName(), value);
                            	channel.setRecord(new Record(value, samplingTime, Flag.VALID));
                            }
                            else {
                            	channel.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_CHANNEL_TEMPORARILY_NOT_ACCESSIBLE));
                            }
                            containers.add(channel);
                        }
                        listener.newRecords(containers);
                    }
                    lastSamplingTime = samplingTime;
                }
            }
        }
    }

}
