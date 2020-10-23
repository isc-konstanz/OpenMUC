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

package org.openmuc.framework.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.server.spi.ServerMappingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Server<C extends Channel> extends ServerContext {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final Map<String, C> channels = new HashMap<String, C>();

    @Override
    public final Server<C> getServer() {
        return this;
    }

    public final ServerContext getContext() {
        return this;
    }

    @Override
    public final void activate(DataAccessService dataAccess) {
        try {
            onActivate(dataAccess);
            onActivate();
            
        } catch (Exception e) {
            logger.warn("Error activating server {}: {}", getId(), e.getMessage());
        }
    }

    @Override
    public final void deactivate() {
        onDeactivate();
    }

    protected void onActivate(DataAccessService dataAccess) throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onActivate() throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onDeactivate() {
        // Placeholder for the optional implementation
    }

    @Override
    public final void serverMappings(List<ServerMappingContainer> mappings) {
        onConfigure(getChannels(mappings));
    }

    protected abstract void onConfigure(List<C> channels);

    @Override
    public final void updatedConfiguration(List<ServerMappingContainer> mappings) {
        onUpdate(getChannels(mappings));
    }

    protected void onUpdate(List<C> channels) {
        // Placeholder for the optional implementation
        onConfigure(channels);
    }

    protected List<C> getChannels() {
        return (List<C>) channels.values();
    }

    protected List<C> getChannels(List<ServerMappingContainer> mappings) {
        List<C> channels = new LinkedList<C>();
        for (ServerMappingContainer mapping : mappings) {
            try {
                channels.add(getChannel(mapping));
                
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel \"{}\": {}", mapping.getChannel().getId(), e.getMessage());
            }
        }
        return channels;
    }

    protected C getChannel(String id) {
        return channels.get(id);
    }

    protected C getChannel(ServerMappingContainer mapping) throws ArgumentSyntaxException {
        String id = mapping.getChannel().getId();
        C channel = channels.get(id);
        if (channel == null) {
            channel = doCreateChannel(mapping);
            channels.put(id, channel);
        }
        else {
            channel.doConfigure(mapping);
        }
        return channel;
    }

    final C doCreateChannel(ServerMappingContainer mapping) throws ArgumentSyntaxException {
        C channel = onCreateChannel(mapping);
        channel.doCreate(this, mapping.getChannel());
        channel.doConfigure(mapping);
        
        return channel;
    }

    protected C onCreateChannel(ServerMappingContainer mapping) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
        return onCreateChannel();
    }

    protected C onCreateChannel() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
        return super.newChannel();
    }

}
