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

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Options extends LinkedList<Option> {
    private static final long serialVersionUID = -8478314560466205212L;

    private static final Logger logger = LoggerFactory.getLogger(Configurable.class);

    private String separator = SettingsSyntax.SEPARATOR_DEFAULT;
    private String assignment = SettingsSyntax.ASSIGNMENT_OPERATOR_DEFAULT;
    private boolean keyValue = SettingsSyntax.KEY_VAL_PAIRS_DEFAULT;
    private Locale locale = Locale.ENGLISH;

    private int mandatoryCount = 0;

    private Options() {
    }

    private Options(SettingsSyntax syntax) {
    	if (syntax == null) {
    		separator = SettingsSyntax.SEPARATOR_DEFAULT;
    		assignment = SettingsSyntax.ASSIGNMENT_OPERATOR_DEFAULT;
    		keyValue = SettingsSyntax.KEY_VAL_PAIRS_DEFAULT;
    	}
    	else {
    		separator = syntax.separator();
    		assignment = syntax.assignmentOperator();
    		keyValue = syntax.keyValuePairs();
    	}
    }

    private Options(AddressSyntax syntax) {
    	if (syntax == null) {
    		separator = AddressSyntax.SEPARATOR_DEFAULT;
    		assignment = AddressSyntax.ASSIGNMENT_OPERATOR_DEFAULT;
    		keyValue = AddressSyntax.KEY_VAL_PAIRS_DEFAULT;
    	}
    	else {
    		separator = syntax.separator();
    		assignment = syntax.assignmentOperator();
    		keyValue = syntax.keyValuePairs();
    	}
    }

    @Override
    public boolean add(Option option) {
        if (option.isMandatory()) mandatoryCount++;
        return super.add(option);
    }

    int getMandatoryCount() {
    	return mandatoryCount;
    }

    public String getSeparator() {
        return separator;
    }

    Options setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public String getAssignmentOperator() {
        return assignment;
    }

    Options setAssignmentOperator(String assignment) {
        this.assignment = assignment;
        return this;
    }

    public boolean hasKeyValuePairs() {
        return keyValue;
    }

    Options setKeyValuePairs(boolean enable) {
        if (!enable) {
            this.assignment = null;
        }
        this.keyValue = enable;
        return this;
    }

    void setSyntax(String separator) {
        this.separator = separator;
        this.assignment = null;
        this.keyValue = false;
    }

    void setSyntax(String separator, String assignment) {
        this.separator = separator;
        this.assignment = assignment;
        this.keyValue = true;
    }

    Locale getLocale() {
        return locale;
    }

    void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getSyntax() {

        StringBuilder sb = new StringBuilder();

        if (size() > 0) {
            sb.append("Synopsis: ");
            boolean first = true;
            for (Option option : this) {
                boolean mandatory = option.isMandatory();
                String key = option.getId();
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

	public static <C extends Configurable> Options parseAddress(Class<C> configs) {
    	//Class<C> configs = (Class<C>) MethodHandles.lookup().lookupClass();
        try {
            List<Field> fields = new LinkedList<Field>();
            Class<?> clazz = configs;
            while(clazz.getSuperclass() != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
        	AddressSyntax syntax = configs.getAnnotation(AddressSyntax.class);
        	Options options = new Options(syntax);
        	
            for (Field field : fields) {
                Address annotation = field.getAnnotation(Address.class);
                if (annotation == null) {
                    continue;
                }
                String[] ids = annotation.id();
                if (Arrays.stream(ids).anyMatch(s -> s.isEmpty() || s.equals(Address.DEFAULT))) {
                	String id = annotation.value();
                	
                    if (id.isEmpty() || id.equals(Address.DEFAULT)) {
                    	id = field.getName();
    				}
                    ids = new String[] { id };
                }
                Option option = parseOption(ids, field.getType());
                
                if (!annotation.name().equals(Address.DEFAULT)) {
                	option.setName(annotation.name());
                }
                if (!annotation.description().equals(Address.DEFAULT)) {
                	option.setDescription(annotation.description());
                }
                
                if (!annotation.valueDefault().equals(Address.DEFAULT)) {
                	option.setValueDefault(parseValue(option.getType(), annotation.valueDefault()));
                }
                if (!annotation.valueSelection().equals(Address.DEFAULT)) {
                	option.setValueSelection(new OptionSelection(option.getType(), annotation.valueSelection()));
                }
            	option.setMandatory(annotation.mandatory());
                options.add(option);
            }
            return options;
            
        } catch (Exception e) {
            logger.warn("Error parsing {} address: {}", configs.getSimpleName(), e.getMessage());
        }
        return null;
	}

	public static <C extends Configurable> Options parseSettings(Class<C> configs) {
    	//Class<C> configs = (Class<C>) MethodHandles.lookup().lookupClass();
        try {
            List<Field> fields = new LinkedList<Field>();
            Class<?> clazz = configs;
            while(clazz.getSuperclass() != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
        	SettingsSyntax syntax = configs.getAnnotation(SettingsSyntax.class);
        	Options options = new Options(syntax);
        	
            for (Field field : fields) {
                Setting annotation = field.getAnnotation(Setting.class);
                if (annotation == null) {
                    continue;
                }
                String[] ids = annotation.id();
                if (Arrays.stream(ids).anyMatch(s -> s.isEmpty() || s.equals(Setting.DEFAULT))) {
                	String id = annotation.value();
                	
                    if (id.isEmpty() || id.equals(Setting.DEFAULT)) {
                    	id = field.getName();
    				}
                    ids = new String[] { id };
                }
                Option option = parseOption(ids, field.getType());
                
                if (!annotation.name().equals(Setting.DEFAULT)) {
                	option.setName(annotation.name());
                }
                if (!annotation.description().equals(Setting.DEFAULT)) {
                	option.setDescription(annotation.description());
                }
                
                if (!annotation.valueDefault().equals(Setting.DEFAULT)) {
                	option.setValueDefault(parseValue(option.getType(), annotation.valueDefault()));
                }
                if (!annotation.valueSelection().equals(Setting.DEFAULT)) {
                	option.setValueSelection(new OptionSelection(option.getType(), annotation.valueSelection()));
                }
            	option.setMandatory(annotation.mandatory());
                options.add(option);
            }
            return options;
            
        } catch (Exception e) {
            logger.warn("Error parsing {} settings: {}", configs.getSimpleName(), e.getMessage());
        }
        return null;
	}

    private static Option parseOption(String[] ids, Class<?> type) {
        Option option = new Option(ids);
        
        if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
        	option.setType(ValueType.BOOLEAN);
        }
        else if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
        	option.setType(ValueType.BYTE);
        }
        else if (type.isAssignableFrom(byte[].class) || type.isAssignableFrom(Byte[].class)) {
        	option.setType(ValueType.BYTE_ARRAY);
        }
        else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
        	option.setType(ValueType.SHORT);
        }
        else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
        	option.setType(ValueType.INTEGER);
        }
        else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
        	option.setType(ValueType.LONG);
        }
        else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
        	option.setType(ValueType.FLOAT);
        }
        else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
        	option.setType(ValueType.DOUBLE);
        }
        else {
        	option.setType(ValueType.STRING);
        }
        return option;
    }

    static Value parseValue(ValueType type, String valueStr) throws ArgumentSyntaxException {
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
                    arr = Option.hexToBytes(valueStr.substring(2).trim());
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

    static Options getFromDomNode(Node node, Map<String, Option> options) throws ParseException {
        
        Options collection = new Options();
        
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

    static Options getFromDomNode(Node node) throws ParseException {
        return getFromDomNode(node, null);
    }

}
