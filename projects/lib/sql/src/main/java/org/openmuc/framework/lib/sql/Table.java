package org.openmuc.framework.lib.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Table {
    private static final Logger logger = LoggerFactory.getLogger(Table.class);

    public static final List<String> COLUMNS = Arrays.asList("channelid", "channelAdress", "loggingInterval",
            "loggingTimeOffset", "unit", "valueType", "scalingFactor", "valueOffset", "listening", "loggingEvent",
            "samplingInterval", "samplingTimeOffset", "samplingGroup", "disabled", "description");

    public static final String STRING_VALUE = "StringValue";
    public static final String SHORT_VALUE = "ShortValue";
    public static final String BYTE_VALUE = "ByteValue";
    public static final String LONG_VALUE = "LongValue";
    public static final String INT_VALUE = "IntValue";
    public static final String DOUBLE_VALUE = "DoubleValue";
    public static final String FLOAT_VALUE = "FloatValue";
    public static final String BYTE_ARRAY_VALUE = "ByteArrayValue";
    public static final String BOOLEAN_VALUE = "BooleanValue";

    public static final String NULL = ") NULL,";
    public static final String AND = "' AND '";
    public static final String VALUE = "value";

    public static int TYPE_LENGTH_DEFAULT = 10;
    public static String TYPE_INDEX_DEFAULT = "INT UNSIGNED";
    public static String TYPE_DATA_DEFAULT = "FLOAT";
    public static String TYPE_NOT_NULL = "NOT NULL";
    public static String[] TYPES = new String[] {
        "FLOAT",
        "DOUBLE",
        "BIGINT",
        "INTEGER",
        "SMALLINT",
        "BOOLEAN",
        "LONGVARBINARY",
        "VARCHAR"
    };

    protected final Index index;

    public Table(Index index) {
    	this.index = index;
    }

    public abstract TableType getType();

    public abstract String getName();

    public Index getIndex() {
        return index;
    }

    public boolean create(Connection connection, List<SqlData> dataList) 
    		throws UnsupportedOperationException, ArgumentSyntaxException, SQLException {
    	throw new UnsupportedOperationException("Table creation not supported for type " + getType());
    }

    protected boolean create(Connection connection, String query) 
            throws SQLException {
        
        logger.debug("Querying \"{}\"", query);
        try (Statement statement = connection.createStatement()) {
             return statement.execute(query);
            
        } catch (SQLException e) {
            logger.warn("Error creating table \"{}\": {}", query, e.getMessage());
        }
        return false;
    }

    public abstract List<Record> read(Connection connection, SqlData data, long startTime, long endTime) 
            throws SQLException, ArgumentSyntaxException;

    protected List<Record> read(Connection connection, SqlData data, String query) 
            throws SQLException {
        
        List<Record> records = new LinkedList<Record>();
        
        logger.debug("Querying \"{}\"", query);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(query)) {
                while (result.next()) {
                    Record record = data.decodeRecord(index, result);
                    records.add(record);
                }
            } catch (SQLException e) {
                logger.warn("Error querying \"{}\": {}", query, e.getMessage());
            }
        }
        return records;
    }

    public abstract void read(Connection connection, List<SqlData> dataList) 
            throws SQLException, ArgumentSyntaxException;

    protected void read(Connection connection, List<SqlData> dataList, String query) 
            throws SQLException {
        
        logger.debug("Querying \"{}\"", query);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(query.toString())) {
                if (result.first()) {
                    for (SqlData data : dataList) {
                        Record record = data.decodeRecord(index, result);
                        data.setRecord(record);
                    }
                }
            } catch (SQLException e) {
                for (SqlData data : dataList) {
                    data.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                }
                logger.warn("Error querying \"{}\": {}", query, e.getMessage());
            }
        }
    }

    public abstract void write(Statement statement, List<SqlData> dataList, long timestamp) 
            throws SQLException, ArgumentSyntaxException;

    protected void write(Statement statement, String query) 
            throws SQLException {
        
        logger.debug("Querying {}", query);
        
        statement.execute(query.toString());
    }

    protected void appendInsert(StringBuilder query, List<SqlData> dataList) 
            throws ArgumentSyntaxException {

        query.append("INSERT INTO ");
        query.append(getName());
        
        query.append(" (");
        query.append(getIndex().getColumn());
        for (SqlData data : dataList) {
            query.append(",");
            query.append(data.getValueColumn());
        }
        query.append(") ");
    }

    protected void appendValues(StringBuilder query, List<SqlData> dataList, long timestamp) 
            throws ArgumentSyntaxException {
        
        query.append("VALUES ");
        query.append("(");
        for (int i=0; i<dataList.size(); i++) {
            SqlData data = dataList.get(i);
            if (i == 0) {
                data.encodeTimestamp(query, index, timestamp);
            }
            query.append(",");
            
            data.encodeValue(query);
        }
        query.append(") ");
    }

    protected void appendUpdate(StringBuilder query, List<SqlData> dataList) {
        query.append("ON DUPLICATE KEY UPDATE");
        for (int i=0; i<dataList.size(); i++) {
            SqlData data = dataList.get(i);
            if (i > 0) {
                query.append(",");
            }
            query.append(MessageFormat.format(" {0} = VALUES({0})", data.getValueColumn()));
        }
    }

    protected void appendSelect(StringBuilder query, List<SqlData> dataList) 
            throws ArgumentSyntaxException {
        
        query.append("SELECT ");
        query.append(getIndex().getColumn());
        for (SqlData data : dataList) {
            query.append(",");
            query.append(data.getValueColumn());
        }
        appendFrom(query);
    }

    protected void appendSelect(StringBuilder query, SqlData data) 
            throws ArgumentSyntaxException {
        
        query.append("SELECT ");
        query.append(getIndex().getColumn());
        query.append(",");
        query.append(data.getValueColumn());
        
        appendFrom(query);
    }

    private void appendFrom(StringBuilder query) {
        query.append(" FROM ");
        query.append(getName());
        query.append(" ");
    }

    protected void appendLatest(StringBuilder query) {
        query.append(getIndex().queryLatest());
    }

    protected void appendWhere(StringBuilder query, long startTime, long endTime) {
        query.append(getIndex().queryWhere(startTime, endTime));
    }

}
