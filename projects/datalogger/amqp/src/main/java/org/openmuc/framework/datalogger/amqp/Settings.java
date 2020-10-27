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

package org.openmuc.framework.datalogger.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Settings {

    private static Logger logger = LoggerFactory.getLogger(Settings.class);
    private static final String PACKAGE_NAME = Settings.class.getPackage().getName().toLowerCase();

    private final int port;
    private final String host;
    private final String username;
    private final String password;
    private final String virtualHost;
    private final String framework;
    private final String exchange;
    private final String parser;
    private final String separator;
    private final boolean ssl;

    Settings() {
        port = getIntProperty(".port", 5672);
        host = getStringProperty(".host", "127.0.0.1");
        username = getStringProperty(".username", "guest");
        password = getStringProperty(".password", "guest");
        virtualHost = getStringProperty(".vhost", "/");
        framework = getStringProperty(".framework", "openmuc");
        exchange = getStringProperty(".exchange", "");
        parser = getStringProperty(".parser", "openmuc");
        separator = getStringProperty(".separator", ".");
        ssl = getBooleanProperty(".ssl", false);
    }

    private String getStringProperty(String propertyName, String defaultValue) {
        String property = "";
        try {
            property = System.getProperty(PACKAGE_NAME + propertyName);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("Property key {}{} is'n set. Using default value: {}", PACKAGE_NAME, propertyName,
                    defaultValue);
        }
        if (property == null) {
            property = defaultValue;
        }
        return property;
    }

    private int getIntProperty(String propertyName, int defaultValue) {
        String property = getStringProperty(propertyName, Integer.toString(defaultValue));
        return Integer.parseInt(property);
    }

    private boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        String property = getStringProperty(propertyName, Boolean.toString(defaultValue));
        return Boolean.parseBoolean(property);
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

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getFramework() {
        return framework;
    }

    public String getExchange() {
        return exchange;
    }

    public String getParser() {
        return parser;
    }

    public String getSeparator() {
        return separator;
    }

    public boolean isSsl() {
        return ssl;
    }

}
