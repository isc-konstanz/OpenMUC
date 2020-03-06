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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.server.spi.ServerMappingContainer;

public class Channel extends ChannelContext {

    private String settings = "";

    protected void doConfigure(ServerMappingContainer container) throws ArgumentSyntaxException {
        doConfigure(container.getServerMapping().getServerAddress());
        onConfigure();
    }

    protected void doConfigure(String settings) throws ArgumentSyntaxException {
        if (!equals(settings)) {
            configureSettings(settings);
        }
        this.settings = settings;
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public final ChannelContext getContext() {
        return this;
    }

    public boolean equals(String settings) {
    	return this.settings.equals(settings);
    }

}
