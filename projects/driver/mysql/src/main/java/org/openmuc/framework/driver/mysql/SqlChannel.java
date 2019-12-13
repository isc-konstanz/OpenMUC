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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.spi.Channel;
import org.openmuc.framework.driver.spi.ChannelContainer;
import org.openmuc.framework.options.Address;

public abstract class SqlChannel extends Channel {

	@Address(id="database", mandatory= false)
	protected String database;
	
    @Address(id="key")
    protected String key;

    @Address(id="table", mandatory= false)
    protected String table;

    public SqlChannel(ChannelContainer container) throws ArgumentSyntaxException {
    	super(container);
    	
    }

    public String getKey() {
    	return key;
    }
    
    public String getDatabase() {
    	return database;
    }

    public abstract String readQuery(String table, String database, String key);

    public abstract String writeQuery();
    
//    public abstract String checkQuery(String table, String database);

    public String getTable() {
    	return table.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
    }
    


}
