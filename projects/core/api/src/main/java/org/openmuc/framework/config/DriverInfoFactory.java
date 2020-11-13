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
package org.openmuc.framework.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DriverInfoFactory {

    public static final String VIRTUAL = "virtual";

    private static final Map<String, DriverOptions> infos = new HashMap<String, DriverOptions>();

    public static DriverOptions getInfo(String id) {
    	DriverOptions info;
        if (infos.containsKey(id)) {
            info = infos.get(id);
        }
        else {
            info = new DriverOptions(id);
            infos.put(id, info);
        }
        return info;
    }

    /**
     * Factory call to read driver info
     * 
     * @param clazz
     *            the class setup to read the options.xml
     *            resource stream, containing all driver info as XML nodes
     * @return the driver information
     */
    public static DriverOptions readInfo(Class<?> clazz) {
        return readInfo(clazz.getResourceAsStream("options.xml"));
    }

    /**
     * Factory call to read driver info
     * 
     * @param is
     *            resource stream, containing all option info as XML nodes
     * @return the driver information
     */
    public static DriverOptions readInfo(InputStream is) {
    	DriverOptions info = new DriverOptions(is);
    	if (!infos.containsKey(info.getId())) {
            infos.put(info.getId(), info);
        }
    	return info;
    }

    public static DriverOptions readVirtualInfo() {
        return new DriverOptions(DriverOptions.class.getResourceAsStream("virtual.xml"));
    }

}
