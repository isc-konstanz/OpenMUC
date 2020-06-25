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

package org.openmuc.framework.server.restws.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyReader {

    private static PropertyReader instance;
    // Map<ORIGIN, [METHODS, HEADERS]>
    private Map<String, ArrayList<String>> propertyMap;
    private Boolean enableCors;
    private static final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }
        return instance;
    }

    private PropertyReader() {
        reloadAllProperties();
    }

    public void reloadAllProperties() {
        try {
            propertyMap = new HashMap<>();
            String[] urls = System.getProperty("org.openmuc.framework.server.restws.url_cors").split(";");
            String[] methods = System.getProperty("org.openmuc.framework.server.restws.methods_cors").split(";");
            String[] headers = System.getProperty("org.openmuc.framework.server.restws.headers_cors").split(";");
            for (int i = 0; i < urls.length; i++) {
                ArrayList<String> methodHeader = new ArrayList<>();
                methodHeader.add(methods[i]);
                methodHeader.add(headers[i]);
                propertyMap.put(urls[i], methodHeader);
            }
            enableCors = Boolean.valueOf(System.getProperty("org.openmuc.framework.server.restws.enable_cors"));
        } catch (NullPointerException e) {
            logger.error("Necessary system properties for Cors handling are missing");
            enableCors = false;
        }
    }

    public Map<String, ArrayList<String>> getPropertyMap() {
        return propertyMap;
    }

    public Boolean isCorsEnabled() {
        return enableCors;
    }
}
