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
package org.openmuc.framework.driver.sql.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.sql.Index;
import org.openmuc.framework.driver.sql.SqlChannel;
import org.openmuc.framework.driver.sql.SqlTable;
import org.openmuc.framework.driver.sql.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimestampTable extends SqlTable {
    private static final Logger logger = LoggerFactory.getLogger(TimestampTable.class);

    protected String table;

    public TimestampTable(String table, Index index) {
        super(index);
        this.table = table;
    }

    @Override
    public void read(Connection connection) throws SQLException {
        StringBuilder columns = new StringBuilder();
        for (SqlChannel channel : channels) {
            if (channel.getKey() != null && !channel.getKey().isEmpty()) {
                readUnnormalized(connection, channel);
                continue;
            }
            if (columns.length() > 0 && channels.size() > 1) {
                columns.append(",");
            }
            columns.append(channel.getDataColumn());
        }
        if (columns.length() == 0) {
            return;
        }
        String query = MessageFormat.format("SELECT {0},{3} FROM {2} {1}", 
                index.getColumn(), index.queryLatest(), table, columns);
        
        logger.debug("Querying \"{}\"", query);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(query)) {
                if (result.first()) {
                    for (SqlChannel channel : channels) {
                        Record record = channel.decode(result, index);
                        channel.setRecord(record);
                    }
                }
            } catch (SQLException e) {
                for (SqlChannel channel : channels) {
                    channel.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                }
                logger.warn("Error querying \"{}\": {}", query, e.getMessage());
            }
        }
    }

    protected void readUnnormalized(Connection connection, SqlChannel channel) throws SQLException {
        String query = MessageFormat.format("SELECT {0},{3} FROM {2} WHERE {4}=''{5}'' {1}", 
                index.getColumn(), index.queryLatest(), table, channel.getDataColumn(), 
                channel.getKeyColumn(), channel.getKey());
        
        logger.debug("Querying {}", query);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(query)) {
                if (result.first()) {
                    Record record = channel.decode(result, index);
                    channel.setRecord(record);
                }
            } catch (SQLException e) {
                channel.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                logger.warn("Error querying \"{}\": {}", query, e.getMessage());
            }
        }
    }

    @Override
    public void write(Transaction transaction) throws SQLException {
        long timestamp = System.currentTimeMillis();
        
        for (SqlChannel channel : channels) {
            String query;
            if (channel.getKey() != null && !channel.getKey().isEmpty()) {
                query = MessageFormat.format("INSERT INTO {0} ({1},{2}) VALUES ('{3}','{4}') ON DUPLICATE KEY UPDATE {2} = VALUES({2})", 
                        table, index.getColumn(), channel.getDataColumn(), index.encode(timestamp), channel.encodeValue());
            }
            else {
                query = MessageFormat.format("INSERT INTO {0} ({1},{2},{3}) VALUES ('{4}','{5}','{6}') ON DUPLICATE KEY UPDATE {3} = VALUES({3})", 
                        table, index.getColumn(), channel.getDataColumn(), channel.getKeyColumn(), index.encode(timestamp), channel.getKey(), channel.encodeValue());
            }
            logger.debug("Querying {}", query);
            
            transaction.execute(query);
        }
    }

}
