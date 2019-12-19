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
import org.openmuc.framework.driver.spi.ChannelContainer;

public class ChannelTimestampFormat extends SqlChannel {

	/**
	 * IVDATA -> Bring TestTime and TestDate together and order them in DESC to get newest value 
	 */
	
    private static String QUERY_SELECT = "SELECT %s FROM %s";/* "SELECT %s FROM %s.Run2087 ORDER BY TestDate, DateTime";*/
    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";
    private static String QUERY_EXTRA = "SELECT table_name FROM information_schema.tables where table_schema = 'ivdata'";

    public ChannelTimestampFormat(ChannelContainer container) throws ArgumentSyntaxException {
    	super(container);
   
    }
    
//    public String extraQuery(String database) {
//    	return String.format(QUERY_EXTRA);
//    }
//
//	@Override
//	public String readQuery(String table) {
//		// TODO Auto-generated method stub
//		return String.format(QUERY_SELECT, getTable());
//	}
//
//	@Override
//	public String writeQuery() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
