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

public abstract class Driver<C extends DeviceConfigs> extends DriverContext implements DeviceCallbacks {
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
    public final Driver<C> getDriver() {
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

	DeviceConnection<?> doConnect(String address, String settings) 
			throws ArgumentSyntaxException, ConnectionException {
		DeviceConnection<?> device = newConnection(address, settings);
		device.doCreate(this);
		device.doConnect();
		
		return device;
	}

	protected DeviceConnection<?> newConnection(String address, String settings) 
			throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
		
		return newConnection(newDeviceConfigs(address, settings));
	}

	protected DeviceConnection<?> newConnection(C configs) 
			throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
        if (configs instanceof DeviceConnection) {
        	return (DeviceConnection<?>) configs;
        }
        return null;
    }

    @Override
	@SuppressWarnings("unchecked")
	public final void onConnected(DeviceConnection<?> device) {
    	if (device.getClass().isAssignableFrom(super.device)) {
    		this.onConnected((C) device);
    	}
    }

	public void onConnected(C configs) {
        // Placeholder for the optional implementation
    }

    @Override
	@SuppressWarnings("unchecked")
	public final void onDisconnected(DeviceConnection<?> device) {
    	if (device.getClass().isAssignableFrom(super.device)) {
    		this.onDisconnected((C) device);
    	}
    }

	public void onDisconnected(C configs) {
        // Placeholder for the optional implementation
    }

}
