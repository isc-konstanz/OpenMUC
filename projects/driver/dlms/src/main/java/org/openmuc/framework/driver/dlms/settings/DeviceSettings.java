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
package org.openmuc.framework.driver.dlms.settings;

import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.Syntax;

@Syntax(separator = ";", assignment = "=", keyValuePairs = SETTING)
public class DeviceSettings extends Configurable {

    @Option(id = "ld",
            type = SETTING,
            name = "Logical Device Address",
            valueDefault = "1",
            mandatory = false
    )
    private int logicalDeviceAddress = 1;

    @Option(id = "cid",
            type = SETTING,
            name = "Client ID",
            valueDefault = "16",
            mandatory = false
    )
    private int clientId = 16;

    @Option(id = "sn",
            type = SETTING,
            name = "SN referencing",
            valueDefault = "false",
            mandatory = false
    )
    private boolean useSn = false;

    @Option(id = "emech",
            type = SETTING,
            name = "Encryption Mechanism",
            valueDefault = "-1",
            mandatory = false
    )
    private int encryptionMechanism = -1;

    @Option(id = "amech",
            type = SETTING,
            name = "Authentication Mechanism",
            valueDefault = "0",
            mandatory = false
    )
    private int authenticationMechanism = 0;

    @Option(id = "ekey",
            type = SETTING,
            name = "Encryption Key",
            mandatory = false
    )
    private byte[] encryptionKey = {};

    @Option(id = "akey",
            type = SETTING,
            name = "Authentication Key",
            mandatory = false
    )
    private byte[] authenticationKey = {};

    @Option(id = "pass",
            type = SETTING,
            name = "Password",
            description = "Authorization password to access the smart meter device",
            mandatory = false
    )
    private String paswd = "";

    @Option(id = "cl",
            type = SETTING,
            name = "Challenge Length",
            valueDefault = "16",
            mandatory = false
    )
    private int challengeLength = 16;

    @Option(id = "rt",
            type = SETTING,
            name = "Response Timeout",
            valueDefault = "20000",
            mandatory = false
    )
    private int responseTimeout = 20000;

    @Option(id = "mid",
            type = SETTING,
            name = "Manufacturer Id",
            valueDefault = "MMM",
            mandatory = false
    )
    private String manufacturerId = "MMM";

    @Option(id = "did",
            type = SETTING,
            name = "Device Id",
            valueDefault = "1",
            mandatory = false
    )
    private long deviceId = 1;

    public DeviceSettings(String settings) throws ArgumentSyntaxException {
        configure(SETTING, settings);
    }

    public int getLogicalDeviceAddress() {
        return logicalDeviceAddress;
    }

    public int getClientId() {
        return clientId;
    }

    public boolean useSn() {
        return useSn;
    }

    public int getEncryptionMechanism() {
        return encryptionMechanism;
    }

    public int getAuthenticationMechanism() {
        return authenticationMechanism;
    }

    public String getPassword() {
        return paswd;
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public byte[] getAuthenticationKey() {
        return authenticationKey;
    }

    public int getChallengeLength() {
        return challengeLength;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public long getDeviceId() {
        return deviceId;
    }

}
