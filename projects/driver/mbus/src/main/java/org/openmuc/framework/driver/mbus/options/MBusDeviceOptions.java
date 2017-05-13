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
package org.openmuc.framework.driver.mbus.options;

import org.openmuc.framework.config.options.DeviceOptions;
import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.ValueType;

public class MBusDeviceOptions extends DeviceOptions {
    
    private static final String DESCRIPTION = "A device for the M-Bus (wired) driver represents a client, communicating with the master over a serial connection.<br>" +
		"The driver, acting as the master, can register one or several slaves such as gas, water, heat, or electricity meters.";

    public static final String SERIAL_PORT = "serial_port";
    public static final String MBUS_ADDRESS = "mbus_address";

    public static final String BAUDRATE = "baudrate";
    public static final String TIMEOUT = "timeout";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void configureAddress(OptionCollection address) {
        address.setSyntax(":");

        address.add(serialPort());
        address.add(mBusAddress());
    }

    @Override
    protected void configureSettings(OptionCollection settings) {
        settings.setSyntax(":");
        
        settings.add(baudrate());
        settings.add(timeout());
    }

    @Override
    protected void configureScanSettings(OptionCollection scanSettings) {
    	scanSettings.setSyntax(":");

    	scanSettings.add(serialPort());
    }

    private Option serialPort() {
        
        Option serialPort = new Option(SERIAL_PORT, "Serial port", ValueType.STRING);
        serialPort.setDescription("The serial port should be given that connects to the M-Bus converter. (e.g. /dev/ttyS0, /dev/ttyUSB0 on Linux).");
        serialPort.setMandatory(true);
        
        return serialPort;
    }

    private Option mBusAddress() {
        
        Option mBusAddress = new Option(MBUS_ADDRESS, "M-Bus address", ValueType.INTEGER);
        mBusAddress.setDescription("The M-Bus adress can either be the the primary address or secondary address of the meter.<br>" +
        		"The primary address is specified as integer (e.g. 1 for primary address 1) whereas the secondary address consits of 8 bytes that should be specified in hexadecimal form. (e.g. e30456a6b72e3e4e).");
        mBusAddress.setMandatory(true);
        
        return mBusAddress;
    }

    private Option baudrate() {
        
        Option baudrate = new Option(BAUDRATE, "Baudrate", ValueType.INTEGER);
        baudrate.setDescription("The baud rate for the serial communication.<br>Defaults to 2400bd.");
        baudrate.setMandatory(false);
        baudrate.setValueDefault(new IntValue(2400));

        return baudrate;
    }

    private Option timeout() {
        
        Option forceSingle = new Option(TIMEOUT, "Timeout", ValueType.INTEGER);
        forceSingle.setDescription("Defines the read timeout in ms. Default is 2500ms.</br></br>"
                + "<b>Example:</b> 5000 for a timeout of 5 seconds.");
        forceSingle.setMandatory(false);
        forceSingle.setValueDefault(new IntValue(2500));
        
        return forceSingle;
    }

}
