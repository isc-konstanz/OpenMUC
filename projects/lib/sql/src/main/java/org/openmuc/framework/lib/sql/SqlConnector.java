/*
 * Copyright 2011-2020 Fraunhofer ISE
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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.lib.sql.table.MultiColumnTable;
import org.openmuc.framework.lib.sql.table.SingleColumnTable;
import org.openmuc.framework.lib.sql.table.UnionizedTable;
import org.openmuc.framework.lib.sql.time.TimestampIndex;
import org.openmuc.framework.lib.sql.time.TimestampSplit;
import org.openmuc.framework.lib.sql.time.TimestampUnix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlConnector {
    private static final Logger logger = LoggerFactory.getLogger(SqlConnector.class);

    protected final List<String> tables = new ArrayList<String>();

    protected final SqlSettings settings;

    private ComboPooledDataSource source;

    public SqlConnector(SqlSettings settings) throws IOException {
        this.settings = settings;
    }

    public void open() throws IOException {
        logger.info("Opening connection to SQL database \"{}\"", settings.getDatabaseUrl());
        try {
            source = new ComboPooledDataSource();
            source.setDriverClass(settings.getDatabaseDriver());
            source.setJdbcUrl(settings.getDatabaseUrl());
            source.setUser(settings.getDatabaseUser());
            source.setPassword(settings.getDatabasePassword());
            
            readTables();
            
        } catch (PropertyVetoException e) {
            throw new IOException(e);
        }
    }

    public void close() {
        source.close();
        tables.clear();
    }

    public Connection connect() throws SQLException {
        if (source == null) {
            throw new SQLException("Connection to database not open: " + source.getJdbcUrl());
        }
        return source.getConnection();
    }

    protected List<String> readTables() throws IOException {
        tables.clear();
        
        try (Connection connection = source.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                
                try (ResultSet result = statement.executeQuery(String.format("SHOW tables FROM %s", settings.getDatabaseName()))) {
                    while (result.next()) {
                        tables.add(result.getString("tables_in_" + settings.getDatabaseName()));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        logger.debug("Read tables for database {}: {}", settings.getDatabaseName(), String.join(", ", tables));
        return tables;
    }

    protected List<SqlDataTable> groupTables(List<SqlData> dataList) throws ArgumentSyntaxException {
        Map<String, SqlDataTable> dataTables = new HashMap<String, SqlDataTable>();
        for (SqlData data : dataList) {
            String tableName = data.getTable();
            SqlDataTable dataTable = dataTables.get(tableName);
            if (dataTable == null) {
                dataTable = new SqlDataTable(createTable(data));
                dataTables.put(tableName, dataTable);
            }
            else if (dataTable.getTable().getType() != data.getTableType()) {
                throw new ArgumentSyntaxException("Invalid table groups configured");
            }
            dataTable.add(data);
        }
        return new ArrayList<SqlDataTable>(dataTables.values());
    }

    protected Table createTable(SqlData data) throws ArgumentSyntaxException {
        Table table;
        Index index;
        
        switch(settings.getIndexType()) {
        case TIMESTAMP:
            index = new TimestampIndex(settings.getIndexColumn(), settings.getTimeFormat());
            break;
        case TIMESTAMP_SPLIT:
            index = new TimestampSplit(settings.getIndexColumn(), settings.getTimeFormat());
            break;
        default:
            index = new TimestampUnix(settings.getIndexColumn(), settings.getTimeResolution());
            break;
        }
        switch(data.getTableType()) {
        case SINGLE_COLUMN:
            table = new SingleColumnTable(data.getTable(), index);
            break;
        case MULTI_COLUMN:
            table = new MultiColumnTable(data.getTable(), index);
            break;
        case UNION:
            table = new UnionizedTable(tables, index);
            break;
        default:
            throw new ArgumentSyntaxException("Table not yet implemented for type " + data.getTableType());
        }
        return table;
    }

    public void createTables(Connection connection, List<SqlData> dataList) 
            throws SQLException, ArgumentSyntaxException {
        
        for (SqlDataTable data : groupTables(dataList)) {
            Table table = data.getTable();
            String tableName = table.getName();
            if (!hasTable(tableName)) {
                try {
                    data.create(connection);
                    tables.add(tableName);
                    
                } catch(UnsupportedOperationException e) {
                    logger.debug("Unable to create table of type {}", 
                            table.getType());
                }
            }
        }
    }

    public List<String> getTables() {
        return tables;
    }

    public boolean hasTable(String tableName) {
        return tables.contains(tableName);
    }

    public boolean hasTable(Table table) {
        return hasTable(table.getName());
    }

    public Record read(Connection connection, SqlData data)
            throws SQLException, ArgumentSyntaxException {

        Table table = createTable(data);
        if (!hasTable(table)) {
            throw new SqlTableUnavalableException("Unable to find table: " + table.getName());
        }
        return table.read(connection, data);
    }

    public List<Record> read(Connection connection, SqlData data, long startTime, long endTime)
            throws SQLException, ArgumentSyntaxException {

        Table table = createTable(data);
        if (!hasTable(table)) {
            throw new SqlTableUnavalableException("Unable to find table: " + table.getName());
        }
        return table.read(connection, data, startTime, endTime);
    }

    public void read(Connection connection, List<SqlData> dataList)
            throws SQLException, ArgumentSyntaxException {

        for (SqlDataTable table : groupTables(dataList)) {
            if (!hasTable(table.getTable())) {
                throw new SqlTableUnavalableException("Unable to find table: " + table.getName());
            }
        	table.read(connection);
        }
    }

    public void write(Statement statement, List<SqlData> dataList, long timestamp) 
            throws SQLException, ArgumentSyntaxException {
        
        for (SqlDataTable table : groupTables(dataList)) {
            if (!hasTable(table.getTable())) {
                throw new SqlTableUnavalableException("Unable to find table: " + table.getName());
            }
            table.write(statement, timestamp);
        }
    }

    public boolean equals(SqlSettings configs) {
        return this.settings.getDatabaseUrl()
                .equals(configs.getDatabaseUrl());
    }

}
