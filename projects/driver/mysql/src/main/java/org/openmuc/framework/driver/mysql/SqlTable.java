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
package org.openmuc.framework.driver.mysql;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SqlTable {
    private static final Logger logger = LoggerFactory.getLogger(SqlTable.class);

	protected final List<SqlChannel> channels = new LinkedList<SqlChannel>();

	protected final Index index;

	public SqlTable(Index index) {
		this.index = index;
	}

    public abstract void read(Connection connection) throws SQLException;

    public abstract void write(Transaction transaction) throws SQLException;

	protected Record decodeRecord(ResultSet result, String column, ValueType type) {
        String valueStr;
		try {
			valueStr = result.getString(column).replaceAll("\"", "");
	        try {
	            Value value = decodeValue(valueStr, type);
	            long time = index.decode(result);
	            
	    		return new Record(value, time, Flag.VALID);
	        
	        } catch(NullPointerException  | IllegalArgumentException e) {
	        	logger.warn("Error decoding column {} ({}): {}", column, type, valueStr);
	        }
		} catch (SQLException e) {
        	logger.warn("Error reading column {} ({}): {}", column, type, e.getMessage());
		}
        return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
	}

	protected Value decodeValue(String value, ValueType type) throws NullPointerException, IllegalArgumentException {
        switch(type) {
        case DOUBLE:
            return new DoubleValue(Double.valueOf(value));
        case FLOAT:
            return new FloatValue(Float.valueOf(value));
        case INTEGER:
            return new IntValue(Integer.valueOf(value));
        case LONG:
            return new LongValue(Long.valueOf(value));
        case SHORT:
            return new ShortValue(Short.valueOf(value));
        case BYTE:
            return new ByteValue(Byte.valueOf(value));
        case BOOLEAN:
            return new BooleanValue(Boolean.valueOf(value));
        case BYTE_ARRAY:
            byte[] arr;
            if (!value.startsWith("0x")) {
                arr = value.getBytes(StandardCharsets.US_ASCII);
            }
            else {
                arr = hexToBytes(value.substring(2).trim());
            }
            return new ByteArrayValue(arr);
        default:
            return new StringValue(value);
        }
	}

    protected String encodeValue(Value value) {
    	return value.asString();
    }

    static byte[] hexToBytes(String s) {
        byte[] b = new byte[s.length() / 2];
        int index;

        for (int i = 0; i < b.length; i++) {
            index = i * 2;
            b[i] = (byte) Integer.parseInt(s.substring(index, index + 2), 16);
        }
        return b;
    }

}
