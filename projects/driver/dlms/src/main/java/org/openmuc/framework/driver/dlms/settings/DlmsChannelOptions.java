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
package org.openmuc.framework.driver.dlms.settings;

import org.openmuc.framework.config.info.ChannelOptions;
import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.data.ValueType;

public class DlmsChannelOptions extends ChannelOptions {
    
    private static final String DESCRIPTION = "A channel references a single attribute or method of a COSEM Interface Object.</br>"
        + "To uniquely identify an Objects attribute or method, the <em>Class ID</em>, the <em>Logical Name</em> and <em>Attribute</em> or <em>Method</em> ID is needed.</br></br>"
        + "For a list of all valid Logical Names and the corresponding COSEM Interface Class, consult the list of standardized OBIS codes administered by the DLMS UA "
        + "<a href='http://dlms.com/documentation/listofstandardobiscodesandmaintenanceproces/index.html'>here</a>.";

    public static final String CLASS_ID = "classId";
    public static final String INSTANCE_ID = "instanceId";
    public static final String ATTRIBUTE_ID = "attributeId";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void configureAddress(OptionCollection address) {
        address.setSyntax("/");

        address.add(classId());
        address.add(instanceId());
        address.add(attributeId());
    }

    @Override
    protected void configureScanSettings(OptionCollection scanSettings) {
    	scanSettings.disable();
    }

    private Option classId() {
        
        Option classId = new Option(CLASS_ID, "Class ID", ValueType.INTEGER);
        classId.setDescription("The COSEM class ID is a 16 bit unsigned number and can be found in the list of standardized OBIS codes.");
        classId.setMandatory(true);
        
        return classId;
    }

    private Option instanceId() {
        
        Option instanceId = new Option(INSTANCE_ID, "Logical Name", ValueType.STRING);
        instanceId.setDescription("A logical name is a 6 byte OBIS code as it is defined by the DLMS UA. It sometimes also called <em>instance ID</em>.</br>"
                + "It can be written as hexadecimal number (e.g. 0101010800FF) or as a series of six decimal numbers separated by periods A-B:C.D.E*F.</br></br>"
                + "<b>Example:</b>  The clock of a smart meter is always reachable under the address [0, 0, 1, 0, 0, 255].");
        instanceId.setMandatory(true);
        
        return instanceId;
    }

    private Option attributeId() {
        
        Option attributeId = new Option(ATTRIBUTE_ID, "Attribute/Method ID", ValueType.INTEGER);
        attributeId.setDescription("The COSEM attribute/method ID is a 16 bit unsigned number and depends on the class ID.</br>"
                + "It can be extracted best by consulting the document IEC 62056-6-2 or the Blue Book from the DLMS UA.</br>"
                + "Usually the first attribute (attribute ID 1) of an COSEM interface class (IC) is the logical name of the object. "
                + "Further attributes refer to actual data (see section 4.5 of IEC 62056-6-2).");
        attributeId.setMandatory(true);
        
        return attributeId;
    }

}
