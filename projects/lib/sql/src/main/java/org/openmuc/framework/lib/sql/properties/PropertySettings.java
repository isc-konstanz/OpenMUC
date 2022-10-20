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

import org.openmuc.framework.lib.osgi.config.GenericSettings;
import org.openmuc.framework.lib.osgi.config.ServiceProperty;

public class PropertySettings extends GenericSettings {

    public static final String URL = "url";

    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String DATABASE = "database";
    public static final String TABLE_TYPE = "table_type";
    public static final String TABLE = "table";
    public static final String TYPE = "type";
    public static final String DRIVER = "driver";

    public static final String USER = "user";
    public static final String PASSWORD = "password";

    public static final String TIME_ZONE = "timezone";
    public static final String TIME_RES = "time_resolution";
    public static final String TIME_FORMAT = "time_format";
    public static final String INDEX_TYPE = "index_type";
    public static final String INDEX_COL = "index_column";

    public static final String DATA_COL = "value_column";

    public static final String SSL = "ssl";
    public static final String SOCKET_TIMEOUT = "socket_timeout";
    public static final String TCP_KEEP_ALIVE = "tcp_keep_alive";
    public static final String PASSWORD_PSQL = "password_psql";

    public PropertySettings() {
        super();
        
        //String defaultUrl = "jdbc:h2:retry:file:./data/h2/h2;AUTO_SERVER=TRUE;MODE=MYSQL";
        //properties.put(url, new ServiceProperty(url, "URL of the used database", URL, false));
        properties.put(HOST, new ServiceProperty(HOST, "Host name of the database", SqlProperties.HOST, false));
        properties.put(PORT, new ServiceProperty(PORT, "Port of the database", SqlProperties.PORT, false));
        properties.put(DATABASE, new ServiceProperty(DATABASE, "Name of the database", SqlProperties.DATABASE, false));
        
        properties.put(TABLE_TYPE, new ServiceProperty(TABLE_TYPE, "The type of the table", SqlProperties.TABLE_TYPE, false));
        properties.put(TABLE, new ServiceProperty(TABLE, "Name of the database", SqlProperties.TABLE, false));
        properties.put(TYPE, new ServiceProperty(TYPE, "Database type", SqlProperties.TYPE, false));
        properties.put(DRIVER, new ServiceProperty(DRIVER, "Database driver", SqlProperties.DRIVER, false));
        
        properties.put(USER, new ServiceProperty(USER, "User of the used database", SqlProperties.USER, true));
        properties.put(PASSWORD, new ServiceProperty(PASSWORD, "Password for the database user", SqlProperties.PASSWORD, true));
        
        properties.put(TIME_RES, new ServiceProperty(TIME_RES, "The time resolution of stored time series", SqlProperties.TIME_RES, false));
        properties.put(TIME_FORMAT, new ServiceProperty(TIME_FORMAT, "The format of the stored time index", SqlProperties.TIME_FORMAT, false));
        properties.put(INDEX_TYPE, new ServiceProperty(INDEX_TYPE, "The type of the index", SqlProperties.INDEX_TYPE, false));
        properties.put(INDEX_COL, new ServiceProperty(INDEX_COL, "The column name of the table primary key", SqlProperties.INDEX_COL, false));
        
        properties.put(DATA_COL, new ServiceProperty(DATA_COL, "The column name of the table containing the value data", SqlProperties.DATA_COL, false));
        
//        properties.put(SSL, new ServiceProperty(SSL, "SSL needed for the database connection", "false", false));
//        properties.put(SOCKET_TIMEOUT, new ServiceProperty(SOCKET_TIMEOUT, "Seconds after a timeout is thrown", "5", false));
//        properties.put(TCP_KEEP_ALIVE, new ServiceProperty(TCP_KEEP_ALIVE, "Keep TCP connection alive", "true", false));
//        properties.put(TIME_ZONE, new ServiceProperty(TIME_ZONE, "Local time zone", "Europe/Berlin", false));
//        properties.put(PASSWORD_PSQL, new ServiceProperty(PASSWORD_PSQL, "Password for postgresql", "postgres", true));
    }

}
