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
package org.openmuc.framework.driver.rpi.w1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.rpi.w1.W1Connection.W1ConnectionCallbacks;
import org.openmuc.framework.driver.rpi.w1.options.W1DevicePreferences;
import org.openmuc.framework.driver.rpi.w1.options.W1DriverInfo;
import org.openmuc.framework.driver.rpi.w1.options.W1ScanPreferences;
import org.openmuc.framework.driver.rpi.w1.options.W1Type;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;


@Component
public class W1Driver implements DriverService, W1ConnectionCallbacks {
    private final static Logger logger = LoggerFactory.getLogger(W1Driver.class);
    private final W1DriverInfo info = W1DriverInfo.getInfo();

    // Pass the ClassLoader, as the W1Master may otherwise not be able to load and 
    // recognize available devices according to their DeviceType
    private final W1Master master = new W1Master(W1Driver.class.getClassLoader());

    private final Map<String, W1Device> devices = new HashMap<String, W1Device>();
    private final List<String> connectedDevices = new ArrayList<String>();

    private volatile boolean isDeviceScanInterrupted = false;

    @Override
    public DriverInfo getInfo() {
        return info;
    }

    @Override
    public void scanForDevices(String settingsStr, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {

        logger.info("Scan for 1-Wire devices connected to the Raspberry Pi platform.");
        W1ScanPreferences prefs = info.getScanPreferences(settingsStr);

        List<W1Device> devices = master.getDevices();
        double size = devices.size();
        if (size > 0) {
            logger.debug("Scan discovered {} 1-Wire devices: {}", size, devices.toString());
    		this.devices.clear();
            
            double counter = 0;
            for (W1Device device : devices) {
                if (isDeviceScanInterrupted) {
                    break;
                }
                
                String id = device.getId().trim().replace("\n", "").replace("\r", "");
                if (!prefs.ignoreExisting() || !connectedDevices.contains(id)) {
                    String name = device.getClass().getSimpleName();
                    W1Type type = W1Type.newType(device);
                    
                    String address = W1DevicePreferences.ID_KEY + ":" + id;
                    String settings = W1DevicePreferences.TYPE_KEY + ":" + type.getName();
                    
                    listener.deviceFound(new DeviceScanInfo(name.toLowerCase()+"_"+id, 
                            address, settings, "1-Wire "+ type.getName() +": "+ name));
                }
                this.devices.put(id, device);
                
                listener.scanProgressUpdate((int) (counter / size * 100.0));
                counter++;
            }
        }
        else logger.debug("Scan discovered no 1-Wire devices");
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        isDeviceScanInterrupted = true;
    }

    @Override
    public Connection connect(String addressStr, String settingsStr) throws ArgumentSyntaxException, ConnectionException {
        
        logger.trace("Connect 1-Wire device \"{}\": {}", addressStr, settingsStr);
        W1DevicePreferences prefs = info.getDevicePreferences(addressStr, settingsStr);
        
        W1Connection connection = null;
        try {
        	W1Device device = null;
        	if (devices.containsKey(prefs.getId())) {
        		device = devices.get(prefs.getId());
        	}
        	else {
        		devices.clear();
        		
                List<W1Device> devices = master.getDevices();
                for (W1Device d : devices) {
                	String id = device.getId().trim().replace("\n", "").replace("\r", "");
                    if (prefs.getId().equals(id)) {
                        device = d;
                    }
                    this.devices.put(id, device);
                }
        	}
        	if (device != null) {
                W1Type type = W1Type.newType(device);
                if (type == prefs.getType()) {
                    connection = new W1Connection(this, device, type);
                    
                    connectedDevices.add(prefs.getId());
                }
        	}
        } catch (IllegalArgumentException e) {
            throw new ArgumentSyntaxException("Unable to configure 1-Wire device: " + e.getMessage());
        }
        if (connection == null) {
            throw new ConnectionException("Unable to find specified 1-Wire device: " + prefs.getId());
        }
        
        return connection;
    }

	@Override
	public void onDisconnect(String id) {
		if (connectedDevices.contains(id)) {
			connectedDevices.remove(id);
		}
	}
}
