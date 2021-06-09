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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.server.annotation.Server;
import org.openmuc.framework.server.spi.ServerMappingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerChannelContext extends Reflectable {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelContext.class);

    Class<? extends ServerChannel> channelClass;

    final Map<String, ServerChannel> channels = new HashMap<String, ServerChannel>();

    ServerChannelContext() {
        channelClass = getChannelClass();
    }

    Server getServerAnnotation() {
        Server server = getClass().getAnnotation(Server.class);
        if (server == null) {
            throw new RuntimeException("Implementation invalid without annotation");
        }
        return server;
    }

    @SuppressWarnings("unchecked")
    Class<? extends ServerChannel> getChannelClass() {
        if (ServerChannelFactory.class.isAssignableFrom(getClass())) {
            try {
                Method method = getClass().getMethod("newChannel", Address.class);
                return (Class<? extends ServerChannel>) method.getReturnType();
                
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        Server server = getServerAnnotation();
        return server.channel();
    }

    @SuppressWarnings("unchecked")
	final <C extends ServerChannel> C newChannel(ServerMappingContainer container) 
            throws RuntimeException, ArgumentSyntaxException {
        
        Address address = Configurations.parseAddress(container.getServerMapping().getServerAddress(), channelClass);
        
        C channel;
        if (this instanceof ServerChannelFactory) {
            channel = (C) ((ServerChannelFactory) this).newChannel(address);
        }
        else {
            channel = (C) newInstance(channelClass);
        }
        return channel;
    }

	final <C extends ServerChannel> C getChannel(ServerMappingContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        C channel = getChannel(id);
        try {
            if (channel == null) {
                channel = newChannel(container);
                channels.put(id, channel);
            }
            channel.invokeConfigure(this, container);
            
        } catch (ArgumentSyntaxException e) {
            
            channels.remove(id);
            
            throw e;
        }
        return channel;
    }

    @SuppressWarnings("unchecked")
	public <C extends ServerChannel> C getChannel(String id) {
        return (C) channels.get(id);
    }

    @SuppressWarnings("unchecked")
	public <C extends ServerChannel> List<C> getChannels() {
        return new ArrayList<C>((Collection<C>) channels.values());
    }

    final <C extends ServerChannel> List<C> getChannels(List<ServerMappingContainer> containers) {
        List<C> channels = new ArrayList<C>();
        for (ServerMappingContainer container : containers) {
            try {
                channels.add(getChannel(container));
                
            } catch (ArgumentSyntaxException | NullPointerException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
            }
        }
        return channels;
    }

}
