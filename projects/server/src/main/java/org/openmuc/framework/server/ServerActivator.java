/*
 * Copyright 2011-2021 Fraunhofer ISE
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

import org.openmuc.framework.server.annotation.Configure;
import org.openmuc.framework.server.spi.ServerMappingContainer;
import org.openmuc.framework.server.spi.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerActivator<C extends ServerChannel> extends ServerChannelContext implements ServerService {

    private static final Logger logger = LoggerFactory.getLogger(ServerActivator.class);

    private final String id;

    public ServerActivator() {
        super();
        this.id = getServerAnnotation().id();
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final void serverMappings(List<ServerMappingContainer> mappings) {
        // Will only be called when OpenMUC receives new server mappings
        // TODO: Don't clear channels, but remove redundant
        channels.clear();
        invokeConfiguration(mappings);
    }

    @Override
    public final void updatedConfiguration(List<ServerMappingContainer> mappings) {
        invokeConfiguration(mappings);
    }

    private final void invokeConfiguration(List<ServerMappingContainer> mappings) {
        try {
            List<ServerChannel> channels = getChannels(mappings);
            invokeMethod(Configure.class, this, channels);
            invokeMethod(Configure.class, this);
            
        } catch (Exception e) {
            logger.error("Error while configuring server:", e);
        }
    }

}
