/*
 * Copyright 2011-2021 Fraunhofer ISE
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
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.lib.sql.Index;
import org.openmuc.framework.lib.sql.SqlData;
import org.openmuc.framework.lib.sql.Table;
import org.openmuc.framework.lib.sql.TableType;


public class MultiColumnTable extends Table {

    protected String table;

    public MultiColumnTable(String table, Index index) {
        super(index);
        this.table = table;
    }

    @Override
    public TableType getType() {
        return TableType.MULTI_COLUMN;
    }

    @Override
    public String getName() {
        return table;
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
    public void write(Statement statement, List<SqlData> dataList, long timestamp) 
            throws SQLException, ArgumentSyntaxException {
        
        // TODO: check if this should be configurable
        dataList.removeIf(d -> !d.isValid());
        if (dataList.size() < 1) {
        	return;
        }
        StringBuilder query = new StringBuilder();
        appendInsert(query, dataList);
        appendValues(query, dataList, timestamp);
        
        appendUpdate(query, dataList);
        
        write(statement, query.toString());
    }

}
