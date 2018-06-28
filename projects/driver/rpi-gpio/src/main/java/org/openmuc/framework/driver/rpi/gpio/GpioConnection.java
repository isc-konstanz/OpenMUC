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
package org.openmuc.framework.driver.rpi.gpio;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.DriverPreferences;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.gpio.settings.ChannelSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GpioConnection implements Connection {
	protected final static Logger logger = LoggerFactory.getLogger(GpioConnection.class);

    protected final DriverPreferences prefs = DriverInfoFactory.getPreferences(GpioDriver.class);

    /**
     * Interface used by {@link GpioConnection} to notify the {@link GpioDriver} about events
     */
    public interface GpioConnectionCallbacks {
        
        public void onDisconnect(GpioPin pin);
    }

    /**
     * The Connections current callback object, which is used to notify of connection events
     */
    protected final GpioConnectionCallbacks callbacks;

    protected final GpioPinDigital pin;

    public GpioConnection(GpioConnectionCallbacks callbacks, GpioPinDigital pin) {
        
        this.callbacks = callbacks;
        this.pin = pin;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        pin.addListener(new GpioListener(containers, listener, pin));
    }

    @Override
    public void disconnect() {
        
        callbacks.onDisconnect(pin);
    }

    protected class GpioListener implements GpioPinListenerDigital {

        private final List<ChannelRecordContainer> containers;
        private final RecordsReceivedListener listener;

        private final GpioPinDigital pin;

        public GpioListener(List<ChannelRecordContainer> containers, RecordsReceivedListener listener, GpioPinDigital pin) {
            this.containers = containers;
            this.listener = listener;
            this.pin = pin;
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            long samplingTime = System.currentTimeMillis();

            for (ChannelRecordContainer container : containers) {
                try {
                    ChannelSettings settings = prefs.get(container.getChannelSettings(), ChannelSettings.class);
                    
                    PinState state = pin.getState();
                    Value value;
                    if (!settings.isInverted()) {
                        value = new BooleanValue(state.isHigh());
                    }
                    else {
                        value = new BooleanValue(state.isLow());
                    }
                    container.setRecord(new Record(value, samplingTime, Flag.VALID));
                    logger.debug("Received value for listened pin \"{}\": {}", pin.getName(), value);

                } catch (ArgumentSyntaxException e) {
                    logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e.getMessage());
                    container.setRecord(new Record(null, samplingTime, Flag.DRIVER_ERROR_READ_FAILURE));
                }
            }
            listener.newRecords(containers);
        }
    }
}
