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
package org.openmuc.framework.config.options;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.openmuc.framework.config.ArgumentSyntaxException;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OptionCollection extends LinkedList<Option> implements OptionInfo {
    private static final long serialVersionUID = -8478314560466205212L;

    public static final String SEPARATOR_DEFAULT = ",";
    public static final String ASSIGNMENT_DEFAULT = ":";
    public static final boolean KEY_VAL_DEFAULT = true;

    private String separator = SEPARATOR_DEFAULT;
    private String assignment = ASSIGNMENT_DEFAULT;
    private boolean keyValue = KEY_VAL_DEFAULT;
    private Locale locale = Locale.ENGLISH;

    private int mandatoryOptCount = 0;


    @Override
    public boolean add(Option option) {
        if (option.isMandatory()) mandatoryOptCount++;
        return super.add(option);
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getAssignmentOperator() {
        return assignment;
    }

    public void setAssignmentOperator(String assignment) {
        this.assignment = assignment;
    }

    public boolean hasKeyValuePairs() {
        return keyValue;
    }

    public void setKeyValuePairs(boolean enable) {
        if (!enable) {
            this.assignment = null;
        }
        this.keyValue = enable;
    }

    public void setSyntax(String separator) {
        this.separator = separator;
        this.assignment = null;
        this.keyValue = false;
    }

    public void setSyntax(String separator, String assignment) {
        this.separator = separator;
        this.assignment = assignment;
        this.keyValue = true;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Map<String, Value> parse(String settingsStr) throws UnsupportedOperationException, ArgumentSyntaxException {
        if (settingsStr != null) {
            Map<String, Value> settings = new HashMap<>();
            
            if (!settingsStr.trim().isEmpty()) {
                String[] settingsArray = settingsStr.trim().split(separator);
                
                if (settingsArray.length >= mandatoryOptCount && settingsArray.length <= size()) {
                    if (keyValue) {
                        for (Option option : this) {
                            boolean mandatoryOptMissing = option.isMandatory() ? true : false;
                            
                            String key = option.getKey();
                            Value value = null;
                            for (String setting : settingsArray) {
                                String[] keyValue = setting.trim().split(assignment, 2);
                                if (keyValue.length != 2) {
                                    throw new ArgumentSyntaxException("Parameter is not a key value pair of type " 
                                    		+ "<key>" + assignment + "<value> in parsed Settings: " + setting);
                                }
                                if (keyValue[0].trim().equalsIgnoreCase(key)) {
                                    if (keyValue[1].trim().isEmpty()) {
                                    	throw new ArgumentSyntaxException("Parameter " + key + "is empty");
                                    }
                                    mandatoryOptMissing = false;
                                    
                                    value = parseValue(option.getType(), keyValue[1].trim());
                                }
                            }
                            if (mandatoryOptMissing) {
                                throw new ArgumentSyntaxException("Mandatory parameter " + key + " is not present in parsed Settings");
                            }
                            
                            if (value != null) {
                                if (option.getValueSelection() != null 
                                        && option.getValueSelection().hasValidation() 
                                        && !option.getValueSelection().contains(value)) {
                                    throw new ArgumentSyntaxException("Parameter value not a valid selection: " + value.toString());
                                }
                            }
                            else if (option.getValueDefault() != null) {
                                value = option.getValueDefault();
                            }
                            settings.put(key, value);
                        }
                    }
                    else {
                        int optionalOptCount = 0;
                        
                        int i = 0;
                        for (Option option : this) {
                            Value value = null;
                            
                            if (i >= settingsArray.length) {
                                break;
                            }
                            else if (option.isMandatory() || mandatoryOptCount+optionalOptCount < settingsArray.length) {
                                value = parseValue(option.getType(), settingsArray[i].trim());
                                
                                if (!option.isMandatory()) {
                                    optionalOptCount++;
                                }
                                i++;
                            }
                            
                            if (value != null) {
                                if (option.getValueSelection() != null 
                                        && option.getValueSelection().hasValidation() 
                                        && !option.getValueSelection().contains(value)) {
                                    throw new ArgumentSyntaxException("Parameter value not a valid selection: " + value.toString());
                                }
                            }
                            else if (option.getValueDefault() != null) {
                                value = option.getValueDefault();
                            }
                            settings.put(option.getKey(), value);
                        }
                    }
                }
                else if (settingsArray.length < mandatoryOptCount) {
                    throw new ArgumentSyntaxException("Mandatory parameters not configured in settings string");
                }
                else if (settingsArray.length > size()) {
                    throw new ArgumentSyntaxException("Too many parameters in passed Settings to be parsed.");
                }
            }
            else if (mandatoryOptCount > 0) {
                throw new ArgumentSyntaxException("Mandatory parameters not configured in empty settings string");
            }
            
            return settings;
        }
        throw new ArgumentSyntaxException("Null value passed to be parsed as settings");
    }
    
    private Value parseValue(ValueType type, String valueStr) throws ArgumentSyntaxException {
        
        Value value;
        switch (type) {
        case DOUBLE:
            value = new DoubleValue(Double.valueOf(valueStr));
            break;
        case FLOAT:
            value = new FloatValue(Float.valueOf(valueStr));
            break;
        case INTEGER:
            value = new IntValue(Integer.valueOf(valueStr));
            break;
        case LONG:
            value = new LongValue(Long.valueOf(valueStr));
            break;
        case SHORT:
            value = new ShortValue(Short.valueOf(valueStr));
            break;
        case BYTE:
            value = new ByteValue(Byte.valueOf(valueStr));
            break;
        case BOOLEAN:
            value = new BooleanValue(Boolean.valueOf(valueStr));
            break;
        case BYTE_ARRAY:
            byte[] arr;
            if (!valueStr.startsWith("0x")) {
                arr = valueStr.getBytes(StandardCharsets.US_ASCII);
            }
            else {
                try {
                    arr = DatatypeConverter.parseHexBinary(valueStr.substring(2).trim());
                } catch (IllegalArgumentException e) {
                    throw new ArgumentSyntaxException("Unable to parse value as byte array: " + valueStr);
                }
            }
            value = new ByteArrayValue(arr);
            break;
        case STRING:
            value = new StringValue(valueStr);
            break;
        default:
            throw new ArgumentSyntaxException("Parameter value type not configured: " + type.name().toLowerCase());
        }
        
        return value;
    }

    @Override
    public String getSyntax() {

        StringBuilder sb = new StringBuilder();

        if (size() > 0) {
            sb.append("Synopsis: ");
            boolean first = true;
            for (Option option : this) {
                boolean mandatory = option.isMandatory();
                String key = option.getKey();
                String value = null;
                
                int i = 0;
                if (option.getValueSelection() != null) {
                    StringBuilder ssb = new StringBuilder();
                    for (Value val : option.getValueSelection().keySet()) {
                        if (i>0) ssb.append('/');
                        i++;
                        
                        ssb.append(val.asString());
                    }
                    value = ssb.toString();
                }
                else if (keyValue) {
                    value = option.getType().name().replace('_', ' ').toLowerCase(locale);
                }
                
                String syntax;
                if (keyValue) {
                    syntax = key + assignment + '<' + value + '>';
                }
                else {
                    syntax = '<' + key + '>';
                }
                
                if (!mandatory) sb.append('[');
                if (!first) {
                    sb.append(separator);
                }
                sb.append(syntax);
                if (!mandatory) sb.append(']');
                
                first = false;
            }
        }
        else {
            sb.append("N.A.");
        }
        return sb.toString();
    }

    public static OptionCollection getFromDomNode(Node node, Map<String, Option> options) throws ParseException {
        
        OptionCollection collection = new OptionCollection();
        
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String childNodeName = childNode.getNodeName();
            if (childNodeName.equals("#text")) {
                continue;
            }
            else if (childNodeName.equals("syntax")) {
                NodeList syntaxNodes = childNode.getChildNodes();
                for (int j = 0; j < syntaxNodes.getLength(); j++) {
                    Node syntaxNode = syntaxNodes.item(j);
                    String syntaxNodeName = syntaxNode.getNodeName();
                    
                    if (syntaxNodeName.equals("#text")) {
                        continue;
                    }
                    else if (syntaxNodeName.equals("keyValue")) {
                        String keyValString = syntaxNode.getTextContent().trim().toLowerCase();
                        if (keyValString.equals("true")) {
                            collection.setKeyValuePairs(true);
                        }
                        else if (keyValString.equals("false")) {
                            collection.setKeyValuePairs(false);
                        }
                        else {
                            throw new ParseException("Syntax \"keyValue\" contains neither \"true\" nor \"false\"");
                        }
                        
                        NamedNodeMap attributes = syntaxNode.getAttributes();
                        Node nameAttribute = attributes.getNamedItem("assignment");
                        if (nameAttribute != null) {
                            collection.setAssignmentOperator(nameAttribute.getTextContent().trim());
                        }
                    }
                    else if (syntaxNodeName.equals("separator")) {
                        collection.setSeparator(syntaxNode.getTextContent().trim());
                    }
                    else {
                        throw new ParseException("Unknown tag found:" + syntaxNodeName);
                    }
                }
            }
            else if (childNodeName.equals("option")) {
                NamedNodeMap attributes = childNode.getAttributes();
                Node nameAttribute = attributes.getNamedItem("id");
                if (nameAttribute == null) {
                    throw new ParseException("Option has no id attribute");
                }
                String id = nameAttribute.getTextContent().trim();
                
                Option option;
                if (options != null && options.containsKey(id) && !childNode.hasChildNodes()) {
                    option = options.get(id);
                }
                else {
                    option = Option.getFromDomNode(id, childNode);
                    if (options != null) {
                        options.put(id, option);
                    }
                }
                collection.add(option);
            }
            else {
                throw new ParseException("Unknown tag found:" + childNodeName);
            }
        }
        
        return collection;
    }

    public static OptionCollection getFromDomNode(Node node) throws ParseException {
        return getFromDomNode(node, null);
    }

}
