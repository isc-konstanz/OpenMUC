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
package org.openmuc.framework.lib.sql.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.lib.sql.Index;
import org.openmuc.framework.lib.sql.SqlData;
import org.openmuc.framework.lib.sql.Table;
import org.openmuc.framework.lib.sql.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UnionizedTable extends Table {

    private static final Logger logger = LoggerFactory.getLogger(UnionizedTable.class);

    private final List<String> tables;

    public UnionizedTable(List<String> tables, Index index) {
        super(index);
        this.tables = tables;
    }

    @Override
    public TableType getType() {
        return TableType.UNION;
    }

    @Override
    public String getName() {
        return tables.stream().collect(Collectors.joining(","));
    }

    @Override
    public void write(Statement statement, List<SqlData> values, long timestamp)
            throws UnsupportedOperationException, SQLException {
        throw new UnsupportedOperationException("Unable to write to table union");
    }

    @Override
    public Record read(Connection connection, SqlData data) 
            throws SQLException, ArgumentSyntaxException {
        
        Record record = new Record(Flag.NO_VALUE_RECEIVED_YET);
        
        StringBuilder query = new StringBuilder();
        appendSelect(query, data);
        appendLatest(query);
        
        List<Record> records = read(connection, data, query.toString());
        if (records.size() > 0) {
            record = records.get(records.size() - 1);
        }
        return record;
    }

    @Override 
    public List<Record> read(Connection connection, SqlData data, long startTime, long endTime) 
            throws SQLException, ArgumentSyntaxException {
        
        StringBuilder query = new StringBuilder();
        appendSelect(query, data);
        appendWhere(query, startTime, endTime);
        
        return read(connection, data, query.toString());
    }

    @Override
    public void read(Connection connection, List<SqlData> dataList) 
            throws SQLException, ArgumentSyntaxException {
        
        StringBuilder query = new StringBuilder();
        appendSelect(query, dataList);
        appendLatest(query);
        
        read(connection, dataList, query.toString());
    }

    @Override
    protected void appendSelect(StringBuilder query, List<SqlData> dataList) 
            throws ArgumentSyntaxException {
        
        if (tables.size() < 1) {
            throw new ArgumentSyntaxException("Unable to find any table to make a union");
        }
        StringBuilder columns = new StringBuilder();
        for (SqlData data : dataList) {
            if (data.hasKey()) {
                logger.warn("Unable to unite unnormalized tables for column: {}", data.getKeyColumn());
                data.setRecord(new Record(Flag.DRIVER_ERROR_CHANNEL_ADDRESS_SYNTAX_INVALID));
                continue;
            }
            if (columns.length() > 0 && dataList.size() > 1) {
                columns.append(",");
            }
            columns.append(data.getValueColumn());
        }
        for (String table : tables) {
            if (query.length() > 0 && tables.size() > 1) {
                query.append("UNION ALL ");
            }
            query.append(MessageFormat.format("SELECT {0},{1} FROM {2} ", 
                index.getColumn(), columns, table));
        }
    }

    @Override
    protected void appendSelect(StringBuilder query, SqlData data) 
            throws ArgumentSyntaxException {
        
        if (tables.size() < 1) {
            throw new ArgumentSyntaxException("Unable to find any table to make a union");
        }
        for (String t : tables) {
            query.append(MessageFormat.format("SELECT {0},{1} FROM {2}", 
                index.getColumn(), data.getValueColumn(), t));
            
            if (query.length() > 0 && tables.size() > 1) {
                query.append(" UNION ALL ");
            }
        }
    }

}
