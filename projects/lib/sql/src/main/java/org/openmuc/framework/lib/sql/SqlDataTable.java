package org.openmuc.framework.lib.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.openmuc.framework.config.ArgumentSyntaxException;

class SqlDataTable extends LinkedList<SqlData> {
	private static final long serialVersionUID = 1L;

    private final Table table;

    SqlDataTable(Table table) {
    	this.table = table;
    }

    public Table getTable() {
    	return table;
    }

    public boolean create(Connection connection) 
    		throws UnsupportedOperationException, ArgumentSyntaxException, SQLException {
    	
    	return table.create(connection, this);
    }

    public void read(Connection connection) 
            throws SQLException, ArgumentSyntaxException {
    	
    	table.read(connection, this);
    }

    public void write(Statement statement, long timestamp) 
            throws SQLException, ArgumentSyntaxException {

    	table.write(statement, this, timestamp);
    }

}
