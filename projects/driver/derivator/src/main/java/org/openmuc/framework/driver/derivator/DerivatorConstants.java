/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.driver.derivator;

public class DerivatorConstants {

    public static final int ADDRESS_PARTS_LENGTH_MIN = 1;
    public static final int ADDRESS_PARTS_LENGTH_MAX = 2;
    public static final String ADDRESS_SEPARATOR = ":";

    public static final int ADDRESS_CHANNEL_ID_INDEX = 0;
    public static final int ADDRESS_DERIVATION_TIME_INDEX = 1;
    public static final int DEFAULT_DERIVATION_TIME = 3600000;

    public static final String DERIVATION_TIME_SECONDS = "S";
    public static final String DERIVATION_TIME_MINUTES = "M";
    public static final String DERIVATION_TIME_HOURS = "H";

    /**
     * Don't let anyone instantiate this class.
     */
    private DerivatorConstants() {
    }
}
