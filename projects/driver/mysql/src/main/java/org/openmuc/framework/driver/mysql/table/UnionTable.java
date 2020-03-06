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
package org.openmuc.framework.driver.mysql.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.mysql.Index;
import org.openmuc.framework.driver.mysql.SqlChannel;
import org.openmuc.framework.driver.mysql.SqlTable;
import org.openmuc.framework.driver.mysql.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UnionTable extends SqlTable {
    private static final Logger logger = LoggerFactory.getLogger(UnionTable.class);

    private final List<String> tables;

    public UnionTable(List<String> tables, Index index) {
        super(index);
        this.tables = tables;
    }

    @Override
    public void read(Connection connection) throws SQLException {
        if (tables.size() < 1) {
            logger.warn("Unable to find any table to make a union");
            return;
        }
        StringBuilder columns = new StringBuilder();
        for (SqlChannel channel : channels) {
            if (channel.getKey() != null && !channel.getKey().isEmpty()) {
                logger.warn("Unable to unite of unnormalized tables for channel: {}", channel.getId());
                channel.setRecord(new Record(Flag.DRIVER_ERROR_CHANNEL_ADDRESS_SYNTAX_INVALID));
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
        StringBuilder query = new StringBuilder();
        for (String table : tables) {
            if (query.length() > 0 && tables.size() > 1) {
                query.append("UNION ALL ");
            }
//            query.append(MessageFormat.format("SELECT * FROM {0} ", table));
            query.append(MessageFormat.format("SELECT {0},{1} FROM {2} ", 
                index.getColumn(), columns, table));
        }
        query.append(index.queryLatest());
        
        logger.debug("Querying \"{}\"", query);
        try (Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(query.toString())) {
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

    @Override
    public void write(Transaction transaction)
            throws UnsupportedOperationException, SQLException {
        throw new UnsupportedOperationException("Unable to write to table union");
    }

}
