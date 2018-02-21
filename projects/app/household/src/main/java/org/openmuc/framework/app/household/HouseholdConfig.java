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
