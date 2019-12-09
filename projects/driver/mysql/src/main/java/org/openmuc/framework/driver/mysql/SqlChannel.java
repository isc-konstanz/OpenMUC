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
package org.openmuc.framework.driver.mysql;

import org.openmuc.framework.driver.spi.Channel;
import org.openmuc.framework.options.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlChannel extends Channel {
    private static final Logger logger = LoggerFactory.getLogger(SqlChannel.class);

    public static int TYPE_LENGTH_DEFAULT = 10;
    public static String TYPE_DEFAULT = "FLOAT";
    public static String TYPE_NOT_NULL = " NOT NULL";
    public static String[] TYPES = new String[] {
            "FLOAT",
            "REAL",
            "BIGINT",
            "INT",
            "SMALLINT",
            "TINYINT",
            "BIT",
            "VARBINARY",
            "VARCHAR"
    };

    private static String QUERY_CREATE = "CREATE TABLE IF NOT EXISTS %s ("
            + "time INT UNSIGNED NOT NULL, "
            + "data %s, "
            + "PRIMARY KEY (time)"
            + ") ENGINE=MYISAM";
    private static String QUERY_SELECT = "SELECT * FROM %s WHERE time >= %s AND time <= %s";
    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";
//  private static String QUERY_UPDATE = "UPDATE feeds SET time = %s, value = %s WHERE id = %i";

    public static String COLUMN_TIME = "time";
    public static String COLUMN_DATA = "data";

    @Address
    protected String table;

//    @Override
//    protected void onCreate() throws ArgumentSyntaxException {
//        if (type == null) {
//            type = TYPE_DEFAULT;
//        }
//        else if (!Arrays.asList(TYPES).contains(type) && 
//                !type.startsWith("VARCHAR(") && !type.startsWith("VARBINARY(")) {
//            throw new EmoncmsException("Value type not allowed: "+type);
//        }
//        if (!empty) {
//            type += TYPE_NOT_NULL;
//        }
//        String query = String.format(QUERY_CREATE, feed.table, type);
//        logger.debug("Query  {}", query);
//        
//        transaction.execute(query);
//    }

    public String readQuery(long start, long end, int interval) {
        return String.format(QUERY_SELECT, getTable(), start, end);
    }

    public String writeQuery(long timestamp, double data) {
        return String.format(QUERY_INSERT, getTable(), timestamp, data);
    }

    public String getTable() {
    	return table.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
    }

}
