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
package org.openmuc.framework.driver.mysql.channel;

import org.openmuc.framework.driver.mysql.SqlChannel;

public class ChannelTimestampFormat extends SqlChannel {

	private static String QUERY_SELECT_DATETIME = "SELECT %s FROM %s WHERE timestamp IN (SELECT MAX(timestamp) FROM %s)";
	private static String QUERY_SELECT_SINGLEROW = "SELECT %s FROM %s ORDER BY timestamp DESC LIMIT 1";
	private static String QUERY_SELECT_MULTIPLEROW = "SELECT %s FROM %s WHERE SVNAME like '%s%%' ORDER BY timestamp DESC LIMIT 1;";

    @Override
  	public String readQuery() {
    	switch(getTable()) {
    	case "ce":
    		return String.format(QUERY_SELECT_SINGLEROW, getDataColumn(), getTable());
    	default:
    		return String.format(QUERY_SELECT_MULTIPLEROW, getDataColumn(), getTable(), getColumn() );
    	}
    }

}
