package org.openmuc.framework.datalogger.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(MysqlChannelHandler.class);
    static int DEFAULT_MAX_ARRAY_LENGTH = 10;
	static String ceateTableSqlCommand = "CREATE TABLE IF NOT EXISTS %s (time BIGINT UNSIGNED NOT NULL, data %s, flag TINYINT, PRIMARY KEY (time)) ENGINE=MYISAM";
	static String insertSqlValueCommand = "INSERT INTO %s (time, data, flag) VALUES(%s,%s, %s)";
	static String insertSqlFlagCommand = "INSERT INTO %s (time, flag) VALUES(%s,%s)";
	static String selectSqlCommand = "SELECT * FROM %s WHERE time >= %s AND time <= %s";
	static String TIME_COLUMN_NAME = "time";
	static String DATA_COLUMN_NAME = "data";
	static String FLAG_COLUMN_NAME = "flag";
	
    private String dbAddress;
	private String userName;
	private String password;
	
	LogChannel logChannel;

	public MysqlChannelHandler(String dbAddress, String userName, String password, LogChannel logChannel) {
		this.dbAddress = dbAddress;
		this.userName = userName;
		this.password = password;
		
		this.logChannel = logChannel;
		
		createChannelTable();
	}
	
	protected void createChannelTable() {		
		String createTableCommand = getCreateTableSqlCommand();	
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = connect();
		    stmt = connection.createStatement();
		    stmt.execute(createTableCommand);
		    
		    close(connection, stmt);		    
		} 
		catch (SQLException e) {
		    close(connection, stmt);		    
			logger.debug("ChannelID (" + logChannel.getId() + ")  SqlException occured: " + e.getMessage());
		}
	}
	
	protected String getCreateTableSqlCommand() {
		if (logChannel == null) {
			return String.format(ceateTableSqlCommand, "test", "VARCHAR(" + DEFAULT_MAX_ARRAY_LENGTH + ")");
		}
		return String.format(ceateTableSqlCommand, getTableName(), 
				convert2SqlTypes(logChannel.getValueType().toString(),
						logChannel.getValueTypeLength()));
	}
	
	protected String getTableName() {
		return logChannel.getId();
	}

	public static Object convert2SqlTypes(String string, Integer valueTypeLength) {
		switch (string) {
			case "FLOAT":
				return "REAL";
			case "INTEGER":
				return "INT";
			case "LONG":
				return "BIGINT";
			case "SHORT":
				return "SMALLINT";
			case "BYTE":
				return "TINYINT";
			case "BOOLEAN":
				return "BIT";
			case "BYTE_ARRAY":
				int maxBytesLength = DEFAULT_MAX_ARRAY_LENGTH;
				if (valueTypeLength != null) {
					maxBytesLength = valueTypeLength;
				}
				return "VARBINARY(" + maxBytesLength + ")";
			case "STRING":
				int maxStrLength = DEFAULT_MAX_ARRAY_LENGTH;
				if (valueTypeLength != null) {
					maxStrLength = valueTypeLength;
				}
				return "VARCHAR(" + maxStrLength + ")";
		}
        return string;
	}

	private Connection connect() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		Connection connection = DriverManager.getConnection(dbAddress, userName, password);
		return  connection;
	}

	public void log(LogRecordContainer container) throws SQLException {
		String insertCommand = "";
        Record record = container.getRecord();
        if (record != null) {
            Value recordValue = record.getValue();
            
            if (record.getFlag().equals(Flag.NO_VALUE_RECEIVED_YET)) {
            	logger.debug("Container " + container.getChannelId() + ": No Value received yet");
            	return;
            }
            
            if (record.getTimestamp() == null) {
            	logger.debug("Container " + container.getChannelId() + ": Timestamp null of record is not allowed");
            	return;
            }
            else {
	            String time = record.getTimestamp().toString();
	            String flag = ((Byte)record.getFlag().getCode()).toString();
	            if (record.getFlag() == Flag.VALID) {
	                if (recordValue == null) {
	                	logger.debug("Container " + container.getChannelId() + ": Value null of record with valid flag is not allowed");
	                	return;
	                }
	                else {
	                	insertCommand = getInsertValueCommand(time, recordValue, flag);
	                }
	            }
	            else {
	                // write error flag
	                if (recordValue == null) {
			        	insertCommand = getInsertSqlFlagCommand(time, 
			        			flag);
	                }
	                else {
	                	// TODO check if the statement is correct in the other case don't set the value
	                	insertCommand = getInsertSqlValueCommand(time, 
	                			recordValue.toString(), flag);                    		                	
	                }
	            }
	        }
        }
        else {
        	logger.debug("Container " + container.getChannelId() + ": Record null is not allowed!");
        	return;
        }
        
		Connection connection = connect();
	    Statement stmt = connection.createStatement();
	    stmt.execute(insertCommand);
	    close(connection, stmt);		
	}

	private String getInsertValueCommand(String time, Value recordValue, String flag) {
		String value = null;
		String channelId = logChannel.getId();
		
        switch (logChannel.getValueType()) {
        case BOOLEAN:
        	value = String.valueOf(recordValue.asShort());
            break;
        case LONG:
        	value = String.valueOf(recordValue.asLong());
            break;
        case INTEGER:
        	value = String.valueOf(recordValue.asInt());
            break;
        case SHORT:
        	value = String.valueOf(recordValue.asShort());
            break;
        case DOUBLE:
        	value = String.valueOf(recordValue.asDouble());
            break;
        case FLOAT:
        	value = String.valueOf(recordValue.asFloat());
            break;
        case BYTE_ARRAY:
            byte[] byteArray = recordValue.asByteArray();
            if (byteArray.length > 256) {
            	String insertCommand = getInsertSqlFlagCommand(time, 
            			Flag.UNKNOWN_ERROR.toString());
                logger.error("The byte array is too big, length is ", byteArray.length,
                        " but max. length allowed is 256, ChannelId: ", channelId);
                return insertCommand;
            }
            else {
            	//TODO Check this conversion
            	value = String.valueOf(byteArray);
            }
            break;
        case STRING:
        	String str = recordValue.asString();
        	if (str.length() > 256) {
            	String insertCommand = getInsertSqlFlagCommand(time, 
            			Flag.UNKNOWN_ERROR.toString());
                logger.error("The string is too big, length is ", str.length(),
                        " but max. length allowed is 256, ChannelId: ", channelId);
                return insertCommand;
        	}
        	else {
        		value = recordValue.asString();		
        	}
            break;
        case BYTE:
        	value = String.format("0x%02x", recordValue.asByte());                    		
            break;
        default:
            throw new RuntimeException("unsupported valueType");
        }
		
        String insertCommand = getInsertSqlValueCommand(time, value, flag);
		return insertCommand;
	}

	private String getInsertSqlValueCommand(String time, String value, String flag) {
		return String.format(insertSqlValueCommand, getTableName(), time, value, flag);
	}
	
	private String getInsertSqlFlagCommand(String time, String flag) {
		return String.format(insertSqlFlagCommand, getTableName(), time, flag);
	}

	public List<Record> getRecords(String channelId, long startTime, long endTime) throws SQLException {
		List<Record>recordList = null;
		
		String selectCommand = String.format(selectSqlCommand, channelId, startTime, endTime);
		Connection connection = connect();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(selectCommand);

		boolean first = true;
		while (rs.next()) {
	    	long time = rs.getLong(TIME_COLUMN_NAME);
	    	int intFlag = rs.getInt(FLAG_COLUMN_NAME);
	    	Flag flag = Flag.newFlag(intFlag);
	    	Record rec = null;
	    	if (flag.equals(Flag.VALID)) {
		    	Value val = getValue(rs.getObject(DATA_COLUMN_NAME));
		    	rec = new Record(val, time);
	    	}
	    	else {
	    		rec = new Record(flag);
	    	}
		    	
	    	if (first) {
	    		recordList = new ArrayList<Record>();
	    		first = false;
	    	}
	    	recordList.add(rec);
	    }
		
	    close(connection, stmt);
		
	    return recordList;
	}
	
	private Value getValue(Object obj) {
		Value val = null;
		ValueType type = logChannel.getValueType();

		if (type == ValueType.BOOLEAN && obj instanceof Boolean) {
			val = new BooleanValue((Boolean)obj);
		}
		else if (type == ValueType.LONG && obj instanceof Long) {
			val = new LongValue((Long)obj);
		}
		else if (type == ValueType.INTEGER && obj instanceof Integer) {
			val = new IntValue((Integer)obj);
		}
		else if (type == ValueType.SHORT && obj instanceof Short) {
			val = new ShortValue((Short)obj);
		}
		else if (type == ValueType.DOUBLE && obj instanceof Double) {
			val = new DoubleValue((Double)obj);
		}
		else if (type == ValueType.FLOAT && obj instanceof Float) {
			val = new FloatValue((Float)obj);
		}
		else if (type == ValueType.BYTE_ARRAY && obj instanceof byte[]) {
			val = new ByteArrayValue((byte[])obj);
		}
		else if (type == ValueType.BYTE && obj instanceof Byte) {
			val = new ByteValue((Byte)obj);
		}
		else if (type == ValueType.STRING) {
			val = new StringValue(obj.toString());
		}
		else {
			throw new RuntimeException("unsupported valueType");
		}
		
		return val;
	}

	private void close(Connection connection, Statement stmt) {
	    try {
	    	if (stmt != null) {
	    		stmt.close();
	    	}
	    	if (connection != null) {
	    		connection.close();
	    	}
	    }
	    catch (SQLException ignore) {}
	}
	
	public static void main(String[] args){ 
		new MysqlChannelHandler("jdbc:mysql://127.0.0.1:3306/emoncms", "emoncms", "emoncms", null);
	}
	
}
