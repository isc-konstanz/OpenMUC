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

package org.openmuc.framework.server.spi;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Server<C extends Channel> extends ServerContext {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    protected Server() {
        super();
        try {
            onCreate();
            
        } catch(Exception e) {
            logger.info("Error while creating driver: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public final Server<C> getServer() {
    	return this;
    }

    public final ServerContext getContext() {
        return this;
    }

    protected void onCreate() {
        // Placeholder for the optional implementation
    }

    public void onActivate() {
        // Placeholder for the optional implementation
    }

    public void onDeactivate() {
        // Placeholder for the optional implementation
    }

	@Override
	public final void updatedConfiguration(List<ServerMappingContainer> mappings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void serverMappings(List<ServerMappingContainer> mappings) {
		// TODO Auto-generated method stub
		
	}

}
