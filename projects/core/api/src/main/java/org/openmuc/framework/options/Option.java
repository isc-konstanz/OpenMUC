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
package org.openmuc.framework.options;

import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Option {

    public static final boolean MANDATORY_DEFAULT = false;
    public static final ValueType TYPE_DEFAULT = ValueType.DOUBLE;

    private final String[] ids;
    private String name = null;
    private String description = null;
    private boolean mandatory = MANDATORY_DEFAULT;
    private ValueType type = TYPE_DEFAULT;

    private Value valueDefault = null;
    private OptionSelection valueSelection = null;

    Option(String id, String name, ValueType type, 
            boolean mandatory, String description, 
            Value valueDefault, OptionSelection valueSelection) {
        
        this(id);
        this.name = name;
        this.type = type;
        this.mandatory = mandatory;
        this.description = description;
        this.valueDefault = valueDefault;
        this.valueSelection = valueSelection;
    }

    Option(String id) {
        this(new String[] { id });
    }

    Option(String[] ids) {
        this.ids = ids;
    }

    public String getId() {
        return this.ids[0];
    }

    public String[] getIds() {
        return this.ids;
    }

    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public ValueType getType() {
        return this.type;
    }

    void setType(ValueType type) {
        this.type = type;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public Value getValueDefault() {
        return valueDefault;
    }

    void setValueDefault(Value valueDefault) {
        this.valueDefault = valueDefault;
    }

    public OptionSelection getValueSelection() {
        return valueSelection;
    }

    void setValueSelection(OptionSelection valueSelection) {
        this.valueSelection = valueSelection;
    }

    static Option getFromDomNode(String id, Node node) throws ParseException {
        
        Option option = new Option(id);
        Node valueDefaultNode = null;
        Node valueSelectionNode = null;
        
        NodeList childNodes = node.getChildNodes();
        try {
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                String childNodeName = childNode.getNodeName();

                if (childNodeName.equals("#text")) {
                    continue;
                }
                else if (childNodeName.equals("name")) {
                    option.name = childNode.getTextContent().trim();
                }
                else if (childNodeName.equals("description")) {
                    option.description = DriverOptions.trimTextFromDomNode(childNode);
                }
                else if (childNodeName.equals("mandatory")) {
                    String mandatoryString = childNode.getTextContent().trim().toLowerCase();
                    if (mandatoryString.equals("true")) {
                        option.mandatory = true;
                    }
                    else if (mandatoryString.equals("false")) {
                        option.mandatory = false;
                    }
                    else {
                        throw new ParseException("Option \"mandatory\" contains neither \"true\" nor \"false\"");
                    }
                }
                else if (childNodeName.equals("type")) {
                    String valueTypeString = childNode.getTextContent().trim().toUpperCase();

                    try {
                        option.type = ValueType.valueOf(valueTypeString);
                    } catch (IllegalArgumentException e) {
                        throw new ParseException("Unknown option value type found:" + valueTypeString);
                    }
                }
                else if (childNodeName.equals("default")) {
                    valueDefaultNode = childNode;
                }
                else if (childNodeName.equals("selection")) {
                    valueSelectionNode = childNode;
                }
                else {
                    throw new ParseException("Unknown tag found:" + childNodeName);
                }
            }
            if (option.name == null) {
                option.name = id;
            }
            if (valueDefaultNode != null) {
                Value valueDefault = new StringValue(valueDefaultNode.getTextContent().trim());
                
                // Verify default values to be of the specified value type
                switch (option.type) {
                case FLOAT:
                    option.valueDefault = new FloatValue(valueDefault.asFloat());
                    break;
                case DOUBLE:
                    option.valueDefault = new DoubleValue(valueDefault.asDouble());
                    break;
                case SHORT:
                    option.valueDefault = new ShortValue(valueDefault.asShort());
                    break;
                case INTEGER:
                    option.valueDefault = new IntValue(valueDefault.asInt());
                    break;
                case LONG:
                    option.valueDefault = new LongValue(valueDefault.asLong());
                    break;
                case BYTE:
                    option.valueDefault = new ByteValue(valueDefault.asByte());
                    break;
                case BYTE_ARRAY:
                    byte[] arr;
                    if (!valueDefault.asString().startsWith("0x")) {
                        arr = valueDefault.asByteArray();
                    }
                    else {
                        try {
                            arr = Option.hexToBytes(valueDefault.asString().substring(2).trim());
                        } catch (IllegalArgumentException e) {
                            throw new ParseException(e);
                        }
                    }
                    option.valueDefault = new ByteArrayValue(arr);
                    break;
                case BOOLEAN:
                    option.valueDefault = new BooleanValue(valueDefault.asBoolean());
                    break;
                case STRING:
                    option.valueDefault = valueDefault;
                    break;
                default:
                    break;
                }
            }
            if (valueSelectionNode != null) {
                option.valueSelection = OptionSelection.getFromDomNode(valueSelectionNode, option.type);
            }
            
        } catch (IllegalArgumentException e) {
            throw new ParseException(e);
        }
        
        return option;
    }

    static byte[] hexToBytes(String s) {
        byte[] b = new byte[s.length() / 2];
        int index;

        for (int i = 0; i < b.length; i++) {
            index = i * 2;
            b[i] = (byte) Integer.parseInt(s.substring(index, index + 2), 16);
        }
        return b;
    }

}