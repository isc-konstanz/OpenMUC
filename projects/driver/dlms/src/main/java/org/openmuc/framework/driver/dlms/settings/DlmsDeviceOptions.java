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
package org.openmuc.framework.driver.dlms.settings;

import org.openmuc.framework.config.info.DeviceOptions;
import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.config.info.OptionSelection;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ValueType;

public class DlmsDeviceOptions extends DeviceOptions {
    
    private static final String DESCRIPTION = "In DLMS/COSEM, a physical smart meter device can host several so called logical devices, of which only a single one will be connected with with DLMS/COSEM. </br>"
            + "Each logical device has an address in the range [0, 16383]. As an example, a meter could consist of one logical device for electricity metering at address 18 and another one for a connected gas meter at address 67.";

    public static final String PROTOCOL = "protocol";
    public static final String SERVER_ADDRESS = "serverAddress";
    public static final String SERVER_PORT = "serverPort";
    public static final String SERVER_LOGICAL = "serverLogical";
    public static final String CLIENT_LOGICAL = "clientLogical";

    public static final String PASSWORD = "password";
    public static final String DISCONNECT = "disconnect";
    public static final String HANDSHAKE = "handshake";
    public static final String BAUDRATE = "baudrate";
    public static final String FORCE_SINGLE = "forceSingle";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void configureAddress(OptionCollection address) {
        address.setSyntax(":");

        address.add(protocol());
        address.add(serverAddress());
        address.add(serverPort());
        address.add(serverLogical());
        address.add(clientLogical());
    }

    @Override
    protected void configureSettings(OptionCollection settings) {
        settings.setSyntax(";", "=");
        
        settings.add(password());
        settings.add(disconnect());
        settings.add(handshake());
        settings.add(baudrate());
        settings.add(forceSingle());
    }

    @Override
    protected void configureScanSettings(OptionCollection scanSettings) {
        scanSettings.disable();
    }

    private Option protocol() {
        
        Option protocol = new Option(PROTOCOL, "Protocol", ValueType.STRING);
        protocol.setDescription("The communication protocol used, to uniquely identify and address a physical smart meter. </br>"
                + "Currently, the DLMS/COSEM driver supports communication via HDLC and TCP/IP using Logical Name Referencing to retrieve values from the device.");
        protocol.setMandatory(true);
        
        OptionSelection selection = new OptionSelection(ValueType.STRING);
        selection.addString("hdlc", "HDLC");
        selection.addString("tcp", "TCP/IP");
        selection.addString("udp", "UDP");
        protocol.setValueSelection(selection);
        
        return protocol;
    }

    private Option serverAddress() {
        
        Option serverAddress = new Option(SERVER_ADDRESS, "Physical device address", ValueType.STRING);
        serverAddress.setDescription("The physical device address to uniquely identify a physical smart meter. "
                + "An optional port can be added with an additional parameter.</br>"
                + "Address and format depend on the used protocol.</br></br>"
                + "<b>Example:</b>"
                + "<ol>"
                + "<li><b>HDLC</b>: ttyUSB0 or ttyUSB0:16</li>"
                + "<li><b>TCP</b>: 16:192.168.200.25 or 16:192.168.200.25:4059</li>"
                + "</ol>");
        serverAddress.setMandatory(true);
        
        return serverAddress;
    }

    private Option serverPort() {
        
        Option serverAddress = new Option(SERVER_PORT, "Physical device port", ValueType.INTEGER);
        serverAddress.setDescription("The physical device address port to uniquely identify a physical smart meter.");
        serverAddress.setMandatory(false);
        
        return serverAddress;
    }

    private Option serverLogical() {
        
        Option serverLogical = new Option(SERVER_LOGICAL, "Logical device address", ValueType.STRING);
        serverLogical.setDescription("The logical device address is a 16-Bit unsigned number and is needed to identify a logical device inside a smart meter.</br>"
                + "In most cases, there are 2 logical devices inside a smart meter with the first being a management device to get common information and data "
                + "about other logical devices in this smart meter while the second logical device is the smart meter itself holding the tariff and measurement data.</br>"
                + "The management device has the address 1, the address of the second device is manufacturer specific but can be read from the management device. "
                + "If the physical device acts as a hub for other smart meter, the number of logical devices increases accordingly.</br></br>"
                + "The logical device address is sometimes called <em>server wPort</em> or <em>server SAP</em>.");
        serverLogical.setMandatory(true);
        
        return serverLogical;
    }

    private Option clientLogical() {
        
        Option clientLogical = new Option(CLIENT_LOGICAL, "Client ID", ValueType.STRING);
        clientLogical.setDescription("The client ID defines the access level with which the client connects to the logical device."
                + "The ID 16 is a special client ID, which refers to the <em>public client</em> for which no authentication or encryption is required.</br></br>"
                + "It is also called <em>client SAP</em> or <em>client wPort</em>.");
        clientLogical.setMandatory(true);
        
        return clientLogical;
    }

    private Option password() {
        
        Option password = new Option(PASSWORD, "Password", ValueType.STRING);
        password.setDescription("Authorization password to access the smart meter device.");
        password.setMandatory(false);
        
        return password;
    }

    private Option disconnect() {
        
        Option disconnect = new Option(DISCONNECT, "Disconnect", ValueType.BOOLEAN);
        disconnect.setDescription("Send a disconnect message at DLMS layer on disconnecting from device.</br>"
                + "Set this flag to false if the remote device is expecting the disconnect message at a lower layer (like HDLC).");
        disconnect.setMandatory(false);
        disconnect.setValueDefault(new BooleanValue(true));
        
        return disconnect;
    }

    private Option handshake() {
        
        Option handshake = new Option(HANDSHAKE, "Handshake", ValueType.BOOLEAN);
        handshake.setDescription("Use initial handshake to negotiate baud rate.</br></br>"
                + "Only used for HDLC connections.");
        handshake.setMandatory(false);
        handshake.setValueDefault(new BooleanValue(true));
        
        return handshake;
    }

    private Option baudrate() {
        
        Option baudrate = new Option(BAUDRATE, "Baudrate", ValueType.INTEGER);
        baudrate.setDescription("Maximum supported baud rate (0 = no maximum). "
                + "If UseHandshake = false, this value will be used to communicate with the device and has to be set.</br></br>"
                + "Only used for HDLC connections.");
        baudrate.setMandatory(false);
        
        return baudrate;
    }

    private Option forceSingle() {
        
        Option forceSingle = new Option(FORCE_SINGLE, "Force single", ValueType.BOOLEAN);
        forceSingle.setDescription("Forces every attribute to be requested individually.</br></br>"
                + "This option has to be enabled to support Kamstrup 382 smart meter devices.");
        forceSingle.setMandatory(false);
        forceSingle.setValueDefault(new BooleanValue(false));
        
        return forceSingle;
    }

}
