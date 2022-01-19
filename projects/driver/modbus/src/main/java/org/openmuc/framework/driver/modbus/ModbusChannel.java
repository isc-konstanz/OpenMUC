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
package org.openmuc.framework.driver.modbus;

public class ModbusChannel {

    private static final boolean WRITE_SINGLE_REGISTER = Boolean.parseBoolean(System.getProperty(ModbusChannel.class.
            getPackage().getName().toLowerCase() + ".write_single_register", "false"));

    /** Contains values to define the access method of the channel */
    public static enum EAccess {
        READ,
        WRITE
    }

    /** A Parameter of the channel address */
    public static final int IGNORE_UNIT_ID = -1;

    /** A Parameter of the channel address */
    private static final int UNITID = 0;

    /** A Parameter of the channel address */
    private static final int PRIMARYTABLE = 1;

    /** A Parameter of the channel address */
    private static final int ADDRESS = 2;

    /** A Parameter of the channel address */
    private static final int DATATYPE = 3;

    /** Start address to read or write from */
    private int startAddress;

    /** Number of registers/coils to be read or written */
    private int count;

    /** Used to determine the register/coil count */
    private DataType datatype;

    /** Used to determine the appropriate transaction method */
    private FunctionCode functionCode;

    /** Specifies whether the channel should be read or written */
    private EAccess accessFlag;

    /** */
    private PrimaryTable primaryTable;

    private String channelAddress;

    /**
     * Is needed when the target device is behind a gateway/bridge which connects Modbus TCP with Modbus+ or Modbus
     * Serial. Note: Some devices requires the unitId even if they are in a Modbus TCP Network and have their own IP.
     * "Like when a device ties its Ethernet and RS-485 ports together and broadcasts whatever shows up on one side,
     * onto the other if the packet isn't for themselves, but isn't "just a bridge"."
     */
    private int unitId;

    public ModbusChannel(String channelAddress, EAccess accessFlag) {

        channelAddress = channelAddress.toLowerCase();
        String[] addressParams = decomposeAddress(channelAddress);
        if (addressParams != null && checkAddressParams(addressParams)) {
            this.channelAddress = channelAddress;
            setUnitId(addressParams[UNITID]);
            setPrimaryTable(addressParams[PRIMARYTABLE]);
            setStartAddress(addressParams[ADDRESS]);
            setDatatype(addressParams[DATATYPE]);
            setCount(addressParams[DATATYPE]);
            setAccessFlag(accessFlag);
            setFunctionCode();
        }
        else {
            throw new RuntimeException("Address initialization faild! Invalid parameters used: " + channelAddress);
        }

    }

    public void update(EAccess access) {
        setAccessFlag(access);
        setFunctionCode();
    }

    private String[] decomposeAddress(String channelAddress) {

        String[] param = new String[4];
        String[] addressParams = channelAddress.toLowerCase().split(":");
        if (addressParams.length == 3) {
            param[UNITID] = "";
            param[PRIMARYTABLE] = addressParams[0];
            param[ADDRESS] = addressParams[1];
            param[DATATYPE] = addressParams[2];
        }
        else if (addressParams.length == 4) {
            param[UNITID] = addressParams[0];
            param[PRIMARYTABLE] = addressParams[1];
            param[ADDRESS] = addressParams[2];
            param[DATATYPE] = addressParams[3];
        }
        else {
            return null;
        }
        return param;
    }

    private boolean checkAddressParams(String[] params) {
        boolean returnValue = false;
        if ((params[UNITID].matches("\\d+?") || params[UNITID].equals(""))
                && PrimaryTable.isValidValue(params[PRIMARYTABLE]) && params[ADDRESS].matches("\\d+?")
                && DataType.isValid(params[DATATYPE])) {
            returnValue = true;
        }
        return returnValue;
    }

    private void setFunctionCode() {
        if (accessFlag.equals(EAccess.READ)) {
            setFunctionCodeForReading();
        }
        else {
            setFunctionCodeForWriting();
        }
    }

    /**
     * Matches data type with function code
     * 
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    private void setFunctionCodeForReading() {

        switch (datatype) {
        case BOOLEAN:
            if (primaryTable.equals(PrimaryTable.COILS)) {
                functionCode = FunctionCode.FC_01_READ_COILS;
            }
            else if (primaryTable.equals(PrimaryTable.DISCRETE_INPUTS)) {
                functionCode = FunctionCode.FC_02_READ_DISCRETE_INPUTS;
            }
            else {
                invalidReadAddressParameterCombination();
            }
            break;
        case SHORT:
        case INT16:
        case INT32:
        case UINT16:
        case UINT32:
        case FLOAT:
        case DOUBLE:
        case LONG:
        case BYTEARRAY:
            if (primaryTable.equals(PrimaryTable.HOLDING_REGISTERS)) {
                functionCode = FunctionCode.FC_03_READ_HOLDING_REGISTERS;
            }
            else if (primaryTable.equals(PrimaryTable.INPUT_REGISTERS)) {
                functionCode = FunctionCode.FC_04_READ_INPUT_REGISTERS;
            }
            else {
                invalidReadAddressParameterCombination();
            }
            break;
        default:
            throw new RuntimeException("read: Datatype " + datatype.toString() + " not supported yet!");
        }
    }

    @SuppressWarnings("deprecation")
    private void setFunctionCodeForWriting() {
        switch (datatype) {
        case BOOLEAN:
            if (primaryTable.equals(PrimaryTable.COILS)) {
                functionCode = FunctionCode.FC_05_WRITE_SINGLE_COIL;
            }
            else {
                invalidWriteAddressParameterCombination();
            }
            break;
        case SHORT:
        case INT16:
        case UINT16:
            if (WRITE_SINGLE_REGISTER) {
                if (primaryTable.equals(PrimaryTable.HOLDING_REGISTERS)) {
                    functionCode = FunctionCode.FC_06_WRITE_SINGLE_REGISTER;
                }
                else {
                    invalidWriteAddressParameterCombination();
                }
                break;
            }
        case INT32:
        case UINT32:
        case FLOAT:
        case DOUBLE:
        case LONG:
        case BYTEARRAY:
            if (primaryTable.equals(PrimaryTable.HOLDING_REGISTERS)) {
                functionCode = FunctionCode.FC_16_WRITE_MULTIPLE_REGISTERS;
            }
            else {
                invalidWriteAddressParameterCombination();
            }
            break;
        default:
            throw new RuntimeException("write: Datatype " + datatype.toString() + " not supported yet!");
        }
    }

    private void invalidWriteAddressParameterCombination() {
        throw new RuntimeException("Invalid channel address parameter combination for writing. \n Datatype: "
                + datatype.toString().toUpperCase() + " PrimaryTable: " + primaryTable.toString().toUpperCase());
    }

    private void invalidReadAddressParameterCombination() {
        throw new RuntimeException("Invalid channel address parameter combination for reading. \n Datatype: "
                + datatype.toString().toUpperCase() + " PrimaryTable: " + primaryTable.toString().toUpperCase());
    }

    private void setStartAddress(String startAddress) {
        this.startAddress = Integer.parseInt(startAddress);
    }

    private void setDatatype(String datatype) {
        this.datatype = DataType.getEnum(datatype);
    }

    private void setUnitId(String unitId) {
        if (unitId.equals("")) {
            this.unitId = IGNORE_UNIT_ID;
        }
        else {
            this.unitId = Integer.parseInt(unitId);
        }
    }

    private void setPrimaryTable(String primaryTable) {
        this.primaryTable = PrimaryTable.getEnumfromString(primaryTable);
    }

    public PrimaryTable getPrimaryTable() {
        return primaryTable;
    }

    private void setCount(String addressParamDatatyp) {
        if (datatype.equals(DataType.BYTEARRAY)) {
            // TODO check syntax first? bytearray[n]

            // special handling of the BYTEARRAY datatyp
            String[] datatypParts = addressParamDatatyp.split("\\[|\\]"); // split string either at [ or ]
            if (datatypParts.length == 2) {
                count = Integer.parseInt(datatypParts[1]);
            }
        }
        else {
            // all other datatyps
            count = datatype.getRegisterSize();
        }
    }

    private void setAccessFlag(EAccess accessFlag) {
        this.accessFlag = accessFlag;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getCount() {
        return count;
    }

    public DataType getDatatype() {
        return datatype;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public EAccess getAccessFlag() {
        return accessFlag;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getChannelAddress() {
        return channelAddress;
    }

    @Override
    public String toString() {
        return "channeladdress: " + unitId + ":" + primaryTable.toString() + ":" + startAddress + ":"
                + datatype.toString();
    }

}
