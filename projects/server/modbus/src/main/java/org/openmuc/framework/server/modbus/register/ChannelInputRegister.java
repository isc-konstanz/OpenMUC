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

import java.nio.ByteBuffer;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.server.modbus.DataType;
import org.openmuc.framework.server.modbus.util.DataTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

public class ChannelInputRegister implements InputRegister {

    private final static Logger logger = LoggerFactory.getLogger(ChannelInputRegister.class);

    boolean useUnscaledValues = false;

    protected final Channel channel;

    protected final DataType dataType;
    protected final int highByte;
    protected final int lowByte;

    public ChannelInputRegister(Channel channel, DataType dataType, int byteHigh, int byteLow) {
        this.channel = channel;
        this.dataType = dataType;
        this.highByte = byteHigh;
        this.lowByte = byteLow;
        try {
            String useUnscaledProperty = System.getProperty("org.openmuc.framework.server.modbus.useUnscaledValues");
            this.useUnscaledValues = Boolean.parseBoolean(useUnscaledProperty);
            
        } catch (Exception e) {
            /* will stick to default setting. */
        }
    }

    public int getLowByte() {
        return lowByte;
    }

    public int getHighByte() {
        return highByte;
    }

    private Value getChannelValue() throws NullPointerException {
        Record record = channel.getLatestRecord();
        if (!record.isValid()) {
            throw new NullPointerException("Channel record is invalid: " + record);
        }
        Value value = channel.getLatestRecord().getValue();
        
        if (useUnscaledValues) {
            switch (dataType) {
            case SHORT:
            case INT16:
                return new ShortValue((short) (value.asShort() / (short) channel.getScalingFactor()));
            case UINT16:
            case INTEGER:
            case INT32:
                    return new IntValue(value.asInt() / (int) channel.getScalingFactor());
            case UINT32:
            case LONG:
                return new LongValue(value.asLong() / (long) channel.getScalingFactor());
            case FLOAT:
                return new FloatValue(value.asFloat() / (float) channel.getScalingFactor());
            case DOUBLE:
                return new DoubleValue(value.asDouble() / channel.getScalingFactor());
            default:
                // Do nothing
            }
        }
        return value;
    }

    @Override
    public int getValue() {
        /*
         * toBytes always only contains two bytes. So cast from short.
         */
        return ByteBuffer.wrap(toBytes()).getShort();
    }

    @Override
    public int toUnsignedShort() {
        short shortVal = ByteBuffer.wrap(toBytes()).getShort();
        return shortVal & 0xFFFF;
    }

    @Override
    public short toShort() {
        return ByteBuffer.wrap(toBytes()).getShort();
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes;
        try {
            Value value = getChannelValue();
            switch (dataType) {
            case BOOLEAN:
                if (value.asBoolean()) {
                    bytes = new byte[] { 0x01b };
                }
                else {
                    bytes = new byte[] { 0x00b };
                }
                break;
            case SHORT:
            case INT16:
                bytes = ModbusUtil.shortToRegister(value.asShort());
                break;
            case UINT16:
                bytes = DataTypeConverter.unsingedInt16ToBytes(value.asInt(),
                        DataTypeConverter.EndianOutput.BYTES_AS_LITTLE_ENDIAN);
//                        DataTypeConverter.EndianOutput.BYTES_AS_BIG_ENDIAN);
                break;
            case INTEGER:
            case INT32:
                bytes = ModbusUtil.intToRegisters(value.asInt());
                break;
            case UINT32:
                bytes = DataTypeConverter.unsingedInt32ToBytes(value.asLong(),
                        DataTypeConverter.EndianOutput.BYTES_AS_LITTLE_ENDIAN);
//                        DataTypeConverter.EndianOutput.BYTES_AS_BIG_ENDIAN);
                break;
            case FLOAT:
                bytes = ModbusUtil.floatToRegisters(value.asFloat());
                break;
            case LONG:
                bytes = ModbusUtil.longToRegisters(value.asLong());
                break;
            case DOUBLE:
                bytes = ModbusUtil.doubleToRegisters(value.asDouble());
                break;
            case BYTE_ARRAY:
            case STRING:
                bytes = value.asByteArray();
                break;
            default:
                throw new RuntimeException("Data type " + dataType.toString() + " not supported yet");
            }
        } catch (NullPointerException e) {
            logger.warn("Error parsing channel value bytes: " + e.getMessage());
            return new byte[] { 0x0, 0x0 };
        }
        return new byte[] { bytes[getHighByte()], bytes[getLowByte()] };
    }

}
