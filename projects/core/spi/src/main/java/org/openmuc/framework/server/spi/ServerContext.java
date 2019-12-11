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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;

import org.openmuc.framework.config.ArgumentSyntaxException;

public abstract class ServerContext implements ServerService {

	Class<? extends Channel> channel = null;

	@SuppressWarnings("unchecked")
	protected ServerContext() {
		channel = ((Class<? extends Channel>) getType(this.getClass(), Server.class));
    }

	private Type getType(Class<?> clazz, Class<?> type) {
		while (clazz.getSuperclass() != null) {
			if (clazz.getSuperclass().equals(type)) {
				break;
			}
            clazz = clazz.getSuperclass();
		}
        // This operation is safe. Because clazz is a direct sub-class, getGenericSuperclass() will
        // always return the Type of this class. Because this class is parameterized, the cast is safe
        ParameterizedType superclass = (ParameterizedType) clazz.getGenericSuperclass();
        return superclass.getActualTypeArguments()[0];
	}

    public abstract Server<?> getServer();

	@SuppressWarnings("unchecked")
	<C extends Channel> C newChannel(ServerMappingContainer container) throws ArgumentSyntaxException {
		C channel;
		try {
			channel = (C) this.channel.getDeclaredConstructor().newInstance();
			channel.doConfigure(container);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Unable to instance {0}: {1}", 
            		this.channel.getSimpleName(), e.getMessage()));
		}
		return channel;
	}

}
