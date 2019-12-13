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
	
    private static String QUERY_SELECT = "SELECT * FROM %s WHERE time >= %s AND time <= %s";
    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";
//  private static String QUERY_UPDATE = "UPDATE feeds SET time = %s, value = %s WHERE id = %i";

    public ChannelTimestampFormat(ChannelContainer container) throws ArgumentSyntaxException {
    	super(container);
    	
    }

	@Override
	public String readQuery(String table, String database, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String writeQuery() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public String checkQuery(String table, String database) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
