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
package org.openmuc.framework.driver.rpi.w1;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.annotation.Setting;
import org.openmuc.framework.driver.DeviceScanner;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.w1.W1Device;

public class W1Scanner extends DeviceScanner {
    private static final Logger logger = LoggerFactory.getLogger(W1Scanner.class);

    @Setting(id = "ignore",
            name = "Ignore existing",
            description = "Ignore already configured devices and only list possible new connections.",
            valueDefault = "true",
            mandatory = false
    )
    private boolean ignore = true;

    private final List<W1Device> devices;
    private final List<String> connected;

    private volatile boolean interrupt = false;

    public W1Scanner(List<W1Device> devices, List<String> connected) {
        this.devices = devices;
        this.connected = connected;
    }

    @Override
    public void onScan(DriverDeviceScanListener listener) 
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException {
        logger.info("Scan for 1-Wire devices connected to the Raspberry Pi platform.");
        
        interrupt = false;
        
        int size = devices.size();
        if (size > 0) {
            logger.debug("Scan discovered {} 1-Wire devices: {}", size, devices.toString());
            
            int counter = 1;
            for (com.pi4j.io.w1.W1Device device : devices) {
                if (interrupt) {
                    break;
                }
                
                String id = device.getId().trim().replace("\n", "").replace("\r", "");
                if (!ignore || !connected.contains(id)) {
                    String name = device.getClass().getSimpleName();
                    W1Type type = W1Type.valueOf(device);
                    
                    String scanSettings = "type:" + type.name();
                    
                    listener.deviceFound(new DeviceScanInfo(name.toLowerCase()+"_"+id, 
                            id, scanSettings, "1-Wire "+ type.getName() +": "+ name));
                }
                
                listener.scanProgressUpdate((int) Math.round(counter/(double) size*100));
                counter++;
            }
        }
        else logger.debug("Scan discovered no 1-Wire devices");
    }

    @Override
    public void onScanInterrupt() throws UnsupportedOperationException {
        interrupt = true;
    }

}
