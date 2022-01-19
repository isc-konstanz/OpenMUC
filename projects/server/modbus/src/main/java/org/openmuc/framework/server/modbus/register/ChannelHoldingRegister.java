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
package org.openmuc.framework.server.modbus.register;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.server.modbus.DataType;
import org.openmuc.framework.server.modbus.util.DataTypeConverter;
import org.openmuc.framework.server.modbus.util.DataTypeConverter.EndianInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

/**
 * This Class implements a linked holding register for Modbus server. The reason behind this class is to collect the
 * input over multiple registers and write into one single channnel. Therefore it is necessary to concatenate the
 * register contents.
 * 
 * Bytes are submitted from one to next register after receiving. Example: [register1] -&gt; [register2] -&gt;
 * [register3] -&gt; [register4] = (represents 64 bytes Long/Double value) 0x01 0x02 -&gt; 0x03 0x04 -&gt; 0x01 0x02
 * -&gt; 0x03 0x04
 * 
 * register1 submits 2 bytes to register2 register2 submits 4 bytes to register3 register3 submits 6 bytes to register4
 * register4 writes channel with 8 bytes value.
 * 
 * The behavior of submission is safe against the order the registers are written.
 * 
 */
public class ChannelHoldingRegister extends ChannelInputRegister implements Register {

	private final static Logger logger = LoggerFactory.getLogger(ChannelHoldingRegister.class);

    private final ChannelHoldingRegister nextRegister;
    private boolean hasLeadingRegister = false;
    private byte[] leadingBytes;
    private byte[] fromBytes;

    public ChannelHoldingRegister(Channel channel, DataType dataType, int byteHigh, int byteLow, 
    		ChannelHoldingRegister nextRegister) {
        super(channel, dataType, byteHigh, byteLow);
        
        this.nextRegister = nextRegister;
        if (nextRegister != null) {
            nextRegister.hasLeadingRegister = true;
        }
    }

    public ChannelHoldingRegister(Channel channel, DataType dataType, int byteHigh, int byteLow) {
        super(channel, dataType, byteHigh, byteLow);
        this.nextRegister = null;
    }

    public int getLowByte() {
        return highByte;
    }

    public int getHighByte() {
        return lowByte;
    }

    @Override
    public void setValue(int v) {
        setValue(ModbusUtil.unsignedShortToRegister(v));
    }

    @Override
    public void setValue(short s) {
        setValue(ModbusUtil.shortToRegister(s));
    }

    @Override
    public void setValue(byte[] bytes) {
    	// TODO: Make word endianess configurable
        this.fromBytes = DataTypeConverter.reverseByteOrder(bytes);
        if (nextRegister != null) {
            if (hasLeadingRegister) {
                if (leadingBytes != null) {
                    nextRegister.submit(concatenate(leadingBytes, fromBytes));
                }
            }
            else {
                nextRegister.submit(fromBytes);
            }
        }
        else {
            if (hasLeadingRegister) {
                if (leadingBytes != null) {
                    writeChannel(newValue(dataType, concatenate(leadingBytes, fromBytes)));
                } /* else wait for leadingBytes from submit */
            }
            else {
                writeChannel(newValue(dataType, fromBytes));
            }
        }
    }

    public static Value newValue(DataType fromType, byte[] fromBytes) throws TypeConversionException {
//    	byte[] registerBytes = DataTypeConverter.reverseByteOrder(fromBytes);
		if (logger.isDebugEnabled()) {
			StringBuilder fromBytesHex = new StringBuilder();
			for(byte b: fromBytes) {
				fromBytesHex.append(String.format("%02x", b&0xff));
			}
	    	logger.debug("Received byte values: {}}", fromBytesHex);
		}
        switch (fromType) {
        case BOOLEAN:
            if (fromBytes[0] == 0x00) {
                return new BooleanValue(false);
            }
            else {
                return new BooleanValue(true);
            }
        case SHORT:
        case INT16:
            return new ShortValue(ModbusUtil.registerToShort(fromBytes));
        case INT32:
            return new IntValue(ModbusUtil.registersToInt(fromBytes));
        case UINT16:
            return new IntValue(ModbusUtil.registerToUnsignedShort(fromBytes));
        case UINT32:
        	return new LongValue(DataTypeConverter.bytesToUnsignedInt32(fromBytes, EndianInput.BYTES_ARE_BIG_ENDIAN));
        case FLOAT:
            return new FloatValue(ModbusUtil.registersToFloat(fromBytes));
        case DOUBLE:
            return new DoubleValue(ModbusUtil.registersToDouble(fromBytes));
        case LONG:
            return new LongValue(ModbusUtil.registersToLong(fromBytes));
        case BYTE_ARRAY:
            return new ByteArrayValue(fromBytes);
        case STRING:
            return new StringValue(new String(fromBytes));
        default:
            throw new TypeConversionException("Data type " + fromType.toString() + " not supported yet");
        }
    }

    protected void submit(byte[] leading) {
        this.leadingBytes = leading;
        if (fromBytes != null) {
            if (nextRegister != null) {
                nextRegister.submit(concatenate(leadingBytes, fromBytes));
            }
            else {
                writeChannel(newValue(dataType, concatenate(leadingBytes, fromBytes)));
            }
        } /* else wait for thisRegisterContent from setValue */
    }

    private static byte[] concatenate(byte[] one, byte[] two) {
        if (one == null) {
            return two;
        }

        if (two == null) {
            return one;
        }

        byte[] combined = new byte[one.length + two.length];

        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }

        return combined;
    }

    private void writeChannel(Value value) {
    	logger.debug("Writing value: {}", value);
        if (value != null) {
            if (useUnscaledValues) {
                channel.write(new DoubleValue(value.asDouble() * channel.getScalingFactor()));
            }
            else {
                channel.write(value);
            }
        }
        else {
            channel.setLatestRecord(new Record(Flag.CANNOT_WRITE_NULL_VALUE));
        }
    }

}
