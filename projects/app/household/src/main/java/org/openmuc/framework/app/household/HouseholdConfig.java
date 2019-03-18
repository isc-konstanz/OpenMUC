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

public class HouseholdConfig {

    private static final String ID_PV_KEY = "org.openmuc.framework.app.household.pv";
    private static final String ID_PV_DEFAULT = "pv_energy";

    private static final String ID_GRID_KEY = "org.openmuc.framework.app.household.grid";
    private static final String ID_GRID_DEFAULT = "grid_energy";

    private static final String ID_GRID_EXPORT_KEY = "org.openmuc.framework.app.household.grid.export";
    private static final String ID_GRID_EXPORT_DEFAULT = "grid_export_energy";

    private static final String ID_GRID_IMPORT_KEY = "org.openmuc.framework.app.household.grid.import";
    private static final String ID_GRID_IMPORT_DEFAULT = "grid_import_energy";

    private static final String ID_CONSUMPTION_KEY = "org.openmuc.framework.app.household.consumption";
    private static final String ID_CONSUMPTION_DEFAULT = "consumption_energy";

    public String getPvEnergy() {
        return System.getProperty(ID_PV_KEY, ID_PV_DEFAULT);
    }

    public String getGridEnergy() {
        return System.getProperty(ID_GRID_KEY, ID_GRID_DEFAULT);
    }

    public String getGridExportEnergy() {
        return System.getProperty(ID_GRID_EXPORT_KEY, ID_GRID_EXPORT_DEFAULT);
    }

    public String getGridImportEnergy() {
        return System.getProperty(ID_GRID_IMPORT_KEY, ID_GRID_IMPORT_DEFAULT);
    }

    public String getConsumptionEnergy() {
        return System.getProperty(ID_CONSUMPTION_KEY, ID_CONSUMPTION_DEFAULT);
    }

}
