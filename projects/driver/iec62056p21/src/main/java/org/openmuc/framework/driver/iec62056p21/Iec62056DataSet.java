/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.driver.iec62056p21;

public class Iec62056DataSet {

    private final String id;
    private final String value;
    private final String unit;

    Iec62056DataSet(String id, String value, String unit) {
        this.id = id;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the ID/Address of this data set. The ID is usually an OBIS code
     * of the format A-B:C.D.E*F or on older EDIS code of the format C.D.E.that
     * specifies exactly what the value of this data set represents. C is the
     * type of the measured quantity (e.g 1 = positive active power), D
     * describes the measurement mode and E is the tariff (e.g. 0 for total or 1
     * for tariff 1 only) associated with this value.
     * 
     * @return the ID. If this data set contains no id this function returns the
     *         empty string.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the value of this data set as a String. The value is usually an
     * decimal number that can be converted to a Double using
     * Double.parseDouble(). But the value may also be a date or have some other
     * format.
     * 
     * @return the value. If this data set contains no value this function
     *         returns the empty string.
     */
    public String getValue() {
        return value;
    }

    /**
     * The unit is an optional element of a data set.
     * 
     * @return the unit. If this data set contains no unit this function returns
     *         the empty string.
     */
    public String getUnit() {
        return unit;
    }

}