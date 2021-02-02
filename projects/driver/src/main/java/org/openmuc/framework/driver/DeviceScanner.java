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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;

public abstract class DeviceScanner extends Configurable {

    protected DeviceScanner() {
    }

    protected final void doConfigure(String settings) throws ArgumentSyntaxException {
        configureSettings(settings);
        onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    final void doCreate(DriverContext context) throws ArgumentSyntaxException {
        onCreate(context);
        onCreate();
    }

    protected void onCreate(DriverContext context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public abstract void onScan(DriverDeviceScanListener listener) 
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException;

    public abstract void onScanInterrupt();

}