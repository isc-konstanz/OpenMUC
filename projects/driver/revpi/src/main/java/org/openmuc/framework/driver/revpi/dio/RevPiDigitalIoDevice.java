/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.driver.revpi.dio;

import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.io.IOException;
import java.util.List;

import org.clehne.revpi.dataio.DataInOut;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.DriverDevice;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.annotation.Device;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.annotation.Write;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Device(channel = RevPiDigitalIoChannel.class)
public class RevPiDigitalIoDevice extends DriverDevice {
    private final static Logger logger = LoggerFactory.getLogger(RevPiDigitalIoDevice.class);

    public static enum RevPiDigitalIoMode {
    	DIGITAL_INPUT,
    	DIGITAL_OUTPUT;
    }

    @Option(id = "mode",
            type = SETTING,
            name = "I/O mode",
            valueSelection = "DIGITAL_INPUT:Input,DIGITAL_OUTPUT:Output"
    )
    private RevPiDigitalIoMode mode;

    public RevPiDigitalIoMode getMode() {
        return mode;
    }

	protected DataInOut data;

    @Configure
    public void configure(RevPiDriver driver) throws ArgumentSyntaxException {
        logger.info("Configuring Revolution Pi {}", mode.toString().toLowerCase().replace("_", " "));
    	data = driver.data;
    }

//	private void setAllOutput(boolean setOn) {
//		for (BooleanWriteChannel ch : this.channelOut) {
//			try {
//				ch.setNextWriteValue(setOn);
//			} catch (OpenemsNamedException e) {
//				// ignore
//			}
//		}
//	}
//
//	private void readOutputFromHardwareOnce() {
//		// read all digital out pins also, because pins have already been initialized
//		// from outside
//		for (var idx = 0; idx < this.channelOut.length; idx++) {
//			try {
//				var in = this.revPiHardware.getDataOut(idx + 1);
//				this.channelOut[idx].setNextWriteValue(in);
//			} catch (Exception e) {
//				this.logError(this.log, "Unable to update channel values ex: " + e.getMessage());
//				this.channelOut[idx].setNextValue(INVALIDATE_CHANNEL);
//			}
//		}
//	}

    @Read
    public void read(List<RevPiDigitalIoChannel> channels, String samplingGroup)
            throws ConnectionException {
        
        long samplingTime = System.currentTimeMillis();
        
        for (RevPiDigitalIoChannel channel : channels) {
			try {
	            Value stateValue;
				boolean state;
				switch (mode) {
				case DIGITAL_OUTPUT:
					state = data.getDataOut(channel.getAddress());
					break;
				case DIGITAL_INPUT:
				default:
					state = data.getDataIn(channel.getAddress());
					break;
				}
	            if (!channel.isInverted()) {
	                stateValue = new BooleanValue(state);
	            }
	            else {
	                stateValue = new BooleanValue(!state);
	            }
	            channel.setRecord(new Record(stateValue, samplingTime, Flag.VALID));
				
			} catch (IOException e) {
				logger.debug("Error reading DIO channel {}: {}", channel.getAddress(), e.getMessage());
	            channel.setFlag(Flag.DRIVER_ERROR_READ_FAILURE);
			}
        }
    }

    @Write
    public void write(List<RevPiDigitalIoChannel> channels) throws ConnectionException {
        for (RevPiDigitalIoChannel channel : channels) {
            Record record = channel.getRecord();
            Value value;
            if (record.isValid()) {
                value = record.getValue();
                logger.debug("Write value to output channel {}: {}", channel.getAddress(), value);
                try {
    				switch (mode) {
    				case DIGITAL_OUTPUT:
                        if (!channel.isInverted()) {
                            data.setDataOut(channel.getAddress(), value.asBoolean());
                        }
                        else {
                            data.setDataOut(channel.getAddress(), !value.asBoolean());
                        }
                        channel.setFlag(Flag.VALID);
    					break;
    				case DIGITAL_INPUT:
    				default:
    					logger.warn("Unable  DIO mode: {}", mode);
    		            channel.setFlag(Flag.ACCESS_METHOD_NOT_SUPPORTED);
    					break;
    				}
				} catch (IOException e) {
					logger.debug("Error writing to DIO channel {}: {}", channel.getAddress(), e.getMessage());
		            channel.setFlag(Flag.DRIVER_ERROR_UNSPECIFIED);
				}
            }
            else {
                logger.warn("No value received to write to DIO channel {}", channel.getAddress());
            }
        }
    }

}
