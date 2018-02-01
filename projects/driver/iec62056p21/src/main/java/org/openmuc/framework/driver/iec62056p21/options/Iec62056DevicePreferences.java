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
package org.openmuc.framework.driver.iec62056p21.options;

import org.openmuc.framework.config.options.Preferences;
import org.openmuc.framework.driver.iec62056p21.serial.SerialSettings;

import gnu.io.SerialPort;


public class Iec62056DevicePreferences {

    protected static final String PORT_KEY = "serialPort";
    protected static final String ADDRESS_KEY = "address";
    protected static final String PASSWORD_KEY = "password";
    
    protected static final String TIMEOUT_KEY = "timeout";
    protected static final int TIMEOUT_DEFAULT = 5000;
    
    protected static final String VERIFY_KEY = "verify";
    protected static final boolean VERIFY_DEFAULT = true;
    
    protected static final String HANDLE_ECHO_KEY = "handleEcho";
    protected static final boolean HANDLE_ECHO_DEFAULT = false;
    
    protected static final String HANDSHAKE_KEY = "handshake";
    protected static final boolean HANDSHAKE_DEFAULT = true;
    
    protected static final String BAUDRATE_CHANGE_DELAY_KEY = "baudrateChangeDelay";
    protected static final int BAUDRATE_CHANGE_DELAY_DEFAULT = 250;
    
    protected static final String BAUDRATE_MAX_KEY = "baudrateMax";
    
    protected static final String BAUDRATE_KEY = "baudrate";
    protected static final int BAUDRATE_DEFAULT = 300;
    
    protected static final String DATABITS_KEY = "databits";
    protected static final int DATABITS_DEFAULT = SerialPort.DATABITS_7;
    
    protected static final String STOPBITS_KEY = "stopbits";
    protected static final int STOPBITS_DEFAULT = SerialPort.STOPBITS_1;
    
    protected static final String PARITY_KEY = "parity";
    protected static final int PARITY_DEFAULT = SerialPort.PARITY_EVEN;

    protected final Preferences address;
    protected final Preferences settings;

    public Iec62056DevicePreferences(Preferences address, Preferences settings) {
        this.address = address;
        this.settings = settings;
    }

    public String getSerialPort() {
        if (address.contains(PORT_KEY)) {
            return address.getString(PORT_KEY);
        }
        return null;
    }

    public String getAddress() {
        if (address.contains(ADDRESS_KEY)) {
        	// Address strings length must by divisible by 4
        	String addressStr = address.getString(ADDRESS_KEY);
        	for (int i=0; i<addressStr.length() % 4; i++) {
        		addressStr = '0' + addressStr;
        	}
            return addressStr;
        }
        return "";
    }

    public String getPassword() {
        if (settings.contains(PASSWORD_KEY)) {
            return settings.getString(PASSWORD_KEY);
        }

        return null;
    }

    public Integer getTimeout() {
        if (settings.contains(TIMEOUT_KEY)) {
            return settings.getInteger(TIMEOUT_KEY);
        }

        return TIMEOUT_DEFAULT;
    }

    public boolean hasVerification() {
        if (settings.contains(VERIFY_KEY)) {
            return settings.getBoolean(VERIFY_KEY);
        }

        return VERIFY_DEFAULT;
    }

    public boolean hasEchoHandling() {
        if (settings.contains(HANDLE_ECHO_KEY)) {
            return settings.getBoolean(HANDLE_ECHO_KEY);
        }

        return HANDLE_ECHO_DEFAULT;
    }

    public boolean hasHandshake() {
        if (settings.contains(HANDSHAKE_KEY)) {
            return settings.getBoolean(HANDSHAKE_KEY);
        }

        return HANDSHAKE_DEFAULT;
    }

    public int getBaudrateChangeDelay() {
        if (settings.contains(BAUDRATE_CHANGE_DELAY_KEY)) {
            return settings.getInteger(BAUDRATE_CHANGE_DELAY_KEY);
        }

        return BAUDRATE_CHANGE_DELAY_DEFAULT;
    }

    public Integer getBaudrateMaximum() {
        if (settings.contains(BAUDRATE_MAX_KEY)) {
            return settings.getInteger(BAUDRATE_MAX_KEY);
        }

        return null;
    }

    public int getBaudrate() {
        if (settings.contains(BAUDRATE_KEY)) {
            return settings.getInteger(BAUDRATE_KEY);
        }

        return BAUDRATE_DEFAULT;
    }

    public int getDatabits() {
        if (settings.contains(DATABITS_KEY)) {
            return settings.getInteger(DATABITS_KEY);
        }

        return DATABITS_DEFAULT;
    }

    public int getStopbits() {
        if (settings.contains(STOPBITS_KEY)) {
            return settings.getInteger(STOPBITS_KEY);
        }

        return STOPBITS_DEFAULT;
    }

    public Integer getParity() {
        if (settings.contains(PARITY_KEY)) {
            return settings.getInteger(PARITY_KEY);
        }

        return PARITY_DEFAULT;
    }

    public SerialSettings getSerialSettings() {
        return new SerialSettings(getSerialPort(), getBaudrate(), getDatabits(), getStopbits(), getParity());
    }

}
