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
package org.openmuc.framework.app.household;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openmuc.framework.app.household.grid.PowerType;

public class HouseholdConfig {

	private static final String FILE = "org.openmuc.framework.app.household.config";

    private static final String SOLAR_POWER_KEY = "org.openmuc.framework.app.household.solar.power";
    private static final String SOLAR_ENERGY_KEY = "org.openmuc.framework.app.household.solar.energy";
    private static final String SOLAR_ENERGY_DEFAULT = "solar_energy";

    private static final String GRID_EXPORT_POWER_KEY = "org.openmuc.framework.app.household.grid.export.power";
    private static final String GRID_EXPORT_ENERGY_KEY = "org.openmuc.framework.app.household.grid.export.energy";
    private static final String GRID_EXPORT_ENERGY_DEFAULT = "grid_export";

    private static final String GRID_IMPORT_POWER_KEY = "org.openmuc.framework.app.household.grid.import.power";
    private static final String GRID_IMPORT_ENERGY_KEY = "org.openmuc.framework.app.household.grid.import.energy";
    private static final String GRID_IMPORT_ENERGY_DEFAULT = "grid_import";

    private static final String GRID_POWER_KEY = "org.openmuc.framework.app.household.grid.power";
    private static final String GRID_POWER_DEFAULT = "grid_power";

    private static final String CONSUMPTION_POWER_KEY = "org.openmuc.framework.app.household.consumption.power";
    private static final String CONSUMPTION_POWER_DEFAULT = "consumption_power";

    private final Properties properties = new Properties();

	public HouseholdConfig() throws IOException {
		String fileName = System.getProperty(FILE);
		if (fileName == null) {
			fileName = "conf" + File.separator + "household.properties";
		}
		File file = new File(fileName);
		if (file.exists()) {
			properties.load(new FileInputStream(file));
		}
	}

    public String get(PowerType type) throws IllegalArgumentException {
        switch(type) {
		case SOLAR:
    		return properties.getProperty(SOLAR_ENERGY_KEY, SOLAR_ENERGY_DEFAULT);
        case GRID_EXPORT:
    		return properties.getProperty(GRID_EXPORT_ENERGY_KEY, GRID_EXPORT_ENERGY_DEFAULT);
        case GRID_IMPORT:
    		return properties.getProperty(GRID_IMPORT_ENERGY_KEY, GRID_IMPORT_ENERGY_DEFAULT);
		case GRID:
    		return properties.getProperty(GRID_POWER_KEY, GRID_POWER_DEFAULT);
		case CONSUMPTION:
    		return properties.getProperty(CONSUMPTION_POWER_KEY, CONSUMPTION_POWER_DEFAULT);
		default:
			throw new IllegalArgumentException("Invalid energy type: "+type.name());
        }
    }

	public boolean hasPower(PowerType type) throws IllegalArgumentException {
		return properties.containsKey(getPowerKey(type));
	}

    public String getPower(PowerType type) throws IllegalArgumentException {
        switch(type) {
		case GRID:
    		return properties.getProperty(GRID_POWER_KEY, GRID_POWER_DEFAULT);
		case CONSUMPTION:
    		return properties.getProperty(CONSUMPTION_POWER_KEY, CONSUMPTION_POWER_DEFAULT);
		default:
    		return properties.getProperty(getPowerKey(type));
        }
    }

	private String getPowerKey(PowerType type) throws IllegalArgumentException {
        switch(type) {
		case SOLAR:
			return SOLAR_POWER_KEY;
        case GRID_EXPORT:
			return GRID_EXPORT_POWER_KEY;
        case GRID_IMPORT:
			return GRID_IMPORT_POWER_KEY;
		case GRID:
			return GRID_POWER_KEY;
		case CONSUMPTION:
			return CONSUMPTION_POWER_KEY;
		default:
			throw new IllegalArgumentException("Invalid power type: "+type.name());
        }
	}

}
