/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.driver.dlms;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.option.DriverOptionsFactory;
import org.openmuc.framework.driver.dlms.settings.ChannelAddress;
import org.openmuc.framework.driver.dlms.settings.DeviceAddress;
import org.openmuc.framework.driver.dlms.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DlmsCosemDriver implements DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DlmsCosemDriver.class);

    private static final String ID = "dlms";
    private static final String NAME = "DLMS/COSEM";
    private static final String DESCRIPTION = 
            "DLMS/COSEM is a international standardized protocol used mostly to communicate with " +
            "smart meter devices. The DLMS/COSEM driver uses the client library developed by the jDLMS project. " +
            "Currently, the DLMS/COSEM driver supports communication via HDLC and TCP/IP using Logical " +
            "Name Referencing to retrieve values from the device.";

    private static final DriverInfo info = DriverOptionsFactory.getInfo(ID)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setDeviceAddress(DeviceAddress.class)
            .setDeviceSettings(DeviceSettings.class)
            .setChannelAddress(ChannelAddress.class);

    public DlmsCosemDriver() {
        logger.debug("DLMS Driver instantiated. Expecting rxtxserial.so in: " + System.getProperty("java.library.path")
                + " for serial (HDLC) connections.");
    }

    @Override
    public DriverInfo getInfo() {
        return info;
    }

    @Override
    public void scanForDevices(String settings, DriverDeviceScanListener listener)
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void interruptDeviceScan() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection connect(String deviceAddress, String settings)
            throws ConnectionException, ArgumentSyntaxException {
        return new DlmsCosemConnection(deviceAddress, settings);
    }

}
