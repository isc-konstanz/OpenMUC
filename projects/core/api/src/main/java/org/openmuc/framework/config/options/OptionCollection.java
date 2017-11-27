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
package org.openmuc.framework.config.options;

import java.util.LinkedList;
import java.util.Locale;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.data.BooleanValue;
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
    private boolean disabled = false;
    
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

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean enable) {
        this.disabled = enable;
    }

    public void disable() {
        this.disabled = true;
    }

    @Override
    public Preferences parse(String settingsStr) throws UnsupportedOperationException, ArgumentSyntaxException {
        if (settingsStr != null) {
            Preferences settings = new Preferences();
            
            if (!settingsStr.trim().isEmpty()) {
                String[] settingsArray = settingsStr.trim().split(separator);
                
                if (settingsArray.length >= mandatoryOptCount && settingsArray.length <= size()) {
                    if (keyValue) {
                        for (Option option : this) {
                            boolean mandatoryOptMissing = option.isMandatory() ? true : false;
                            
                            String key = option.getKey();
                            Value value = null;
                            for (String setting : settingsArray) {
                                String[] keyValue = setting.trim().split(assignment);
                                if (keyValue.length == 2) {
                                    if (keyValue[0].trim().equalsIgnoreCase(key)) {
                                        mandatoryOptMissing = false;
                                        
                                        value = parseValue(option.getType(), keyValue[1]);
                                    }
                                }
                                else {
                                    throw new ArgumentSyntaxException("Parameter is not a key value pair of type "
                                                + "<key>" + assignment + "<value> in parsed Settings: " + setting);
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
                                
                                settings.parameters.put(key, value);
                            }
                        }
                    }
                    else {
                        int optionalOptCount = 0;
                        
                        int i = 0;
                        for (Option option : this) {
                            if (i >= settingsArray.length) {
                                break;
                            }
                            else if (option.isMandatory() || mandatoryOptCount+optionalOptCount < settingsArray.length) {
                                
                                Value value = parseValue(option.getType(), settingsArray[i]);
                                if (option.getValueSelection() != null 
                                        && option.getValueSelection().hasValidation() 
                                        && !option.getValueSelection().contains(value)) {
                                    throw new ArgumentSyntaxException("Parameter value not a valid selection: " + value.toString());
                                }
                                settings.parameters.put(option.getKey(), value);
                                
                                if (!option.isMandatory()) {
                                    optionalOptCount++;
                                }
                                i++;
                            }
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
        else throw new ArgumentSyntaxException("Null value passed to be parsed as Settings");
    }
    
    private Value parseValue(ValueType type, String valueStr) throws ArgumentSyntaxException {
        
        Value value;
        switch (type) {
        case BOOLEAN:
            value = new BooleanValue(Boolean.valueOf(valueStr));
            break;
        case BYTE:
            value = new ByteValue(Byte.valueOf(valueStr));
            break;
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
                
                String syntax;
                if (keyValue) {
                    syntax = key + assignment + '<' + key.toLowerCase(locale) + '>';
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

    public static OptionCollection getFromDomNode(Node node) throws ParseException {
        
        OptionCollection collection = new OptionCollection();
        
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String childNodeName = childNode.getNodeName();
            if (childNodeName.equals("#text")) {
                continue;
            }
            else if (childNodeName.equals("disabled")) {
            	String disabledString = childNode.getTextContent().toLowerCase();
                if (disabledString.equals("true")) {
                	collection.setDisabled(true);
                }
                else if (disabledString.equals("false")) {
                	collection.setDisabled(false);
                }
                else {
                    throw new ParseException("Option \"disabled\" contains neither \"true\" nor \"false\"");
                }
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
                        String keyValString = syntaxNode.getTextContent().toLowerCase();
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
                            collection.setAssignmentOperator(nameAttribute.getTextContent());
                        }
                    }
                    else if (syntaxNodeName.equals("separator")) {
                        collection.setSeparator(syntaxNode.getTextContent());
                    }
                    else {
                        throw new ParseException("Unknown tag found:" + syntaxNodeName);
                    }
                }
            }
            else if (childNodeName.equals("option")) {
                collection.add(Option.getFromDomNode(childNode));
            }
            else {
                throw new ParseException("Unknown tag found:" + childNodeName);
            }
        }
        
        return collection;
    }

}
