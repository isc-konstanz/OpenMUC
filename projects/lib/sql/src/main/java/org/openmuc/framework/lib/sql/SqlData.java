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

package org.openmuc.framework.lib.sql;

import java.nio.charset.StandardCharsets;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.slf4j.LoggerFactory;

public interface SqlData {

    public static final String POSTGRESQL = "postgresql";
    public static final String POSTGRES = "postgres";
    public static final String MYSQL = "mysql";

    public static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public default JDBCType getType() {
        return getType(getValueType());
    }

    public static JDBCType getType(ValueType valueType) {
        switch (valueType) {
        case BOOLEAN:
            return JDBCType.BOOLEAN;
        case BYTE_ARRAY:
            return JDBCType.LONGVARBINARY;
        case DOUBLE:
            return JDBCType.FLOAT;
        case FLOAT:
            return JDBCType.DOUBLE;
        case INTEGER:
            return JDBCType.INTEGER;
        case LONG:
            return JDBCType.BIGINT;
        case SHORT:
            return JDBCType.SMALLINT;
        case BYTE:
            return JDBCType.SMALLINT;
        case STRING:
            return JDBCType.VARCHAR;
        default:
            return JDBCType.DOUBLE;
        }
    }

    public ValueType getValueType();

    public int getValueTypeLength();

    public String getValueColumn();

    public String getKeyColumn();

    public String getKey();

    public default boolean hasKey() {
        return getKey() != null && 
                !getKey().isEmpty();
    }

    public String getTable();

    public TableType getTableType();

    public default boolean isValid() {
    	Record record = getRecord();
        if (record != null && record.getFlag() == Flag.VALID && record.getValue() != null) {
            return true;
        }
        return false;
    }

    public Record getRecord();

    public default void setRecord(Record record) { throw new UnsupportedOperationException(); }

    public default Record decodeRecord(Index index, ResultSet result) {
        try {
        	String valueStr = result.getString(getValueColumn()).replaceAll("\"", "");
	        try {
	        	Value value;
	        	long timestamp = index.decode(result);
	        	
	            switch(getValueType()) {
	            case DOUBLE:
	                value = new DoubleValue(Double.valueOf(valueStr));
	            case FLOAT:
	                value = new FloatValue(Float.valueOf(valueStr));
	            case INTEGER:
	                value = new IntValue(Integer.valueOf(valueStr));
	            case LONG:
	                value = new LongValue(Long.valueOf(valueStr));
	            case SHORT:
	                value = new ShortValue(Short.valueOf(valueStr));
	            case BYTE:
	                value = new ByteValue(Byte.valueOf(valueStr));
	            case BOOLEAN:
	                value = new BooleanValue(Boolean.valueOf(valueStr));
	            case BYTE_ARRAY:
	                byte[] arr;
	                if (!valueStr.startsWith("0x")) {
	                    arr = valueStr.getBytes(StandardCharsets.US_ASCII);
	                }
	                else {
	                    arr = hexStringToByteArray(valueStr.substring(2).trim());
	                }
	                value = new ByteArrayValue(arr);
	            default:
	                value = new StringValue(valueStr);
	            }
	            return new Record(value, timestamp, Flag.VALID);
	        
	        } catch(NullPointerException  | IllegalArgumentException | ParseException e) {
	            LoggerFactory.getLogger(getClass())
	                         .warn("Error decoding column {} ({}): {}", getValueColumn(), getType(), valueStr);
	        }
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass())
                         .warn("Error reading column {} ({}): {}", getValueColumn(), getType(), e.getMessage());
        }
        return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
    }

    public default StringBuilder encodeTimestamp(StringBuilder sb, Index index, long fallback) {
        return appendTimestamp(sb, index, getRecord().getTimestamp(), fallback);
    }

    public static StringBuilder appendTimestamp(StringBuilder sb, Index index, Long timestamp, long fallback) {
    	if (timestamp == null) {
    		timestamp = fallback;
    	}
    	sb.append(index.encode(timestamp));
    	
        return sb;
    }

    public default StringBuilder encodeValue(StringBuilder sb) {
        return appendValue(sb, getRecord().getValue());
    }

    public static StringBuilder appendValue(StringBuilder sb, Value value) {
        
        switch (value.getClass().getSimpleName()) {
        case "BooleanValue":
            sb.append(value.asBoolean());
            break;
        case "ByteValue":
            sb.append(value.asByte());
            break;
        case "ByteArrayValue":
            byteArrayToHexString(sb, value.asByteArray());
            break;
        case "DoubleValue":
            sb.append(value.asDouble());
            break;
        case "FloatValue":
            sb.append(value.asFloat());
            break;
        case "IntValue":
            sb.append(value.asInt());
            break;
        case "LongValue":
            sb.append(value.asLong());
            break;
        case "ShortValue":
            sb.append(value.asShort());
            break;
        case "StringValue":
            sb.append('\'').append(value.asString()).append('\'');
            break;
        default:
            break;
        }
        return sb;
    }

    static StringBuilder byteArrayToHexString(StringBuilder sb, byte[] byteArray) {
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = HEX[v >>> 4];
            hexChars[j * 2 + 1] = HEX[v & 0x0F];
        }
        sb.append('\'').append(hexChars).append('\'');
        
        return sb;
    }

    static byte[] hexStringToByteArray(String str) {
        byte[] b = new byte[str.length() / 2];
        int index;

        for (int i = 0; i < b.length; i++) {
            index = i * 2;
            b[i] = (byte) Integer.parseInt(str.substring(index, index + 2), 16);
        }
        return b;
    }

}
