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

package org.openmuc.framework.driver.modbus;

/**
 * Matching from Java Datatype to Modbus Register
 * 
 * One modbus register has the size of two Bytes
 */
public enum DataType {

    /** 1 Bit */
    BOOLEAN(1),

    // not implemented yet
    /// ** RHB register high byte */
    // INT8_RHB("int8_rhb", 1),

    // not implemented yet
    /// ** RLB register low byte */
    // INT8_RLB("int8_rlb", 1),

    /**
     * @deprecated Use INT16 instead
     */
    @Deprecated
    SHORT(1),

    /** 1 Register, 2 bytes */
    INT16(1),

    /** 2 Register, 4 bytes */
    INT32(2),

    // not implemented yet
    // /** 4 Register, 8 bytes */
    // INT64( 4),

    // not implemented yet
    /// ** to convert register high byte RHB to uint8 */
    // UINT8_RHB( 1),

    // not implemented yet
    /// ** to convert register low byte RLB to uint8 */
    // UINT8_RLB( 1),

    /** 1 Register, 2 bytes */
    UINT16(1),

    /** 2 Register, 4 bytes */
    UINT32(2),

    /** 2 Register, 4 bytes */
    FLOAT(2),

    /** 4 Register, 8 bytes */
    DOUBLE(4),

    /** 4 Register, 8 bytes */
    LONG(4),

    /** n Registers, n*2 bytes. Note: 0 will be overwritten */
    BYTEARRAY(0),

    /** n Registers, n*2 bytes. Note: max. registers 4; 0 will be overwritten */
    BYTEARRAYLONG(0);

    // not implemented yet
    /// ** 1 Register, 2 bytes, access high byte of a register */
    // BYTE_HIGH( 1),

    // not implemented yet
    /// ** 1 Register, 2 bytes, access low byte of a register */
    // BYTE_LOW( 1);

    private final int registerSize;

    private DataType(int registerSize) {
        this.registerSize = registerSize;
    }

    public int getRegisterSize() {
        return registerSize;
    }

    public static DataType getEnum(String string) {
        DataType returnValue = null;

        if (string != null) {
            string = string.toUpperCase();

            for (DataType type : DataType.values()) {
                if (string.equals(type.toString())) {
                    returnValue = type;
                    break;
                }
                else if (string.toUpperCase().matches(DataType.BYTEARRAY + "\\[\\d+\\]")) {
                    // Special check for BYTEARRAY[n] datatyp
                    returnValue = DataType.BYTEARRAY;
                    break;
                }
                else if (string.toUpperCase().matches(DataType.BYTEARRAYLONG + "\\[\\d+\\]")) {
                    // Special check for BYTEARRAYLONG[n] datatyp
                    returnValue = DataType.BYTEARRAYLONG;
                    break;
                }
            }
        }

        if (returnValue == null) {
            throw new RuntimeException(string + " is not supported. Use one of the following supported datatypes: "
                    + getSupportedDatatypes());
        }

        return returnValue;

    }

    /**
     * @return all supported datatypes
     */
    public static String getSupportedDatatypes() {

        String supported = "";

        for (DataType type : DataType.values()) {
            supported += type.toString() + ", ";
        }

        return supported;
    }

    /**
     * Checks if the datatype is valid
     * 
     * @param string
     *            Enum as String
     * @return true if valid, otherwise false
     */
    public static boolean isValid(String string) {
        boolean returnValue = false;

        try {
            if (getEnum(string) != null) {
                returnValue = true;
            }
        } catch (RuntimeException e) {
        }

        return returnValue;
    }
}
