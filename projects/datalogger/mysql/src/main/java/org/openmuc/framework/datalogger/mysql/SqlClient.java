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
package org.openmuc.framework.datalogger.mysql;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlClient {
    private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

    private final String database;
    private final ComboPooledDataSource source;

    private final TimeType timeType;
    private final Double timeScale;
    private final String timeFormat;

    private final String table;
    private final List<String> tables = new ArrayList<String>();

    public SqlClient(SqlConfigs configs) throws IOException  {
    	table = SqlLogger.TABLE;
    	timeType = configs.getTimeType();
    	timeScale = configs.getTimeScale();
    	timeFormat = configs.getTimeFormat();
    	database = configs.getDatabaseName();
    	
		logger.info("Connecting to MySQL database \"{}\"", configs.getDatabase());
        try {
            source = new ComboPooledDataSource();
			source.setDriverClass(SqlLogger.DB_DRIVER);
	        source.setJdbcUrl(configs.getDatabase());
	        source.setUser(configs.getDatabaseUser());
	        source.setPassword(configs.getDatabasePassword());
	        this.readTables();
	        
		} catch (PropertyVetoException e) {
			throw new IOException(e);
		}
    }

    public void close() {
        source.close();
        tables.clear();
    }

    private void readTables() {
    	try (Connection connection = source.getConnection()) {
        	try (Statement statement = connection.createStatement()) {
            	try (ResultSet result = statement.executeQuery(String.format("SHOW tables FROM %s", database))) {
                    if (result.first()) {
	                    tables.add(result.getString("tables_in_"+database));
	                    logger.debug("Found table: {}", result.getString("tables_in_"+database));
                    }
            	}
            }
        } catch (SQLException e) {
            logger.error("Error retrieving table list for database {}: {}", 
            		source.getJdbcUrl(), e.getMessage());
        }
    }

	public void write(List<SqlChannel> channels, long timestamp) throws IOException {
    	if (source == null) {
    		throw new IOException("Connection to database not open: "+source.getJdbcUrl());
    	}
		for (SqlChannel channel : channels) {
			
		}
    }

	public List<Record> read(SqlChannel channel, long startTime, long endTime) throws IOException {
		return null;
	}

}
