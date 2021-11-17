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

import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.DriverChannelScanner;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.lib.sql.SqlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnScanner extends DriverChannelScanner {
    private static final Logger logger = LoggerFactory.getLogger(ColumnScanner.class);

    @Option(type = SETTING,
            name = "Table name",
            description = "Tablename to scan columns.",
            mandatory = false)
    private String table;

    private String database;

    private SqlConnector connector;

    @Configure
    public void configure(SqlClient client) {
        this.connector = client.getDatabaseConnector();
        this.database = client.getDatabaseUrl();
        if (table == null) {
            table = client.getTable();
        }
    }

    public String getTable() {
        return table;
    }

    public String getDatabase() {
        return database;
    }

    @Override
    public void scan(List<ChannelScanInfo> channels) throws ArgumentSyntaxException, ScanException, ConnectionException {
        logger.info("Scan for columns in {}.{}", database, table);
        
        try (Connection connection = connector.connect()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet result = statement.executeQuery(String.format("SELECT * FROM %s LIMIT 1", table))) {
                    if (result.first()) {
                        ResultSetMetaData metaData = result.getMetaData();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            String column = metaData.getColumnName(i);
                            Integer typeLength = null;
                            ValueType type;
                            switch(metaData.getColumnTypeName(i)) {
                            case "BIT":
                                type = ValueType.BOOLEAN;
                                break;
                            case "TINYINT":
                                type = ValueType.BYTE;
                                break;
                            case "SMALLINT":
                                type = ValueType.SHORT;
                                break;
                            case "INTEGER":
                                type = ValueType.INTEGER;
                                break;
                            case "BIGINT":
                                type = ValueType.LONG;
                                break;
                            case "REAL":
                                type = ValueType.FLOAT;
                                break;
                            case "DOUBLE":
                            case "NUMERIC":
                                type = ValueType.DOUBLE;
                                break;
                            case "VARBINARY":
                            case "LONGVARBINARY":
                                typeLength = metaData.getColumnDisplaySize(i);
                                type = ValueType.BYTE_ARRAY;
                                break;
                            case "VARCHAR":
                            case "LONGVARCHAR":
                            default:
                                typeLength = metaData.getColumnDisplaySize(i);
                                type = ValueType.STRING;
                            }
                            channels.add(new ChannelScanInfo(column, column, type, typeLength));
                        }
                    }
                } catch (SQLException e) {
                    throw new ScanException(e);
                }
            }
        } catch (SQLException e) {
            throw new ConnectionException(e);
        }
    }

}
