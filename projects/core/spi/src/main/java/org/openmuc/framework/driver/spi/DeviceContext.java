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
package org.openmuc.framework.driver.spi;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.options.Configurable;

public abstract class DeviceContext extends Configurable {

    DriverContext context;

    void doCreate(DriverContext context) throws ArgumentSyntaxException {
    	this.context = context;
        this.onCreate(context);
        this.onCreate();
    }

    protected void onCreate(DriverContext context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    final void doConfigure(String address, String settings) throws ArgumentSyntaxException {
        configure(address, settings);
    	onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onDestroy() {
        // Placeholder for the optional implementation
    }

    public DriverContext getDriver() {
        return context;
    }

}
