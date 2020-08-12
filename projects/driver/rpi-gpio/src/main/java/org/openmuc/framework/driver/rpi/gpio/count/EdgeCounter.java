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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.gpio.InputPin;
import org.openmuc.framework.driver.rpi.gpio.configs.GpioChannel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinPullResistance;

public class EdgeCounter extends InputPin {

    private final EdgeListener counter;
    private final Map<String, Record> counters = new HashMap<String, Record>();

    public EdgeCounter(GpioPinDigital pin, PinPullResistance pullResistance, int bounceTime) {
        super(pin);
        
        counter = new EdgeListener(pin, pullResistance, bounceTime);
        pin.addListener(counter);
    }

    @Override
    public void onStartListening(List<GpioChannel> channels, RecordsReceivedListener listener) throws ConnectionException {
    	counter.setRecordListener(channels, listener);
    }

    @Override
    public Object onRead(List<GpioChannel> channels, Object containerListHandle, String samplingGroup) throws ConnectionException {
        long samplingTime = System.currentTimeMillis();
        int newVal = counter.getValue();

        for (GpioChannel channel : channels) {
            Value value = null;
            if (channel.isDerivative() || channel.isIntervalCount()) {
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
                double counterDelta = (newVal - lastVal)/channel.getImpulses();
                
                if (channel.isDerivative()) {
                    if (lastRecord != null) {
                        double timeDelta = (samplingTime - lastRecord.getTimestamp())/channel.getDerivativeTime();
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
                value = new DoubleValue(newVal/channel.getImpulses());
            }
            if (value != null) {
                channel.setRecord(new Record(value, samplingTime, Flag.VALID));
            }
            else {
                channel.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_CHANNEL_TEMPORARILY_NOT_ACCESSIBLE));
            }
        }
        return null;
    }

}
