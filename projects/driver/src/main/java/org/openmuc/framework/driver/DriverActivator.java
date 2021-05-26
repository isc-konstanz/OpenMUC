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
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DriverActivator extends DriverDeviceContext implements DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverActivator.class);

    DriverDeviceScanner scanner;

    @Override
    public final DriverContext getInfo() {
        return getContext();
    }

    /**
     * Returns the ID of the driver. The ID may only contain ASCII letters, digits, hyphens and underscores. By
     * convention the ID should be meaningful and all lower case letters (e.g. "mbus", "modbus").
     * 
     * @return the unique ID of the driver.
     */
    public final String getId() {
        return getContext().getId();
    }

    /**
     * Returns the name of the driver. The name should be meaningfull to decorate the driver (e.g. "M-Bus", "Modbus").
     * 
     * @return the unique name of the driver.
     */
    public final String getName() {
        return getContext().getName();
    }

    /**
     * Returns the description of the driver. The description should provide a short summary of the functionality 
     * and/or purpose of the driver.
     * 
     * @return the description of the driver.
     */
    public final String getDescription() {
        return getContext().getDescription();
    }

    public DriverActivator() {
        super();
        
        driverContext = new DriverContext(this, getDriverAnnotation());
        try {
            invokeMethod(Configure.class, this, driverContext);
            invokeMethod(Configure.class, this);
            
        } catch (Exception e) {
            logger.warn("Error creating driver {}: {}", getContext().getId(), e.getMessage());
        }
    }

    @Override
    public final void scanForDevices(String settings, DriverDeviceScanListener listener) 
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {
        if (scannerClass == DriverDeviceScanner.class) {
            throw new UnsupportedOperationException("Scanning devices unsupported for " + getClass().getSimpleName());
        }
        scanner = newScanner(settings);
        scanner.scan(listener);
    }

    @Override
    public final void interruptDeviceScan() throws UnsupportedOperationException {
        if (scanner != null) {
            scanner.interrupt();
        }
    }

    @Override
    public final Connection connect(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
        DriverDevice device = newDevice(address, settings);
        return device;
    }

}
