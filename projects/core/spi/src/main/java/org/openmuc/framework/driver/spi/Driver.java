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
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Driver<D extends Device<?>> extends DriverContext {
    private static final Logger logger = LoggerFactory.getLogger(Driver.class);

    private DeviceScanner scanner = null;

    protected Driver() {
        super();
        try {
            onCreate(this);
            onCreate();
            
        } catch(Exception e) {
            logger.info("Error while creating driver: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public final Driver<D> getDriver() {
    	return this;
    }

    public final DriverContext getContext() {
        return this;
    }

    protected void onCreate(DriverContext context) {
        // Placeholder for the optional implementation
    }

    protected void onCreate() {
        // Placeholder for the optional implementation
    }

    public void onActivate() {
        // Placeholder for the optional implementation
    }

    public void onDeactivate() {
        // Placeholder for the optional implementation
    }

    @Override
    public final void scanForDevices(String settings, DriverDeviceScanListener listener) 
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {
        scanner = newScanner(settings);
        scanner.doCreate(this);
        scanner.onScan(listener);
    }

    protected DeviceScanner newScanner(String settings) 
            throws UnsupportedOperationException, ArgumentSyntaxException {
        // Placeholder for the optional implementation
        if (!hasDeviceScanner()) {
            throw new UnsupportedOperationException();
        }
        return newDeviceScanner(settings);
    }

    @Override
    public final void interruptDeviceScan() throws UnsupportedOperationException {
        if (scanner == null) {
            throw new UnsupportedOperationException();
        }
        scanner.onScanInterrupt();
    }

    @Override
    public final Connection connect(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
        return doConnect(address, settings);
    }

	D doConnect(String address, String settings) 
			throws ArgumentSyntaxException, ConnectionException {
		D device = newConnection(address, settings);
		device.doCreate(this);
		device.doConnect();
		this.newConnection(device);
		
		return device;
	}

	protected D newConnection(String address, String settings) 
			throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
		
		return newDevice(address, settings);
	}

	protected void newConnection(D configs) 
			throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
    }

    @Override
	@SuppressWarnings("unchecked")
	final void doConnect(Device<?> device) {
    	if (device.getClass().isAssignableFrom(super.device)) {
    		this.onConnect((D) device);
    	}
    }

	protected void onConnect(D device) {
        // Placeholder for the optional implementation
    }

    @Override
	@SuppressWarnings("unchecked")
	final void doDisconnect(Device<?> device) {
    	if (device.getClass().isAssignableFrom(super.device)) {
    		this.onDisconnect((D) device);
    	}
    }

    protected void onDisconnect(D device) {
        // Placeholder for the optional implementation
    }

}
