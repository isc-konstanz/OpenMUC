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
package org.openmuc.framework.driver.dlms.settings;

import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Preferences;
import org.openmuc.framework.data.Value;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject.Type;

public class ChannelAddress extends Preferences {

    private static final String LOGICAL_NAME_FORMAT = "<Interface_Class_ID>/<Instance_ID>/<Object_Attribute_ID>";

    @Option("a")
    private String address;
    
    @Option("t")
    private Type type;

    private AttributeAddress attributeAddress;

    @Override
    public int parseFields(Map<String, Value> settings) throws ArgumentSyntaxException {
        int setFieldCounter = super.parseFields(settings);

        String[] arguments = address.split("/");
        if (arguments.length != 3) {
            String msg = String.format("Wrong number of DLMS/COSEM address arguments. %s", LOGICAL_NAME_FORMAT);
            throw new ArgumentSyntaxException(msg);
        }
        int classId = Integer.parseInt(arguments[0]);
        ObisCode instanceId = new ObisCode(arguments[1]);
        int attributeId = Integer.parseInt(arguments[2]);

        attributeAddress = new AttributeAddress(classId, instanceId, attributeId);

        return setFieldCounter;
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
