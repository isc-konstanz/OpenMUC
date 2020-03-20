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
package org.openmuc.framework.datalogger.sql;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlClient {
    private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

    private final String database;
    private final String databaseName;
    private final List<String> tables = new ArrayList<String>();

    private final ComboPooledDataSource source;

    public SqlClient(SqlConfigs configs) throws IOException {
        logger.info("Connecting to SQL database \"{}\"", configs.getDatabase());
        
        database = configs.getDatabase();
        databaseName = configs.getDatabaseName();
        try {
            source = new ComboPooledDataSource();
            source.setDriverClass(configs.getDatabaseDriver());
            source.setJdbcUrl(configs.getDatabase());
            source.setUser(configs.getDatabaseUser());
            source.setPassword(configs.getDatabasePassword());
            
            readTables();
            
        } catch (PropertyVetoException e) {
            throw new IOException(e);
        }
    }

    public void close() {
        source.close();
        tables.clear();
    }

    public List<Record> read(SqlChannel channel, long startTime, long endTime) throws IOException {
        if (source == null) {
            throw new IOException("Connection to database not open: " + source.getJdbcUrl());
        }
        try (Connection connection = source.getConnection()) {
            if (channel.isUnion()) {
                readTables();
                
                return readUnion(channel, connection, startTime, endTime);
            }
            else if (channel.getKey() != null && !channel.getKey().isEmpty()) {
                return readUnnormalized(channel, connection, startTime, endTime);
            }
            else {
                return read(channel, connection, startTime, endTime);
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private List<Record> readUnion(SqlChannel channel, Connection connection, long startTime, long endTime) 
            throws IOException, SQLException {
        
        if (channel.getKey() != null && !channel.getKey().isEmpty()) {
            throw new IOException("Unable to unite of unnormalized tables for channel: " + channel.getId());
        }
        Index index = channel.getIndex();
        
        StringBuilder query = new StringBuilder();
        for (String table : tables) {
            query.append(MessageFormat.format("SELECT {0},{1} FROM {2}", 
                index.getColumn(), channel.getDataColumn(), table));
            
            if (query.length() > 0 && tables.size() > 1) {
                query.append(" UNION ALL ");
            }
        }
        query.append(index.queryWhere(startTime, endTime));
        
        return read(channel, connection, query.toString());
    }

    protected List<Record> readUnnormalized(SqlChannel channel, Connection connection, long startTime, long endTime) 
            throws SQLException {
        
        return read(channel, connection, MessageFormat.format("SELECT {0},{3} FROM {2} WHERE {4}=''{5}'' {1}", 
                channel.getIndexColumn(), channel.getIndex().queryWhere(startTime, endTime), 
                channel.getTable(), channel.getDataColumn(), channel.getKeyColumn(), channel.getKey()));
    }

    private List<Record> read(SqlChannel channel, Connection connection, long startTime, long endTime)
            throws SQLException {
        
        return read(channel, connection, MessageFormat.format("SELECT {0},{3} FROM {2} {1}", 
                channel.getIndexColumn(), channel.getIndex().queryWhere(startTime, endTime), 
                channel.getTable(), channel.getDataColumn()));
    }

    private List<Record> read(SqlChannel channel, Connection connection, String query)
            throws SQLException {
        
        List<Record> records = new LinkedList<Record>();
        
        logger.debug("Querying \"{}\"", query);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(query)) {
                while (result.next()) {
                    Record record = channel.decode(result);
                    records.add(record);
                }
            } catch (SQLException e) {
                logger.warn("Error querying \"{}\": {}", query, e.getMessage());
            }
        }
        return records;
    }

    private List<String> readTables() throws IOException {
        tables.clear();
        
        try (Connection connection = source.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                
                try (ResultSet result = statement.executeQuery(String.format("SHOW tables FROM %s", databaseName))) {
                    while (result.next()) {
                        tables.add(result.getString("tables_in_" + databaseName));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        logger.debug("Read tables for database {}: {}", databaseName, String.join(", ", tables));
        return tables;
    }

    public void createTable(SqlChannel channel) {
        StringBuilder query = new StringBuilder("CREATE TABLE " + channel.getTable());
        query.append(MessageFormat.format("({0} {1} {2}, ", channel.getIndexColumn(), 
                SqlChannel.TYPE_INDEX_DEFAULT, SqlChannel.TYPE_NOT_NULL));
        
        String valueType;
        switch (channel.getValueType()) {
        case STRING:
            Integer maxStrLength =  channel.getValueTypeLength();
            valueType = "VARCHAR(" + maxStrLength + ")";
        case BYTE_ARRAY:
            Integer maxBytesLength = channel.getValueTypeLength();
            valueType = "VARBINARY(" + maxBytesLength + ")";
        case BYTE:
            valueType = "TINYINT";
        case BOOLEAN:
            valueType = "BIT";
        case SHORT:
            valueType = "SMALLINT";
        case LONG:
            valueType = "BIGINT";
        case INTEGER:
            valueType = "INT";
        case FLOAT:
            valueType = "REAL";
        default:
            valueType = "FLOAT";
        }
        query.append(MessageFormat.format("{0} {1} {2}, ", channel.getDataColumn(), valueType, 
                SqlChannel.TYPE_NOT_NULL));
        
        query.append(MessageFormat.format("PRIMARY KEY ({0}))", channel.getIndexColumn()));
        query.append(" ENGINE=MYISAM");
        
        logger.debug("Querying \"{}\"", query);
        try (Connection connection = source.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(query.toString());
            }
        } catch (SQLException e) {
            logger.warn("Error creating table \"{}\": {}", query, e.getMessage());
        }
    }

    public boolean hasTable(SqlConfigs configs) {
        return tables.contains(configs.getTable());
    }

    public void write(List<SqlChannel> channels, long timestamp) throws IOException {
        if (source == null) {
            throw new IOException("Connection to database not open: " + source.getJdbcUrl());
        }
        try (Connection connection = source.getConnection()) {
            try (Transaction transaction = new Transaction(connection)) {
                for (SqlChannel channel : channels) {
                    if (channel.getFlag() != Flag.VALID) {
                        logger.debug("Skipping logging of invalid record: {}", channel.getFlag());
                    }
                    else if (channel.isUnion()) {
                        logger.warn("Unable to write to table union for channel \"{}\"", channel.getId());
                    }
                    else if (channel.getKey() != null && !channel.getKey().isEmpty()) {
                        writeUnnormalized(transaction, channel, timestamp);
                    }
                    else {
                        write(transaction, channel, timestamp);
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected void writeUnnormalized(Transaction transaction, SqlChannel channel, long timestamp) throws SQLException {
        String query = MessageFormat.format("INSERT INTO {0} ({1},{2},{3}) VALUES (''{4}'',''{5}'',''{6}'') ON DUPLICATE KEY UPDATE {3} = VALUES({3})", 
                channel.getTable(), channel.getIndexColumn(), channel.getDataColumn(), channel.getKeyColumn(), 
                channel.encodeTimestamp(timestamp), channel.getKey(), channel.encodeValue());
        
        logger.debug("Querying {}", query);
        transaction.execute(query);
    }

    protected void write(Transaction transaction, SqlChannel channel, long timestamp) throws SQLException {
        String query = MessageFormat.format("INSERT INTO {0} ({1},{2}) VALUES (''{3}'',''{4}'') ON DUPLICATE KEY UPDATE {2} = VALUES({2})", 
                channel.getTable(), channel.getIndexColumn(), channel.getDataColumn(), 
                channel.encodeTimestamp(timestamp), channel.encodeValue());
        
        logger.debug("Querying {}", query);
        transaction.execute(query);
    }

    public boolean equals(SqlConfigs configs) {
        return database.equals(configs.getDatabase());
    }

}
