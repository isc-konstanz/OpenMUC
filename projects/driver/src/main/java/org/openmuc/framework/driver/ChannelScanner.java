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
package org.openmuc.framework.driver;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.driver.spi.ConnectionException;

public abstract class ChannelScanner extends Configurable {

    protected ChannelScanner() {
    }

    protected final void doConfigure(String settings) throws ArgumentSyntaxException, ConnectionException {
        configureSettings(settings);
        onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
    }

    final void doCreate(DeviceContext context) throws ArgumentSyntaxException, ConnectionException {
        onCreate(context);
        onCreate();
    }

    protected void onCreate(DeviceContext context) throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
    }

    public abstract List<ChannelScanInfo> doScan() 
            throws ArgumentSyntaxException, ScanException, ConnectionException;

}
