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

    private static String QUERY_SELECT_DATETIME = "SELECT %s FROM %s WHERE (TestTime =  '12:05:42') and (TestDate = '2019-12-10');";

    public ChannelDateTimeFormat(ChannelContainer container) throws ArgumentSyntaxException {
    	super(container);
    }

	@Override
	public String readQuery() {
		return String.format(QUERY_SELECT_DATETIME, getDataColumn(), getTable());
	}
}
