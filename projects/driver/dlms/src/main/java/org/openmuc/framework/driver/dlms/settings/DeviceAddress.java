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
package org.openmuc.framework.driver.dlms.settings;

import java.net.InetAddress;

import org.openmuc.framework.config.Preferences;

public class DeviceAddress extends Preferences {

    @Option("t")
    private String connectionType = null;

    @Option("h")
    private InetAddress hostAddress = null;

    @Option("p")
    private int port = 4059;

    @Option("hdlc")
    private boolean useHdlc = false;

    @Option("sp")
    private String serialPort = "";

    @Option("bd")
    private int baudrate = 9600;

    @Option("d")
    private long baudRateChangeDelay = 0;

    @Option("eh")
    private boolean enableBaudRateHandshake = false;

    @Option("iec")
    private String iec21Address = "";

    @Option("pd")
    private int physicalDeviceAddress = 0;

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
