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
package org.openmuc.framework.driver.rpi.gpio;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.rpi.gpio.options.GpioChannelPreferences;
import org.openmuc.framework.driver.rpi.gpio.options.GpioDriverInfo;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@Component
public class GpioConnection implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(GpioConnection.class);
    private final GpioDriverInfo info = GpioDriverInfo.getInfo();
    
    /**
     * Interface used by {@link GpioConnection} to notify the {@link GpioDriver} about events
     */
    public interface GpioConnectionCallbacks {
        
        public void onDisconnect(GpioPin pin);
    }

    /**
     * The Connections current callback object, which is used to notify of connection events
     */
    private final GpioConnectionCallbacks callbacks;

    private final GpioPinDigital pin;

    public GpioConnection(GpioConnectionCallbacks callbacks, GpioPinDigital pin) {
        
        this.callbacks = callbacks;
        this.pin = pin;
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

        for (ChannelRecordContainer container : containers) {
            try {
                GpioChannelPreferences prefs = info.getChannelPreferences(container);

                PinState state = pin.getState();
                Value value;
                if (!prefs.isInverted()) {
                    value = new BooleanValue(state.isHigh());
                }
                else {
                    value = new BooleanValue(state.isLow());
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
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        pin.addListener(new GpioListener(containers, listener, pin));
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        
        for (ChannelValueContainer container : containers) {
            Value value = container.getValue();
            try {
                GpioChannelPreferences prefs = info.getChannelPreferences(container);
                
                if (value != null) {
                    if (pin instanceof GpioPinDigitalOutput) {
                        logger.debug("Write value to GPIO pin {}: {}", pin.getName(), value);

                        PinState state;
                        if (!prefs.isInverted()) {
                            state = PinState.getState(value.asBoolean());
                        }
                        else {
                            state = PinState.getState(!value.asBoolean());
                        }
                        ((GpioPinDigitalOutput) pin).setState(state);
                    }
                    else {
                        throw new UnsupportedOperationException("GPIO pin "+ pin.getName() +" was provisioned as input and cannot be written to");
                    }

                    container.setFlag(Flag.VALID);
                }
                else {
                    logger.warn("No value received to write to GPIO pin {}", pin.getName());
                }
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel address \"{}\": {}", container.getChannelAddress(), e.getMessage());
            }
        }
        
        return null;
    }

    @Override
    public void disconnect() {
        
        callbacks.onDisconnect(pin);
    }

    private class GpioListener implements GpioPinListenerDigital {

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
                    GpioChannelPreferences prefs = info.getChannelPreferences(container);

                    PinState state = pin.getState();
                    Value value;
                    if (!prefs.isInverted()) {
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
