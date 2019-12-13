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
import org.openmuc.framework.dataaccess.DataAccessService;

public abstract class Driver<D extends DeviceConfigs<?>> extends DriverContext {

	private DeviceScanner scanner = null;

    @Override
    public final Driver<D> getDriver() {
    	return this;
    }

    public final DriverContext getContext() {
        return this;
    }

    public final void activate(DataAccessService dataAccess) throws Exception {
    	onActivate(dataAccess);
    	onActivate();
    }

    public final void deactivate() {
    	onDeactivate();
    }

    protected void onActivate(DataAccessService dataAccess) throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onActivate() throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onDeactivate() {
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

    Device<?> doConnect(String address, String settings) 
			throws ArgumentSyntaxException, ConnectionException {
		Device<?> device = newConnection(address, settings);
		if (device != null) {
			device.doCreate(this);
			device.doConnect();
			
			return device;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected Device<?> newConnection(String address, String settings) 
			throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
		return newConnection(super.newConnection(address, settings));
	}

	protected Device<?> newConnection(D device) 
			throws ArgumentSyntaxException, ConnectionException {
        // Placeholder for the optional implementation
		if (device instanceof Device) {
			return (Device<?>) device;
		}
		return null;
    }

}