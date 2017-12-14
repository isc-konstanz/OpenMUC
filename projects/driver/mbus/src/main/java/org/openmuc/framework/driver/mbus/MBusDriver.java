/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.driver.mbus;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.transportlayer.SerialBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MBusDriver implements DriverService {
    private final static Logger logger = LoggerFactory.getLogger(MBusDriver.class);

    private final Map<String, MBusSerialInterface> interfaces = new HashMap<>();

    private final static DriverInfo info = new DriverInfo(MBusDriver.class.getResourceAsStream("options.xml"));
    // "Synopsis: <serial_port>[:<baud_rate>][:s]\nExamples for <serial_port>: /dev/ttyS0 (Unix), COM1 (Windows); s
    // forsecondary address scan.";

    private boolean interruptScan;
    // private final boolean scanSecondary = false;

    private int timeout = 2500;
    private int baudRate = 2400;

    @Override
    public DriverInfo getInfo() {
        return info;
    }

    @Override
    public void scanForDevices(String settings, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {

        interruptScan = false;

        String[] args = settings.split(":");
        if (settings.isEmpty() || args.length > 2) {
            throw new ArgumentSyntaxException(
                    "Less than one or more than two arguments in the settings are not allowed.");
        }

        setScanOptions(args);

        MBusConnection connection;
        if (!interfaces.containsKey(args[0])) {
            try {
                SerialBuilder<?, ?> connectionBuilder;
                connectionBuilder = MBusConnection.newSerialBuilder(args[0]);
                connectionBuilder.setBaudrate(baudRate);
                connectionBuilder.setTimeout(timeout);
                
            	connection = (MBusConnection) connectionBuilder.build();
                
            } catch (IllegalArgumentException e) {
                throw new ArgumentSyntaxException();
            } catch (IOException e) {
                throw new ScanException(e);
            }
        }
        else {
            connection = interfaces.get(args[0]).getConnection();
        }

        try {
            VariableDataStructure dataStructure = null;
            for (int i = 0; i <= 250; i++) {

                if (interruptScan) {
                    throw new ScanInterruptedException();
                }

                if (i % 5 == 0) {
                    listener.scanProgressUpdate(i * 100 / 250);
                }
                logger.debug("scanning for meter with primary address {}", i);
                try {
                    dataStructure = connection.read(i);
                } catch (InterruptedIOException e) {
                    continue;
                } catch (IOException e) {
                    throw new ScanException(e);
                }
                String description = "";
                if (dataStructure != null) {
                    SecondaryAddress secondaryAddress = dataStructure.getSecondaryAddress();
                    description = getScanDescription(secondaryAddress);
                }
                listener.deviceFound(new DeviceScanInfo(args[0] + ":" + i, "", description));
                logger.debug("found meter: {}", i);
            }

        } finally {
            connection.close();
        }

    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        interruptScan = true;

    }

    @Override
    public Connection connect(String deviceAddress, String settings)
            throws ArgumentSyntaxException, ConnectionException {
    	
        String[] deviceAddressTokens = deviceAddress.trim().split(":");

        if (deviceAddressTokens.length != 2) {
            throw new ArgumentSyntaxException("The device address does not consist of two parameters.");
        }
        String serialPortName = deviceAddressTokens[0];
        Integer mBusAddress;
        SecondaryAddress secondaryAddress = null;
        try {
            if (deviceAddressTokens[1].length() == 16) {
                mBusAddress = 0xfd;
                secondaryAddress = SecondaryAddress.newFromLongHeader(DatatypeConverter.parseHexBinary(deviceAddressTokens[1]), 0);
            }
            else {
                mBusAddress = Integer.decode(deviceAddressTokens[1]);
            }
        } catch (NumberFormatException e) {
            throw new ArgumentSyntaxException("Settings: mBusAddress (" + deviceAddressTokens[1]
                    + ") is not a int nor a 16 sign long hexadecimal secondary address");
        }

        MBusSerialInterface serialInterface;

        synchronized (this) {

            synchronized (interfaces) {

                serialInterface = interfaces.get(serialPortName);

                if (serialInterface == null) {

                    parseDeviceSettings(settings);
                    try {
                        SerialBuilder<?, ?> connectionBuilder;
                        connectionBuilder = MBusConnection.newSerialBuilder(serialPortName);
                        connectionBuilder.setBaudrate(baudRate);
                        connectionBuilder.setTimeout(timeout);
                        
                    	MBusConnection connection = (MBusConnection) connectionBuilder.build();
                        serialInterface = new MBusSerialInterface(connection, serialPortName, interfaces);
                        
                    } catch (IOException e) {
                        throw new ConnectionException("Unable to bind local interface: " + deviceAddressTokens[0]);
                    }
                }
            }

            synchronized (serialInterface) {
                try {
                    serialInterface.getConnection().linkReset(mBusAddress);
                    sleep(100); // for slow slaves
                    if (secondaryAddress != null) {
                        serialInterface.getConnection().selectComponent(secondaryAddress);
                        sleep(100);
                    }
                    serialInterface.getConnection().read(mBusAddress);

                } catch (InterruptedIOException e) {
                    if (serialInterface.getDeviceCounter() == 0) {
                        serialInterface.close();
                    }
                    throw new ConnectionException(e);
                } catch (IOException e) {
                    serialInterface.close();
                    throw new ConnectionException(e);
                }

                serialInterface.increaseConnectionCounter();

            }

        }

        return new MBusDevice(serialInterface, mBusAddress, secondaryAddress);

    }

    private void parseDeviceSettings(String settings) throws ArgumentSyntaxException {
        if (!settings.isEmpty()) {
            String[] settingArray = settings.split(":");

            for (String setting : settingArray) {
                if (setting.matches("^[t,T][0-9]*")) {
                    setting = setting.substring(1);
                    timeout = parseInt(setting, "Settings: Timeout is not a parsable number.");
                }
                else if (setting.matches("^[0-9]*")) {
                    baudRate = parseInt(setting, "Settings: Baudrate is not a parsable number.");
                }
                else {
                    throw new ArgumentSyntaxException("Settings: Unknown settings parameter. [" + setting + "]");
                }
            }

        }
    }

    private int parseInt(String setting, String errorMsg) throws ArgumentSyntaxException {
        int ret = 0;
        try {
            ret = Integer.parseInt(setting);
        } catch (NumberFormatException e) {
            throw new ArgumentSyntaxException(errorMsg + " [" + setting + "]");
        }
        return ret;
    }

    private void sleep(long millisec) throws ConnectionException {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            throw new ConnectionException(e);
        }
    }

    private String getScanDescription(SecondaryAddress secondaryAddress) {
        String description = secondaryAddress.getManufacturerId() + '_' + secondaryAddress.getDeviceType() + '_'
                + secondaryAddress.getVersion();
        return description;
    }

    private void setScanOptions(String args[]) throws ArgumentSyntaxException {
        for (int i = 1; i < args.length; ++i) {
            // if (args[i] == "s") {
            // scanSecondary = true;
            // }
            // else {
            try {
                baudRate = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                throw new ArgumentSyntaxException("Argument number " + i + " is not an integer");// nor option s.");
            }
            // }
        }
    }
}
