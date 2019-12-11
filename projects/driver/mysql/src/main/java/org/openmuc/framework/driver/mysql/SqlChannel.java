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
import org.openmuc.framework.options.Setting;

public class SqlChannel extends Channel {

    private static String QUERY_SELECT = "SELECT * FROM %s WHERE time >= %s AND time <= %s";
    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";
//  private static String QUERY_UPDATE = "UPDATE feeds SET time = %s, value = %s WHERE id = %i";

    public static String COLUMN_TIME = "time";
    public static String COLUMN_DATA = "data";

    @Address
    protected String table;

    @Setting
    protected TimeType timeType;

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
