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
package org.openmuc.framework.driver;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;

public abstract class DriverDeviceScanner extends Reflectable {

    DriverDeviceContext context;

    public DriverDeviceContext getContext() {
        return context;
    }

    protected DriverDeviceScanner() {
    }

    void invokeConfigure(DriverDeviceContext context, Settings settings) throws ArgumentSyntaxException {
    	this.configure(settings);
        this.context = context;
    	
        invokeMethod(Configure.class, this, context, settings);
        invokeMethod(Configure.class, this, context);
        invokeMethod(Configure.class, this);
    }

    public abstract void scan(DriverDeviceScanListener listener) 
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException;

    public abstract void interrupt() throws UnsupportedOperationException;

}
