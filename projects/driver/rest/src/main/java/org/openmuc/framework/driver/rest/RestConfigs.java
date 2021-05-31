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
package org.openmuc.framework.driver.rest;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.apache.commons.codec.binary.Base64;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.Syntax;
import org.openmuc.framework.driver.DriverDevice;
import org.openmuc.framework.driver.annotation.Configure;

@Syntax(separator = ";", assignment = ":", keyValuePairs = SETTING)
public abstract class RestConfigs extends DriverDevice {

    @Option(type = ADDRESS,
    		name = "Prefix",
            description = "The URL prefix, which specifies the protocol used to access the remote OpenMUC",
            valueSelection = "http:http,https:https",
            valueDefault = "https",
            mandatory = false)
    protected String prefix = "https";

    @Option(type = ADDRESS,
    		name = "Host name",
            description = "The host name of the remote OpenMUC, e.g. 127.0.0.1")
    protected String host;

    @Option(type = ADDRESS,
    		name = "Port",
            description = "The port of the remote OpenMUC, e.g. 8888",
            mandatory = false)
    protected int port = 8888;

    @Option(type = SETTING,
    		name = "Username",
            description = "The username of the remote OpenMUC")
    protected String username;

    @Option(type = SETTING,
    		name = "Password",
            description = "The password of the remote OpenMUC")
    protected String password;

    @Option(type = SETTING,
    		name = "Check timestamp",
            description = "Flags the driver that it should check the remote timestamp, before reading the complete record",
            valueDefault = "false",
            mandatory = false)
    protected boolean checkTimestamp = false;

    @Option(type = SETTING,
    		name = "Bulk reading",
            description = "Flags the driver that it should read all available channels at once, instead of requesting them one by one",
            valueDefault = "false",
            mandatory = false)
    protected boolean bulkReading = false;

    @Option(type = SETTING,
    		name = "Timeout",
            description = "The timeout, after which the HTTP(S) call will be canceled.",
            valueDefault = "10000",
            mandatory = false)
    protected int timeout = 10000;

    protected String url;
    protected String authorization;

    @Configure
    public void configure() {
    	while (host.startsWith("/")) {
    		host = host.substring(1);
    	}
    	if (host.endsWith("/")) {
    		host = host.substring(0, host.length()-1);
    	}
    	url = prefix + "://" + host + ":" + port + "/";
    	
    	String authorization = username + ":" + password;
    	this.authorization = new String(Base64.encodeBase64(authorization.getBytes()));
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

	public boolean isBulkReading() {
		return bulkReading;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getUrl() {
		return url;
	}

	public String getAuthorization() {
		return authorization;
	}

}
