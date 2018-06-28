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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.driver.rpi.gpio.InputPin;
import org.openmuc.framework.driver.rpi.gpio.settings.ChannelSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinPullResistance;

public class EdgeCounter extends InputPin {
    private final static Logger logger = LoggerFactory.getLogger(EdgeCounter.class);

    private final EdgeListener counter;
    private final Map<String, Record> counters = new HashMap<String, Record>();

    public EdgeCounter(GpioConnectionCallbacks callbacks, GpioPinDigital pin, PinPullResistance pullResistance, int bounceTime) {
        super(callbacks, pin);
        
        counter = new EdgeListener(pin, pullResistance, bounceTime);
        pin.addListener(counter);
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
                if (settings.isDerivative() || settings.isIntervalCount()) {
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
                    double counterDelta = (newVal - lastVal)/settings.getImpulses();
                    
                    if (settings.isDerivative()) {
                        if (lastRecord != null) {
                            double timeDelta = (samplingTime - lastRecord.getTimestamp())/settings.getDerivariveTime();
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
                    value = new DoubleValue(newVal/settings.getImpulses());
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

}
