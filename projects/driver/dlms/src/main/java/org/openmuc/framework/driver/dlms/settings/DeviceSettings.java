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

import org.openmuc.framework.config.ArgumentSyntaxException;

public class DeviceSettings extends GenericSetting {

    @Option(value = "ld", range = "int")
    private final int logicalDeviceAddress = 1;

    @Option("cid")
    private final int clientId = 16;

    @Option("sn")
    private final boolean useSn = false;

    @Option("emech")
    private final int encryptionMechanism = -1;

    @Option("amech")
    private final int authenticationMechanism = 0;

    @Option("ekey")
    private final byte[] encryptionKey = {};

    @Option("akey")
    private final byte[] authenticationKey = {};

    @Option("pass")
    private final String paswd = "";

    @Option("cl")
    private final int challengeLength = 16;

    @Option("rt")
    private final int responseTimeout = 20_000;

    @Option("mid")
    private final String manufacturerId = "MMM";

    @Option("did")
    private final long deviceId = 1;

    public DeviceSettings(String settings) throws ArgumentSyntaxException {
        super.parseFields(settings);
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
