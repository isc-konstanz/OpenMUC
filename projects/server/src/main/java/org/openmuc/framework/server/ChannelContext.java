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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.server.spi.ServerMappingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelContext extends Configurable implements ChannelFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChannelContext.class);

    Class<? extends ServerChannel> channelClass;

    final Map<String, ServerChannel> channels = new HashMap<String, ServerChannel>();

    ChannelContext() {
        channelClass = getChannelClass();
    }

    @SuppressWarnings("unchecked")
	private Class<? extends ServerChannel> getChannelClass() {
    	Class<?> serverClass = getClass();
        while (serverClass.getSuperclass() != null) {
            if (serverClass.getSuperclass().equals(Server.class)) {
                break;
            }
            serverClass = serverClass.getSuperclass();
        }
        // This operation is safe. Because deviceClass is a direct sub-class, getGenericSuperclass() will
        // always return the Type of this class. Because this class is parameterized, the cast is safe
        ParameterizedType superClass = (ParameterizedType) serverClass.getGenericSuperclass();
        return (Class<? extends ServerChannel>) superClass.getActualTypeArguments()[0];
    }

    public ServerChannel getChannel(String id) {
    	return channels.get(id);
    }

	final ServerChannel getChannel(ServerMappingContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        ServerChannel channel = channels.get(id);
        try {
            if (channel == null) {
                channel = newChannel(container);
                channel.doCreate(this);
                channel.doConfigure(container);
                
                channels.put(id, channel);
            }
            else {
	            channel.doConfigure(container);
        	}
        } catch (ArgumentSyntaxException e) {
        	
            channels.remove(id);
            
            throw e;
        }
        return channel;
    }

    final ServerChannel newChannel(ServerMappingContainer container) throws ArgumentSyntaxException {
        return this.newChannel(container.getServerMapping().getServerAddress());
    }

    public ServerChannel newChannel(String address) throws ArgumentSyntaxException {
        return this.newChannel(Configurations.parseAddress(address, channelClass));
    }

    @Override
    public ServerChannel newChannel(Address address) throws ArgumentSyntaxException {
        return this.newChannel();
    }

    protected ServerChannel newChannel() {
        try {
            return channelClass.getDeclaredConstructor().newInstance();
            
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(MessageFormat.format("Unable to instance {0}: {1}", 
            		channelClass.getSimpleName(), e.getMessage()));
        }
    }

    final void bindChannel(Class<? extends ServerChannel> channelClass) {
        this.channelClass = channelClass;
    }

    public List<ServerChannel> getChannels() {
        return (List<ServerChannel>) channels.values();
    }

	final List<ServerChannel> getChannels(List<? extends ServerMappingContainer> containers) {
        List<ServerChannel> channels = new ArrayList<ServerChannel>();
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
