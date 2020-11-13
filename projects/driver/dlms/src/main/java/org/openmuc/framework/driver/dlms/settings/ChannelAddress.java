/*
 * Copyright 2011-2020 Fraunhofer ISE
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
package org.openmuc.framework.driver.dlms.settings;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.address.Address;
import org.openmuc.framework.config.address.AddressSyntax;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@AddressSyntax(separator = ";", assignmentOperator = "=", keyValuePairs = true)
public class ChannelAddress extends Configurable {

    private static final String LOGICAL_NAME_FORMAT = "<Interface_Class_ID>/<Instance_ID>/<Object_Attribute_ID>";

    @Address(id = "a",
             name = "Address",
             description = "The Address in logical name format "+LOGICAL_NAME_FORMAT
    )
    private String address;

    @Address(id = "t",
             name = "Data Object Type",
    		 valueSelection = "NULL_DATA:Null," +
		    		          "ARRAY:Array," +
		    		          "STRUCTURE:Structure," +
		    		          "BOOLEAN:Bool," +
		    		          "BIT_STRING:Bit String," +
		    		          "DOUBLE_LONG:Integer 32," +
		    		          "DOUBLE_LONG_UNSIGNED:Unsigned integer 32," +
		    		          "OCTET_STRING:Octet String," +
		    		          "UTF8_STRING:UTF-8 String," +
		    		          "VISIBLE_STRING:Visible String," +
		    		          "BCD:BCD," +
		    		          "INTEGER:Integer 8," +
		    		          "LONG_INTEGER:Integer 16," +
		    		          "UNSIGNED:Unsigned integer 8," +
		    		          "LONG_UNSIGNED:Unsigned integer 16," +
		    		          "COMPACT_ARRAY:Compact array," +
		    		          "LONG64:Integer 64," +
		    		          "LONG64_UNSIGNED:Unsigned integer 64," +
		    		          "ENUMERATE:Enum," +
		    		          "FLOAT32:Float 32," +
		    		          "FLOAT64:Float 64," +
		    		          "DATE_TIME:Date Time," +
		    		          "DATE:Date," +
		    		          "TIME:Time," +
		    		          "DONT_CARE:None"
    )
    private Type type;

    private AttributeAddress attributeAddress;

    public ChannelAddress(String parameters) throws ArgumentSyntaxException {
        configureAddress(parameters);

        String[] arguments = address.split("/");
        if (arguments.length != 3) {
            String msg = String.format("Wrong number of DLMS/COSEM address arguments. %s", LOGICAL_NAME_FORMAT);
            throw new ArgumentSyntaxException(msg);
        }
        int classId = Integer.parseInt(arguments[0]);
        ObisCode instanceId = new ObisCode(arguments[1]);
        int attributeId = Integer.parseInt(arguments[2]);

        attributeAddress = new AttributeAddress(classId, instanceId, attributeId);
    }

    public String getAddress() {
        return address;
    }

    public Type getType() {
        return type;
    }

    public AttributeAddress getAttributeAddress() {
        return attributeAddress;
    }

}
