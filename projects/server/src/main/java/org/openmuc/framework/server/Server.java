/*
 * Copyright 2011-2020 Fraunhofer ISE
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

package org.openmuc.framework.server;

import java.util.List;

import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.server.spi.ServerActivator;
import org.openmuc.framework.server.spi.ServerMappingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Server<C extends ServerChannel> extends ChannelContext implements ServerActivator {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public Server() {
    	super();
        try {
	        doCreate();
	        
		} catch (Exception e) {
            logger.warn("Error instancing driver {}: {}", getId(), e.getMessage());
		}
    }

    void doCreate() throws Exception {
    	onCreate();
    }

    protected void onCreate() throws Exception {
        // Placeholder for the optional implementation
    }

    @Override
    public final void activate(DataAccessService dataAccess) {
        try {
            doActivate(dataAccess);
            
        } catch (Exception e) {
            logger.warn("Error activating server {}: {}", getId(), e.getMessage());
        }
    }

    void doActivate(DataAccessService dataAccess) throws Exception {
        onActivate(dataAccess);
        onActivate();
    }

    protected void onActivate(DataAccessService dataAccess) throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onActivate() throws Exception {
        // Placeholder for the optional implementation
    }

    @Override
    public final void deactivate() {
        try {
            doDeactivate();
            
        } catch (Exception e) {
            logger.warn("Error deactivating server {}: {}", getId(), e.getMessage());
        }
    }

    void doDeactivate() throws Exception {
        onDeactivate();
    }

    protected void onDeactivate() throws Exception {
        // Placeholder for the optional implementation
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void serverMappings(List<ServerMappingContainer> mappings) {
        onConfigure((List<C>) getChannels(mappings));
    }

    protected abstract void onConfigure(List<C> channels);

	@Override
    @SuppressWarnings("unchecked")
    public final void updatedConfiguration(List<ServerMappingContainer> mappings) {
        onUpdate((List<C>) getChannels(mappings));
    }

    protected void onUpdate(List<C> channels) {
        // Placeholder for the optional implementation
        onConfigure(channels);
    }

}
