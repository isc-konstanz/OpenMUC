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
package org.openmuc.framework.driver.sql;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.Syntax;
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
import org.openmuc.framework.driver.DriverChannel;
import org.openmuc.framework.driver.annotation.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Syntax(separator = ";", assignment = "=", keyValuePairs = { ADDRESS, SETTING })
public class SqlChannel extends DriverChannel {
    private static final Logger logger = LoggerFactory.getLogger(SqlChannel.class);

    @Option(type = ADDRESS,
            name = "Table name",
            description = "Tablename to read columns from.<br>" +
                          "Will override the configured table name of the connection.",
            mandatory = false)
    protected String table = null;

    @Option(type = ADDRESS,
            name = "Key",
            description = "The unique key identifying the series.<br>" +
                    "<i>Only necessary for unnormalized tables.</i>",
            mandatory = false)
    protected String key = null;

    @Option(type = ADDRESS,
            name = "Key column",
            description = "The column name of the table containing the unique key identifying the series.<br>" +
                          "<i>Only necessary for unnormalized tables</i>.",
            mandatory = false)
    protected String keyColumn = "key";

    @Option(type = ADDRESS,
            name = "Data column",
            description = "The column name of the table containing the value data.",
            mandatory = false)
    protected String dataColumn = "data";

    @Configure
    public void setTable(SqlClient client) {
        if (table == null) {
            table = client.getTable();
        }
        if (table == null) {
            table = getId().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        }
    }

    public String getTable() {
        return table;
    }

    public String getKey() {
        return key;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public String getDataColumn() {
        return dataColumn;
    }

    public Record decode(ResultSet result, Index index) {
        String valueStr;
        try {
            valueStr = result.getString(getDataColumn()).replaceAll("\"", "");
            try {
                Value value = decode(valueStr);
                long time = index.decode(result);
                
                return new Record(value, time, Flag.VALID);
            
            } catch(NullPointerException  | IllegalArgumentException | ParseException e) {
                logger.warn("Error decoding column {} ({}): {}", getDataColumn(), getValueType(), valueStr);
            }
        } catch (SQLException e) {
            logger.warn("Error reading column {} ({}): {}", getDataColumn(), getValueType(), e.getMessage());
        }
        return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
    }

    public Value decode(String value) throws NullPointerException, IllegalArgumentException {
        switch(getValueType()) {
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

    public String encodeValue() {
        return getRecord().getValue().asString();
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
