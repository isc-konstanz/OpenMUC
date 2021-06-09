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
package org.openmuc.framework.config.option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.OptionType;
import org.openmuc.framework.config.option.annotation.Syntax;
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

public class Options extends ArrayList<OptionValue> {
    private static final long serialVersionUID = -8478314560466205212L;

    private static final Logger logger = LoggerFactory.getLogger(Options.class);

    private final OptionType type;
    private final OptionSyntax syntax;

    private Locale locale = Locale.ENGLISH;

    private int mandatoryCount = 0;

    private Options(OptionType type) {
        this.type = type;
        this.syntax = new OptionSyntax(type);
    }

    private Options(OptionType type, OptionSyntax syntax) {
        this.type = type;
        this.syntax = syntax;
    }

    private Options(OptionType type, Syntax syntax) {
        this(type, new OptionSyntax(type, syntax));
    }

    public OptionType getType() {
        return type;
    }

    @Override
    public boolean add(OptionValue option) {
        if (option.isMandatory()) mandatoryCount++;
        return super.add(option);
    }

    public int getMandatoryCount() {
        return mandatoryCount;
    }

    public OptionSyntax getSyntax() {
    	return syntax;
    }

    public String getSynopsis() {

        StringBuilder sb = new StringBuilder();

        if (size() > 0) {
            sb.append("Synopsis: ");
            boolean first = true;
            for (OptionValue option : this) {
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
                else if (syntax.hasKeyValuePairs()) {
                    value = option.getType().name().replace('_', ' ').toLowerCase(locale);
                }
                
                String synopsis;
                if (syntax.hasKeyValuePairs()) {
                    synopsis = key + syntax.getAssignment() + '<' + value + '>';
                }
                else {
                    synopsis = '<' + key + '>';
                }
                
                if (!mandatory) sb.append('[');
                if (!first) {
                    sb.append(syntax.getSeparator());
                }
                sb.append(synopsis);
                if (!mandatory) sb.append(']');
                
                first = false;
            }
        }
        else {
            sb.append("N.A.");
        }
        return sb.toString();
    }

    Locale getLocale() {
        return locale;
    }

    void setLocale(Locale locale) {
        this.locale = locale;
    }

    public static Options parse(OptionType type, Class<? extends Configurable> configs) {
    	return Options.parse(type, new OptionSyntax(type, configs), configs);
    }

    public static Options parse(OptionType type, OptionSyntax syntax, Class<? extends Configurable> configs) {
        //Class<C> configs = (Class<C>) MethodHandles.lookup().lookupClass();
        try {
            List<Field> fields = new LinkedList<Field>();
            Class<?> clazz = configs;
            while(clazz.getSuperclass() != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
            Options options = new Options(type, syntax);
            
            for (Field field : fields) {
                Option annotation = field.getAnnotation(Option.class);
                if (annotation == null || 
                        annotation.type() != type) {
                    
                    continue;
                }
                String[] ids = annotation.id();
                if (Arrays.stream(ids).anyMatch(s -> s.isEmpty() || s.equals(Option.DEFAULT))) {
                    ids = annotation.value();
                }
                if (Arrays.stream(ids).anyMatch(s -> s.isEmpty() || s.equals(Option.DEFAULT))) {
                    ids = new String[] { field.getName() };
                }
                OptionValue option = parseOption(ids, field.getType());
                
                if (!annotation.name().equals(Option.DEFAULT)) {
                    option.setName(annotation.name());
                }
                if (!annotation.description().equals(Option.DEFAULT)) {
                    option.setDescription(annotation.description());
                }
                
                if (!annotation.valueDefault().equals(Option.DEFAULT)) {
                    option.setValueDefault(parseValue(option.getType(), annotation.valueDefault()));
                }
                if (!annotation.valueSelection().equals(Option.DEFAULT)) {
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

    static OptionValue parseOption(String[] ids, Class<?> type) {
        OptionValue option = new OptionValue(ids);
        
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

    public static Value parseValue(ValueType type, String valueStr) throws ArgumentSyntaxException {
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
                    arr = OptionValue.hexToBytes(valueStr.substring(2).trim());
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

    public static String trimDomNodeText(Node node) {
        BufferedReader reader = new BufferedReader(new StringReader(node.getTextContent()));
        StringBuffer result = new StringBuffer();
        try {
            String line;
            while ( (line = reader.readLine() ) != null)
                result.append(line.replaceAll("^\\s+", "").replace("\n", "").replace("\r", ""));
            
            return result.toString();
        } catch (IOException e) {
            logger.info("Error while trimming text: {}", e.getMessage());
        }
        return null;
    }

    public static Options fromDomNode(Node node, Map<String, OptionValue> optionValues) throws ParseException {
        Options options;
        String nodeName = node.getNodeName();
        if (nodeName.toLowerCase().contains("address")) {
            options = new Options(OptionType.ADDRESS);
        }
        else {
            options = new Options(OptionType.SETTING);
        }
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
                            options.getSyntax().setKeyValuePairs(true);
                        }
                        else if (keyValString.equals("false")) {
                            options.getSyntax().setKeyValuePairs(false);
                        }
                        else {
                            throw new ParseException("Syntax \"keyValue\" contains neither \"true\" nor \"false\"");
                        }
                        
                        NamedNodeMap attributes = syntaxNode.getAttributes();
                        Node nameAttribute = attributes.getNamedItem("assignment");
                        if (nameAttribute != null) {
                            options.getSyntax().setAssignmentOperator(nameAttribute.getTextContent());
                        }
                    }
                    else if (syntaxNodeName.equals("separator")) {
                        options.getSyntax().setSeparator(syntaxNode.getTextContent());
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
                
                OptionValue option;
                if (optionValues != null && optionValues.containsKey(id) && !childNode.hasChildNodes()) {
                    option = optionValues.get(id);
                }
                else {
                    option = OptionValue.getFromDomNode(id, childNode);
                    if (optionValues != null) {
                        optionValues.put(id, option);
                    }
                }
                options.add(option);
            }
            else {
                throw new ParseException("Unknown tag found:" + childNodeName);
            }
        }
        
        return options;
    }

    public static Options fromDomNode(Node node) throws ParseException {
        return fromDomNode(node, null);
    }

}
