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
package org.openmuc.framework.driver.iec62056p21;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.iec62056p21.options.Iec62056DevicePreferences;
import org.openmuc.framework.driver.iec62056p21.options.Iec62056DeviceScanPreferences;
import org.openmuc.framework.driver.iec62056p21.options.Iec62056DriverInfo;
import org.openmuc.framework.driver.iec62056p21.serial.SerialSettings;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public final class Iec62056Driver implements DriverService {
    private final static Logger logger = LoggerFactory.getLogger(Iec62056Driver.class);
    private final Iec62056DriverInfo info = Iec62056DriverInfo.getInfo();

    private final Map<String, Iec62056Connection> connections = new HashMap<String, Iec62056Connection>();
    
    public Iec62056Driver() {
        logger.debug("IEC 62056 part 21 Driver instantiated. Expecting rxtxserial.so in: " + 
                System.getProperty("java.library.path") + " for serial connections.");
    }

    @Override
    public DriverInfo getInfo() {
        return info;
    }

    @Override
    public void scanForDevices(String settingsStr, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {
        
        Iec62056DeviceScanPreferences settings = info.getDeviceScanPreferences(settingsStr);

        SerialSettings serialSettings = settings.getSerialSettings();
        
        Iec62056Connection connection;
        synchronized(connections) {
            if (connections.containsKey(serialSettings.getPort())) {
                connection = connections.get(serialSettings.getPort());
            }
            else {
                connection = new Iec62056Connection(serialSettings);
                connections.put(serialSettings.getPort(), connection);
            }
        }

        logger.debug("Scanning for devices at {}", serialSettings.getPort());
        synchronized(connection) {
            try {
                if (connection.open()) {
                    Integer timeout = settings.getTimeout();
                    if (timeout != null && timeout != connection.getTimeout()) {
                        connection.setTimeout(timeout);
                    }
                    
                    List<Iec62056DataSet> dataSets = connection.read(settings);
                    
                    listener.deviceFound(new DeviceScanInfo("", settingsStr,
                            dataSets.get(0).getId().replaceAll("\\p{Cntrl}", "")));
                }
            } catch (IOException e) {
                logger.debug("Scanning channels for device \"{}\" at {} failed: {}", 
                		settings.getAddress(), serialSettings.getPort(), e.getMessage());

                throw new ScanException(e);
            } catch (TimeoutException e) {
                logger.debug("Timeout while scanning channels for device \"{}\" at {} failed: {}", 
                		settings.getAddress(), serialSettings.getPort(), e.getMessage());
                
                throw new ScanException(e);
            } finally {
                connection.close();
            }
        }
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection connect(String addressStr, String settingsStr)
            throws ArgumentSyntaxException, ConnectionException {

        Iec62056DevicePreferences settings = info.getDevicePreferences(addressStr, settingsStr);

        SerialSettings serialSettings = settings.getSerialSettings();
        
        Iec62056Connection connection;
        synchronized(connections) {
            if (connections.containsKey(serialSettings.getPort())) {
                connection = connections.get(serialSettings.getPort());
            }
            else {
                connection = new Iec62056Connection(serialSettings);
                connections.put(serialSettings.getPort(), connection);
            }
        }
        
        synchronized(connection) {
            try {
                connection.open();
                
                if (settings.hasVerification()) {
                    connection.read(settings);
                }
            } catch (IOException | TimeoutException e) {
            	connection.close();
            	
                throw new ConnectionException("Failed to open local serial port \"" +serialSettings.getPort() + "\": " + e.getMessage(), e);
            }
            logger.debug("Connected to device \"{}\" at {}", settings.getAddress(), serialSettings.getPort());
        }
        return new Iec62056Device(connection, settings);
    }

}
