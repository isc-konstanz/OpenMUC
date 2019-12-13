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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.mysql.SqlChannel;
import org.openmuc.framework.driver.mysql.SqlClient;
import org.openmuc.framework.driver.spi.ChannelContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelDateTimeFormat extends SqlChannel {
	private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

    private static String QUERY_SELECT = "SELECT %s FROM %s.%s ORDER BY TIMESTAMP DESC LIMIT %s";
    private static String QUERY_HELPER = "SELECT TIMESTAMP FROM %s.%s ORDER BY TIMESTAMP DESC LIMIT 1";
//    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";
    private static String QUERY_AMOUNT = "SELECT * FROM %s.%s WHERE timestamp = %s";
    private static String QUERY_TEST = "select %s from %s.%s where timestamp in (\r\n" + 
    		"	select max(TIMESTAMP) from %s.%s\r\n" + 
    		") ;";

    public ChannelDateTimeFormat(ChannelContainer container) throws ArgumentSyntaxException {
    	super(container);
    	readQuery(table, database, key);
    }
    
//    @Override
//    public String checkQuery(String table, String database) {
//    	String usedTimestamp = String.format(QUERY_HELPER, getDatabase(), getTable(), "1");
//    	return String.format(QUERY_AMOUNT, getDatabase(),getTable(),usedTimestamp);
//    }

	@Override
	public String readQuery(String table, String database, String key) {
		// TODO Auto-generated method stub
		return String.format(QUERY_TEST, getKey(), getDatabase(), getTable(), getDatabase(), getTable() /*SqlClient.getAmount()*/);

	}

	@Override
	public String writeQuery() {
		// TODO Auto-generated method stub
		return null;
	}

}
