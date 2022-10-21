/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.config.option;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.option.DriverOptions.DriverConfigs;

public class DriverOptionsFactory {

    public static final String VIRTUAL = "virtual";

    private static final Map<String, DriverConfigs> configs = new HashMap<String, DriverConfigs>();

    public static DriverConfigs getInfo(String id) {
        DriverConfigs config;
        if (configs.containsKey(id)) {
            config = configs.get(id);
        }
        else {
            config = new DriverConfigs(id);
            configs.put(id, config);
        }
        return config;
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
        DriverConfigs config = new DriverConfigs(is);
        if (!configs.containsKey(config.getId())) {
            configs.put(config.getId(), config);
        }
        return config;
    }

    public static DriverInfo readVirtualInfo() {
        return new DriverConfigs(DriverConfigs.class.getResourceAsStream("virtual.xml"));
    }

}
