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
package org.openmuc.framework.app.gridservice;

public class GridServiceConfig {

    private static final String SERVICE_ID_KEY = "org.openmuc.framework.app.gridservice.service";
    private static final String SERVICE_ID_DEFAULT = "service";

    private static final String SERVICE_MAX_KEY = "org.openmuc.framework.app.gridservice.service.max";
    private static final String SERVICE_MAX_DEFAULT = "10000";

    private static final String POWER_ID_KEY = "org.openmuc.framework.app.gridservice.external";
    private static final String POWER_ID_DEFAULT = "external";

    private static final String POWER_MAX_KEY = "org.openmuc.framework.app.gridservice.grid.power.max";
    private static final String POWER_MIN_KEY = "org.openmuc.framework.app.gridservice.grid.power.min";

    private static final String FREQUENCY_ID_KEY = "org.openmuc.framework.app.gridservice.grid.frequency";
    private static final String FREQUENCY_ID_DEFAULT = "frequency";

    private static final String FREQUENCY_MAX_KEY = "org.openmuc.framework.app.gridservice.grid.frequency.max";
    private static final String FREQUENCY_MAX_DEFAULT = "50.1";

    private static final String FREQUENCY_MIN_KEY = "org.openmuc.framework.app.gridservice.grid.frequency.min";
    private static final String FREQUENCY_MIN_DEFAULT = "49.9";

    public String getServiceChannel() {
        return System.getProperty(SERVICE_ID_KEY, SERVICE_ID_DEFAULT);
    }

    public int getServiceMax() {
        return Integer.valueOf(System.getProperty(SERVICE_MAX_KEY, SERVICE_MAX_DEFAULT));
    }

    public String getPowerChannels() {
        return System.getProperty(POWER_ID_KEY, POWER_ID_DEFAULT);
    }

    public int getPowerMax() throws NullPointerException, IllegalArgumentException {
    	String power = System.getProperty(POWER_MAX_KEY);
    	if (power == null) {
    		throw new NullPointerException("Grid service maximum not defined.");
    	}
    	if (power.isEmpty()) {
    		throw new IllegalArgumentException("Grid service maximum invalid.");
    	}
        return Integer.valueOf(power);
    }

    public int getPowerMin() throws NullPointerException, IllegalArgumentException {
    	String power = System.getProperty(POWER_MIN_KEY);
    	if (power == null) {
    		throw new NullPointerException("Grid service minimum not defined.");
    	}
    	if (power.isEmpty()) {
    		throw new IllegalArgumentException("Grid service minimum invalid.");
    	}
        return Integer.valueOf(power);
    }

    public String getFrequencyChannel() {
        return System.getProperty(FREQUENCY_ID_KEY, FREQUENCY_ID_DEFAULT);
    }

    public double getFrequencyMax() {
        return Double.valueOf(System.getProperty(FREQUENCY_MAX_KEY, FREQUENCY_MAX_DEFAULT));
    }

    public double getFrequencyMin() {
        return Double.valueOf(System.getProperty(FREQUENCY_MIN_KEY, FREQUENCY_MIN_DEFAULT));
    }

}
