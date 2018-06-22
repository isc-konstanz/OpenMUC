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
package org.openmuc.framework.config;

import java.util.HashMap;
import java.util.Map;

public class DriverInfoFactory {

    public static final String VIRTUAL = "virtual";

	private static final Map<String, DriverInfo> singletonMap = new HashMap<>();

    public static DriverInfo getInfo(Class<?> driver) {
    	String name = driver.getName();
    	DriverInfo info;
    	if (singletonMap.containsKey(name)) {
    		info = singletonMap.get(name);
    	}
    	else {
        	info = new DriverInfo(driver);
        	singletonMap.put(name, info);
    	}
    	return info;
    }

    public static DriverPreferences getPreferences(Class<?> driver) {
    	String name = driver.getName();
    	DriverPreferences prefs = null;
    	if (singletonMap.containsKey(name)) {
    		prefs = (DriverPreferences) singletonMap.get(name);
    	}
    	if (prefs == null || !(prefs instanceof DriverPreferences)) {
    		prefs = new DriverPreferences(driver);
        	singletonMap.put(name, prefs);
    	}
    	return prefs;
    }

    public static DriverInfo getVirtualInfo() {
    	return new DriverInfo(DriverInfo.class.getResourceAsStream("virtual.xml"));
    }

}
