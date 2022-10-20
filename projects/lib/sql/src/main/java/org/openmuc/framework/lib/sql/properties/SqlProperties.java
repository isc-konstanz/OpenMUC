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

package org.openmuc.framework.lib.sql.properties;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlProperties {
    private static final Logger logger = LoggerFactory.getLogger(SqlProperties.class);

    private static final String PACKAGE = SqlProperties.class.getPackage().getName().toLowerCase().replace(".properties", "");

    private static final String DEFAULT_TYPE = "jdbc:mysql";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "openmuc";
    private static final String DEFAULT_TABLE_TYPE = "SINGLE_COLUMN";

    private static final String DEFAULT_USER = "openmuc";
    private static final String DEFAULT_PWD = "openmuc";
    private static final String DEFAULT_PWD_PSQL = "postgres";

    private static final String DEFAULT_SSL = "true";
    private static final String DEFAULT_SOCKET_TIMEOUT = "5";
    private static final String DEFAULT_TCP_KEEP_ALIVE = "true";

    private static final String DEFAULT_TIME_ZONE = "Europe/Berlin";
    private static final String DEFAULT_TIME_RES = "1000";
    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_INDEX_TYPE = "TIMESTAMP_UNIX";
    private static final String DEFAULT_INDEX_COL = "time";

    private static final String DEFAULT_DATA_COL = "value";


    public static String TYPE = System.getProperty(PACKAGE + ".url", DEFAULT_TYPE);
    public static String DRIVER = System.getProperty(PACKAGE + ".driver", DEFAULT_DRIVER);

    public static String URL = System.getProperty(PACKAGE + ".url", null);
    public static String HOST = System.getProperty(PACKAGE + ".host", DEFAULT_HOST);
    public static String PORT = System.getProperty(PACKAGE + ".port", DEFAULT_PORT);
    public static String DATABASE = System.getProperty(PACKAGE + ".database", DEFAULT_DATABASE);
    public static String TABLE_TYPE = System.getProperty(PACKAGE + ".table.type", DEFAULT_TABLE_TYPE);
    public static String TABLE = System.getProperty(PACKAGE + ".table", null);

    public static String USER = System.getProperty(PACKAGE + ".user", DEFAULT_USER);
    public static String PASSWORD = System.getProperty(PACKAGE + ".password", DEFAULT_PWD);
    public static String PASSWORD_PSQL = System.getProperty(PACKAGE + ".psqlPass", DEFAULT_PWD_PSQL);

    public static String SSL = System.getProperty(PACKAGE + ".ssl", DEFAULT_SSL);
    public static String SOCKET_TIMEOUT = System.getProperty(PACKAGE + ".socketTimeout", DEFAULT_SOCKET_TIMEOUT);
    public static String TCP_KEEP_ALIVE = System.getProperty(PACKAGE + ".tcpKeepAlive", DEFAULT_TCP_KEEP_ALIVE);

    public static String TIME_ZONE = System.getProperty(PACKAGE + ".timeZone", DEFAULT_TIME_ZONE);
    public static String TIME_RES = System.getProperty(PACKAGE + ".time.resolution", DEFAULT_TIME_RES);
    public static String TIME_FORMAT = System.getProperty(PACKAGE + ".time.format", DEFAULT_TIME_FORMAT);
    public static String INDEX_TYPE = System.getProperty(PACKAGE + ".index.type", DEFAULT_INDEX_TYPE);
    public static String INDEX_COL = System.getProperty(PACKAGE + ".index.column", DEFAULT_INDEX_COL);

    public static String DATA_COL = System.getProperty(PACKAGE + ".data.column", DEFAULT_DATA_COL);


    private SqlProperties() {
        try {
            loadProperties();
            
        } catch (IOException e) {
            logger.warn("Error loading Properties: " + e.getMessage());
        }
    }

    private void loadProperties() throws IOException {
        String propertyFile = System.getProperties().containsKey("logger.sql.conf.file") ?
                System.getProperty("logger.sql.conf.file") : "conf/logger.sql.conf";

        FileReader reader = new FileReader(propertyFile);
        Properties properties = new Properties();
        properties.load(reader);

        setProperties(properties);
    }

    private void setProperties(Properties properties) {
        TYPE = properties.getProperty(PACKAGE + ".type", DEFAULT_TYPE);
        DRIVER = properties.getProperty(PACKAGE + ".driver", DEFAULT_DRIVER);

        URL = System.getProperty(PACKAGE + ".url", null);
        HOST = System.getProperty(PACKAGE + ".host", DEFAULT_HOST);
        PORT = System.getProperty(PACKAGE + ".port", DEFAULT_PORT);
        DATABASE = System.getProperty(PACKAGE + ".database", DEFAULT_DATABASE);
        TABLE_TYPE = System.getProperty(PACKAGE + ".table.type", DEFAULT_TABLE_TYPE);
        TABLE = System.getProperty(PACKAGE + ".table", null);

        USER = System.getProperty(PACKAGE + ".user", DEFAULT_USER);
        PASSWORD = System.getProperty(PACKAGE + ".password", DEFAULT_PWD);
        PASSWORD_PSQL = System.getProperty(PACKAGE + ".psqlPass", DEFAULT_PWD_PSQL);

        SSL = System.getProperty(PACKAGE + ".ssl", DEFAULT_SSL);
        SOCKET_TIMEOUT = System.getProperty(PACKAGE + ".socketTimeout", DEFAULT_SOCKET_TIMEOUT);
        TCP_KEEP_ALIVE = System.getProperty(PACKAGE + ".tcpKeepAlive", DEFAULT_TCP_KEEP_ALIVE);

        TIME_ZONE = System.getProperty(PACKAGE + ".timeZone", DEFAULT_TIME_ZONE);
        TIME_RES = System.getProperty(PACKAGE + ".time.resolution", DEFAULT_TIME_RES);
        TIME_FORMAT = System.getProperty(PACKAGE + ".time.format", DEFAULT_TIME_FORMAT);
        INDEX_TYPE = System.getProperty(PACKAGE + ".index.type", DEFAULT_INDEX_TYPE);
        INDEX_COL = System.getProperty(PACKAGE + ".index.column", DEFAULT_INDEX_COL);

        DATA_COL = System.getProperty(PACKAGE + ".data.column", DEFAULT_DATA_COL);
    }

}
