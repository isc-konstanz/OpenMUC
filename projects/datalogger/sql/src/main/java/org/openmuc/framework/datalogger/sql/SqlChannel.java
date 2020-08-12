package org.openmuc.framework.datalogger.sql;

import java.nio.charset.StandardCharsets;
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
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlChannel extends SqlConfigs {
    private static final Logger logger = LoggerFactory.getLogger(SqlChannel.class);

    static int TYPE_LENGTH_DEFAULT = 10;
    static String TYPE_INDEX_DEFAULT = "INT UNSIGNED";
    static String TYPE_DATA_DEFAULT = "FLOAT";
    static String TYPE_NOT_NULL = "NOT NULL";
    static String[] TYPES = new String[] {
        "FLOAT",
        "REAL",
        "BIGINT",
        "INT",
        "SMALLINT",
        "TINYINT",
        "BIT",
        "VARBINARY",
        "VARCHAR"
    };

    @Address(mandatory = false)
    protected String dataColumn = "data";

    @Setting(mandatory = false)
    protected String keyColumn = "key";

    @Setting(mandatory = false)
    protected String key = null;

    public String getDataColumn() {
        return dataColumn;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public String getKey() {
        return key;
    }

    public Record decode(ResultSet result) {
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

    public String encodeTimestamp(long fallback) {
        Long timestamp = getRecord().getTimestamp();
        if (timestamp == null) {
            timestamp = fallback;
        }
        return index.encode(timestamp);
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
