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
package org.openmuc.framework.server.modbus;

/**
 * Modbus defines four different address areas called primary tables.
 */
public enum PrimaryTable {
    COILS,
    DISCRETE_INPUTS,
    INPUT_REGISTERS,
    HOLDING_REGISTERS;

    public static PrimaryTable getEnumfromString(String enumAsString) {
        PrimaryTable returnValue = null;
        if (enumAsString != null) {
            for (PrimaryTable value : PrimaryTable.values()) {
                if (enumAsString.equalsIgnoreCase(value.toString())) {
                    returnValue = PrimaryTable.valueOf(enumAsString.toUpperCase());
                    break;
                }
            }
        }
        if (returnValue == null) {
            throw new RuntimeException(
                    enumAsString + " is not supported. Use one of the following supported primary tables: "
                            + getSupportedValues());
        }
        return returnValue;
    }

    /**
     * @return all supported values as a comma separated string
     */
    public static String getSupportedValues() {
        StringBuilder sb = new StringBuilder();
        for (PrimaryTable value : PrimaryTable.values()) {
            sb.append(value.toString()).append(", ");
        }
        return sb.toString();
    }
}
