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
package org.openmuc.framework.driver.rest;

import org.apache.commons.codec.binary.Base64;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;

public abstract class RestConfigs extends Device<RestChannel> {

    @Address(id = "prefix",
            name = "Prefix",
            description = "The URL prefix, which specifies the protocol used to access the remote OpenMUC",
            valueSelection = "http:http,https:https",
            valueDefault = "https",
            mandatory = false)
    protected String prefix = "https";

    @Address(id = "host",
            name = "Host name",
            description = "The host name of the remote OpenMUC, e.g. 127.0.0.1")
    protected String host;

    @Address(id = "port",
            name = "Port",
            description = "The port of the remote OpenMUC, e.g. 8888",
            mandatory = false)
    protected int port = 8888;

    @Setting(id = "username",
            name = "Username",
            description = "The username of the remote OpenMUC")
    protected String username;

    @Setting(id = "password",
            name = "Password",
            description = "The password of the remote OpenMUC")
    protected String password;

    @Setting(id = "checkTimestamp",
            name = "Check timestamp",
            description = "Flags the driver that it should check the remote timestamp, before reading the complete record",
            valueDefault = "false",
            mandatory = false)
    protected boolean checkTimestamp = false;

    @Setting(id = "timeout",
            name = "Timeout",
            description = "The timeout, after which the HTTP(S) call will be canceled.",
            valueDefault = "10000",
            mandatory = false)
    protected int timeout = 10000;

    protected String address;
    protected String auth;

    @Override
    protected void onConfigure() {
    	while (host.startsWith("/")) {
    		host = host.substring(1);
    	}
    	if (host.endsWith("/")) {
    		host = host.substring(0, host.length()-1);
    	}
    	address = prefix + "://" + host + ":" + port + "/";
    	
    	String credentials = username + ":" + password;
    	auth = new String(Base64.encodeBase64(credentials.getBytes()));
    }

	public String getPrefix() {
		return prefix;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean checkTimestamp() {
		return checkTimestamp;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getAddress() {
		return address;
	}

	public String getAuthentication() {
		return auth;
	}

}