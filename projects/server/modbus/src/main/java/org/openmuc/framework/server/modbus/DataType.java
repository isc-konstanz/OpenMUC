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
 * Matching from Java Datatype to Modbus Register
 * 
 * One modbus register has the size of two Bytes
 */
public enum DataType {

    /** 1 Bit */
    BOOLEAN(1),

    /** 1 Register, 2 bytes */
    SHORT(1),
    INT16(1),
    UINT16(1),

    /** 2 Register, 4 bytes */
    INT32(2),
    UINT32(2),
    INTEGER(2),
    FLOAT(2),

    /** 4 Register, 8 bytes */
    LONG(4),
    DOUBLE(4),

    /** n Registers, n*2 bytes. */
    BYTE_ARRAY(0),
	STRING(0);

    private final int registerSize;

    private DataType(int registerSize) {
        this.registerSize = registerSize;
    }

    public int getRegisterSize() {
        return registerSize;
    }

}
