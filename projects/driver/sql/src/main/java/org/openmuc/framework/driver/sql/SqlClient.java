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
package org.openmuc.framework.driver.sql;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.Syntax;
import org.openmuc.framework.driver.DriverDevice;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Device;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.annotation.Write;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.lib.sql.IndexType;
import org.openmuc.framework.lib.sql.SqlConnector;
import org.openmuc.framework.lib.sql.SqlData;
import org.openmuc.framework.lib.sql.SqlSettings;
import org.openmuc.framework.lib.sql.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Syntax(separator = ";", assignment = "=", keyValuePairs = { ADDRESS, SETTING })
@Device(channel = SqlChannel.class, scanner = ColumnScanner.class)
public class SqlClient extends DriverDevice implements SqlSettings {
    private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

    private SqlConnector connector;

//    @Option(type = ADDRESS,
//            name = "URL",
//            description = "URL of the database.<br><br>" +
//                          "<b>Example:</b>" +
//                          "<ol>" +
//                              "<li>jdbc:mysql://127.0.0.1:3306/openmuc</li>" +
//                              "<li>jdbc:postgresql://127.0.0.1:5432/openmuc</li>" +
//                          "</ol>",
//            mandatory = false)
    protected String url;

    @Option(type = ADDRESS,
            name = "Host name",
            description = "Host name of the database, if the URL is not configured.<br><br>" +
                          "<b>Example:</b>" +
                          "<ol>" +
                              "<li>localhost</li>" +
                              "<li>127.0.0.1</li>" +
                              "<li>192.168.178.88</li>" +
                          "</ol>",
            mandatory = false)
    protected String host;

    @Option(type = ADDRESS,
            name = "Port",
            description = "Port of the database, if the URL is not configured.",
            valueDefault = "3306",
            mandatory = false)
    protected int port;

    @Option(type = ADDRESS,
            name = "Database name",
            description = "Name of the database, if the URL is not configured.",
            mandatory = false)
    protected String database;

    @Option(type = ADDRESS,
            name = "Table name",
            description = "Default tablename to read columns from.",
            mandatory = false)
    protected String table;

    @Option(type = SETTING,
            name = "Table type",
            description = "The type of the table.",
            valueSelection = "SINGLE_COLUMN:Single column,MULTI_COLUMN:Multi columns,VALUE_TYPE:Value types,UNION:Union",
            mandatory = false)
    protected TableType tableType;

    @Option(type = SETTING,
            name = "Database driver",
            mandatory = false)
    protected String driver;

    @Option(type = SETTING,
            name = "Database type",
            mandatory = false)
    protected String type;

    @Option(type = SETTING,
            name = "Username",
            description = "Username to authorize the connection to the database.",
            mandatory = false)
    protected String user;

    @Option(type = SETTING,
            name = "Password",
            description = "Password to authenticate the connection to the database.",
            mandatory = false)
    protected String password;

    @Option(type = SETTING,
            name = "Time resolution",
            description = "The time resolution of stored time series.",
            valueSelection = "1:Milliseconds,1000:Seconds,60000:Minutes,3600000:Hours",
            mandatory = false)
    protected int timeResolution;

    @Option(type = SETTING,
            name = "Time format",
            description = "The format of the stored time index.<br><br>" +
                          "<b>Example:</b> yyyy-MM-dd HH:mm:ss",
            mandatory = false)
    protected String timeFormat;

    @Option(type = SETTING,
            name = "Index type",
            description = "The type of the index.",
            valueSelection = "TIMESTAMP:Timestamp,TIMESTAMP_UNIX:Unix timestamp,TIMESTAMP_SPLIT:Split timestamp.",
            mandatory = false
    )
    protected IndexType indexType;

    @Option(type = SETTING,
            name = "Index column",
            description = "The column name of the table primary key.",
            mandatory = false)
    protected String indexColumn;

    @Connect
    public void open() throws ConnectionException {
    	try {
			connector.open();
			
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
    }

    public SqlConnector getDatabaseConnector() {
    	return connector;
    }

    @Override
    public String getDatabaseUrl() {
        return url;
    }

    @Override
    public String getDatabaseName() {
        return database;
    }

    @Override
    public String getDatabaseDriver() {
        return driver;
    }

    @Override
    public String getDatabaseType() {
        return type;
    }

    @Override
    public String getDatabaseUser() {
        return user;
    }

    @Override
    public String getDatabasePassword() {
        return password;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public TableType getTableType() {
        return tableType;
    }

    @Override
    public int getTimeResolution() {
        return timeResolution;
    }

    @Override
    public String getTimeFormat() {
        return timeFormat;
    }

    @Override
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public String getIndexColumn() {
        return indexColumn;
    }

    @Configure
    protected void configure() throws ArgumentSyntaxException {
        if (database == null || database.isEmpty()) {
            throw new ArgumentSyntaxException("Database name needs to be configured");
        }
        url = type + "://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            throw new ArgumentSyntaxException("Database login credentials need to be configured");
        }
        
        if (table != null) {
            String valid = table.replaceAll("[^a-zA-Z0-9]", "_");
            if (!table.equals(valid)) {
                throw new ArgumentSyntaxException(
                        "Table name invalid. Only alphanumeric letters separated by underscore are allowed: " + valid);
            }
        }
    }

    @Connect
    protected void connect() throws ArgumentSyntaxException, ConnectionException {
        logger.info("Initializing SQL connection \"{}\"", url);
        try {
            if (connector != null) {
                connector.close();
            }
            connector = new SqlConnector(this);

        } catch (Exception e) {
            throw new ConnectionException(e);
        }
    }

    @Disconnect
    public void close() {
        connector.close();
        connector = null;
    }

    @Read
    public void read(List<SqlData> data, String samplingGroup) throws  ConnectionException {
        try (Connection connection = connector.connect()) {
            connector.read(connection, data);
            
        } catch (Exception e) {
            throw new ConnectionException(e);
        }
    }

    @Write
    public void write(List<SqlData> data) throws ConnectionException {
        long timestamp = System.currentTimeMillis();
        
        try (Connection connection = connector.connect()) {
            try (Statement statement = connection.createStatement()) {
                connector.write(statement, data, timestamp);
            }
        } catch (Exception e) {
            throw new ConnectionException(e);
        }
    }

}
