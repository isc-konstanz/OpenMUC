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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.server.spi.ServerMappingContainer;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;

public abstract class ChannelContainerWrapper extends Configurable {

    ServerMappingContainer container;

    protected ChannelContainerWrapper() {
    }

    protected void doConfigure(ServerMappingContainer container) throws ArgumentSyntaxException {
        if (!equals(container)) {
            doConfigure(container.getServerMapping().getServerAddress());
            onConfigure();
        }
        setMappingContainer(container);
    }

    protected void doConfigure(String address) throws ArgumentSyntaxException {
        configure(ADDRESS, address);
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public final ServerMappingContainer getMappingContainer() {
        return container;
    }

    final void setMappingContainer(ServerMappingContainer container) throws ArgumentSyntaxException {
        this.container = container;
    }

    public boolean equals(ServerMappingContainer container) {
        return this.container != null && container != null &&
                this.container.getChannel().getId().equals(container.getChannel().getId()) &&
                this.container.getServerMapping().getServerAddress().equals(
                     container.getServerMapping().getServerAddress());
    }

}
