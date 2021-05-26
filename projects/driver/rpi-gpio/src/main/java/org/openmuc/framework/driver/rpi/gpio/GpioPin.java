/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.driver.rpi.gpio;

import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.annotation.Listen;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public abstract class GpioPin extends GpioConfigs {
    protected static final Logger logger = LoggerFactory.getLogger(GpioPin.class);

    protected final GpioPinDigital pin;

    public GpioPin(GpioPinDigital pin) {
        this.pin = pin;
    }

    public GpioPinDigital getGpioPin() {
    	return pin;
    }

    @Listen
    public void listen(List<GpioChannel> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        pin.addListener(new GpioListener(channels, listener, pin));
    }

    protected class GpioListener implements GpioPinListenerDigital {

        private final List<GpioChannel> channels;
        private final RecordsReceivedListener listener;

        private final GpioPinDigital pin;

        public GpioListener(List<GpioChannel> channels, RecordsReceivedListener listener, GpioPinDigital pin) {
            this.channels = channels;
            this.listener = listener;
            this.pin = pin;
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            long samplingTime = System.currentTimeMillis();
            
            List<ChannelRecordContainer> containers = new LinkedList<ChannelRecordContainer>();
            for (GpioChannel channel : channels) {
                PinState state = pin.getState();
                Value value;
                if (!channel.isInverted()) {
                    value = new BooleanValue(state.isHigh());
                }
                else {
                    value = new BooleanValue(state.isLow());
                }
                channel.setRecord(new Record(value, samplingTime, Flag.VALID));
                containers.add((ChannelRecordContainer) channel.getTaskContainer());
                
                logger.debug("Received value for listened pin \"{}\": {}", pin.getName(), value);
            }
            listener.newRecords(containers);
        }
    }

}
