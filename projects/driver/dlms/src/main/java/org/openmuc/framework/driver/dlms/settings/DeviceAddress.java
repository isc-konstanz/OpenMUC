/*
 * Copyright 2011-2020 Fraunhofer ISE
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
package org.openmuc.framework.driver.dlms.settings;

import java.net.InetAddress;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.address.Address;
import org.openmuc.framework.config.address.AddressSyntax;

@AddressSyntax(separator = ";", assignmentOperator = "=", keyValuePairs = true)
public class DeviceAddress extends Configurable {

    @Address(id = "t",
             name = "Connection type",
             description = "The connection type used. Currently, the DLMS/COSEM driver supports serial communication and TCP/IP.",
             valueSelection = "serial:Serial,tcp:TCP/IP"
    )
    private String connectionType = null;

    @Address(id = "sp",
             name = "Serial Port",
             description = "<b>Example:</b>" +
                           "<ol>" +
                               "<li>sp=ttyS0</li>" +
                               "<li>sp=COM1</li>" +
                           "</ol>" +
                           "<br>" +
                           "<i>Only used for Serial connection types</i>",
             mandatory = false
    )
    private String serialPort = "";

    @Address(id = "bd",
             name = "Baud rate",
             description = "<i>Only used for Serial connection types</i>",
             valueDefault = "9600",
             mandatory = false
    )
    private int baudrate = 9600;

    @Address(id = "h",
             name = "Host name",
             description = "<b>Example:</b>" +
                           "<ol>" +
                               "<li>h=127.0.0.1</li>" +
                               "<li>h=192.168.178.88</li>" +
                           "</ol>" +
                           "<br>" +
                           "<i>Only used for TCP/IP connection types</i>",
             mandatory = false
    )
    private InetAddress hostAddress = null;

    @Address(id = "p",
             name = "Port",
             description = "<i>Only used for TCP/IP connection types</i>",
             valueDefault = "4059",
             mandatory = false
    )
    private int port = 4059;

    @Address(id = "hdlc",
             name = "HDLC",
             description = "Use HDLC (<a href='https://en.wikipedia.org/wiki/High-Level_Data_Link_Control'>High-Level Data Link Control</a>",
             valueDefault = "false",
             mandatory = false
    )
    private boolean useHdlc = false;

    @Address(id = "d",
             name = "Baud rate change delay",
             description = "The baud rate change delay in milliseconds",
             valueDefault = "0",
             mandatory = false
    )
    private long baudRateChangeDelay = 0;

    @Address(id = "eh",
             name = "Handshake",
             description = "Use initial handshake to negotiate baud rate",
             valueDefault = "false",
             mandatory = false
    )
    private boolean enableBaudRateHandshake = false;

    @Address(id = "iec",
             name = "IEC 21 address",
             mandatory = false
    )
    private String iec21Address = "";

    @Address(id = "pd",
             name = "Physical Device Address",
             valueDefault = "0",
             mandatory = false
    )
    private int physicalDeviceAddress = 0;

    public DeviceAddress(String address) throws ArgumentSyntaxException {
        configureAddress(address);
    }

    public String getConnectionType() {
        return connectionType;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean useHdlc() {
        return useHdlc;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public long getBaudRateChangeDelay() {
        return baudRateChangeDelay;
    }

    public boolean enableBaudRateHandshake() {
        return enableBaudRateHandshake;
    }

    public String getIec21Address() {
        return iec21Address;
    }

    public int getPhysicalDeviceAddress() {
        return physicalDeviceAddress;
    }

}
