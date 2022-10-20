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
import java.text.MessageFormat;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.lib.sql.Index;
import org.openmuc.framework.lib.sql.SqlData;


public class MultiIndexTable extends MultiColumnTable {
    public MultiIndexTable(String table, Index index) {
        super(table, index);
    }

    @Override 
    public List<Record> read(Connection connection, SqlData data, long startTime, long endTime) 
            throws SQLException, ArgumentSyntaxException {
        
        if (!data.hasKey()) {
            throw new ArgumentSyntaxException("Unable to query invalid data, missing key value");
        }
        StringBuilder query = new StringBuilder();
        appendSelect(query, data);
        appendWhere(query, startTime, endTime);
        appendKey(query, data);
        
        return read(connection, data, query.toString());
    }

    @Override
    public void read(Connection connection, List<SqlData> dataList) 
            throws SQLException, ArgumentSyntaxException {
        
    	String dataKey = dataList.get(0).getKey();
        if (dataList.stream().anyMatch(d -> !d.hasKey() || !d.getKey().equals(dataKey))) {
            throw new ArgumentSyntaxException("Unable to query ");
        }
        StringBuilder query = new StringBuilder();
        appendSelect(query, dataList);
        appendWhere(query, dataList);
        appendLatest(query);
        
        read(connection, dataList, query.toString());
    }

    @Override
    protected void appendInsert(StringBuilder query, List<SqlData> dataList) 
    		throws ArgumentSyntaxException {
        
    	String dataKey = dataList.get(0).getKey();
        if (dataList.stream().anyMatch(d -> !d.hasKey() || !d.getKey().equals(dataKey))) {
            throw new ArgumentSyntaxException("Unable to query ");
        }
        query.append("INSERT INTO ");
        query.append(table);
        
        query.append(" (");
        query.append(index.getColumn());
        for (int i=0; i<dataList.size(); i++) {
            SqlData data = dataList.get(i);
            if (i == 0) {
            	query.append(data.getKeyColumn());
            }
            query.append(",");
            query.append(data.getValueColumn());
        }
        query.append(") ");
    }

    @Override
    protected void appendValues(StringBuilder query, List<SqlData> dataList, long timestamp) 
            throws ArgumentSyntaxException {
        
        query.append(" VALUES (");
        query.append("(");
        for (int i=0; i<dataList.size(); i++) {
            SqlData data = dataList.get(i);
            if (i == 0) {
                data.encodeTimestamp(query, index, timestamp);
                query.append(",");
                query.append("\'" + data.getKey() + "\'");
            }
            query.append(",");
            
            data.encodeValue(query);
        }
        query.append(") ");
    }

    private void appendKey(StringBuilder query, SqlData data) {
        query.append(MessageFormat.format(" AND {0}=\'{1}\'", data.getKeyColumn(), data.getKey()));
    }

    private void appendWhere(StringBuilder query, List<SqlData> dataList) throws ArgumentSyntaxException {
    	SqlData data = dataList.get(0);
    	String dataKey = data.getKey();
        if (dataList.stream().anyMatch(d -> !d.hasKey() || !d.getKey().equals(dataKey))) {
            throw new ArgumentSyntaxException("Unable to query invalid data list for nonmatching key values");
        }
        query.append(MessageFormat.format(" WHERE {0}=\'{1}\' ", data.getKeyColumn(), data.getKey()));
    }

}
