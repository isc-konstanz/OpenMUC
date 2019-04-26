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
package org.openmuc.framework.driver.csv.settings;

import org.openmuc.framework.config.Preferences;
import org.openmuc.framework.driver.csv.ESamplingMode;

public class DeviceSettings extends Preferences {

    public static final String SAMPLING_MODE = "samplingmode";

    @Option(SAMPLING_MODE)
    private ESamplingMode samplingMode = ESamplingMode.LINE;

    @Option
    private boolean rewind = false;

    public ESamplingMode samplingMode() {
        return samplingMode;
    }

    public boolean rewind() {
        return rewind;
    }

}
