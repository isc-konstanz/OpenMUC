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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.lib.sql.Index;
import org.openmuc.framework.lib.sql.SqlData;
import org.openmuc.framework.lib.sql.TableType;


public class SingleColumnTable extends MultiColumnTable {

    protected String table;

    public SingleColumnTable(String table, Index index) {
        super(table, index);
    }

    @Override
    public TableType getType() {
        return TableType.SINGLE_COLUMN;
    }

    @Override
    public boolean create(Connection connection, List<SqlData> dataList) 
    		throws SQLException, ArgumentSyntaxException {
    	
        if (dataList.size() > 1) {
            throw new ArgumentSyntaxException("Unable to query several columns for SingleColumnTables");
        }
        SqlData data = dataList.get(0);
        
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE " + getName());
        
        query.append(" (");
        query.append(getIndex().getColumn());
        query.append(" " + TYPE_INDEX_DEFAULT);
        query.append(" " + TYPE_NOT_NULL);
        query.append(", ");
        
        String type = data.getType().getName();
        switch (data.getValueType()) {
        case BYTE_ARRAY:
        case STRING:
            type += "(" + data.getValueTypeLength() + ")";
            break;
        default:
        	break;
        }
        query.append(MessageFormat.format("{0} {1} {2}, ", data.getValueColumn(), type, TYPE_NOT_NULL));
        
        query.append(MessageFormat.format("PRIMARY KEY ({0}))", getIndex().getColumn()));
        query.append(" ENGINE=MYISAM");
        
        return create(connection, query.toString());
    }

    @Override
    public void read(Connection connection, List<SqlData> dataList) 
            throws SQLException, ArgumentSyntaxException {
        
        if (dataList.size() > 1) {
            throw new ArgumentSyntaxException("Unable to query several columns for SingleColumnTables");
        }
        super.read(connection, dataList);
    }

    @Override
    public void write(Statement statement, List<SqlData> dataList, long timestamp) 
            throws SQLException, ArgumentSyntaxException {
        
        if (dataList.size() > 1) {
            throw new ArgumentSyntaxException("Unable to query several columns for SingleColumnTables");
        }
        super.write(statement, dataList, timestamp);
    }

}
