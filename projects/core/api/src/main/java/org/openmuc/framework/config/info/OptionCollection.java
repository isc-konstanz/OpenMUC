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
package org.openmuc.framework.config.info;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.openmuc.framework.config.ArgumentSyntaxException;
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

public class OptionCollection {
    
    private final List<Option> options;

    private String delimiter = ",";
    private String keyValueSeparator = ":";
    private Boolean keyValue = true;
    private Locale locale = Locale.ENGLISH;
    
    private int mandatoryOptCount = 0;

    public OptionCollection() {
        this.options = new LinkedList<Option>();
    }

    private OptionCollection(OptionCollection optionCollection) {
        this.options = (List<Option>) Collections.unmodifiableList(optionCollection.options);
    }

    public boolean add(Option option) {
        if (option.isMandatory()) mandatoryOptCount++;
        return options.add(option);
    }

    public boolean add(String key, String name, ValueType type, 
            boolean mandatory, String description, 
            Value defaultValue, OptionSelection valueSelection) {
        return add(new Option(key, name, type, mandatory, description, defaultValue, valueSelection));
    }

    public boolean add(String key, String name, ValueType type) {
        return add(new Option(key, name, type));
    }

    public boolean add(String key, ValueType type) {
        return add(key, key, type);
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setKeyValueSeperator(String separator) {
        this.keyValueSeparator = separator;
    }

    public void enableKeyValuePairs(boolean enable) {
        this.keyValue = enable;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Settings parse(String settingsStr) throws ArgumentSyntaxException {
        if (settingsStr != null) {
            String[] settingsArray = settingsStr.trim().split(delimiter);
            
            Settings settings = new Settings();
            if (settingsArray.length >= 1 && settingsArray.length >= mandatoryOptCount && settingsArray.length <= options.size()) {
                if (keyValue) {
                    for (Option option : options) {
                        boolean mandatoryOptMissing = option.isMandatory() ? true : false;
                        
                        String key = option.getKey();
                        Value value = null;
                        for (String setting : settingsArray) {
                            String[] keyValue = setting.trim().split(keyValueSeparator);
                            if (keyValue.length == 2) {
                                if (keyValue[0].trim().equalsIgnoreCase(key)) {
                                    mandatoryOptMissing = false;
                                    
                                    value = parseValue(option.getType(), keyValue[1]);
                                }
                            }
                            else {
                                throw new ArgumentSyntaxException("Parameter is not a key value pair of type "
                                            + "<key>" + keyValueSeparator + "<value> in parsed Settings: " + setting);
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
                    for (Option option : options) {
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
                throw new ArgumentSyntaxException("Mandatory parameters not configured in empty settings string");
            }
            else if (settingsArray.length > options.size()) {
                throw new ArgumentSyntaxException("Too many parameters in passed Settings to be parsed.");
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

    public String syntax() {

        StringBuilder sb = new StringBuilder();

        if (options.size() > 0) {
            sb.append("Synopsis: ");
            boolean first = true;
            for (Option option : options) {
                boolean mandatory = option.isMandatory();
                String key = option.getKey();
                
                String syntax;
                if (keyValue) {
                    syntax = key + keyValueSeparator + '<' + key.toLowerCase(locale) + '>';
                }
                else {
                    syntax = '<' + key + '>';
                }

                if (!mandatory) sb.append('[');
                if (!first) {
                    sb.append(delimiter);
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

    @Override
    public String toString() {
        return options.toString();
    }

    public static OptionCollection unmodifiableOptions(OptionCollection options) {
        
        OptionCollection unmodifiableOptions = new OptionCollection(options);
        unmodifiableOptions.mandatoryOptCount = options.mandatoryOptCount;
        unmodifiableOptions.delimiter = options.delimiter;
        unmodifiableOptions.keyValueSeparator = options.keyValueSeparator;
        unmodifiableOptions.keyValue = options.keyValue;
        unmodifiableOptions.locale = options.locale;
        
        return unmodifiableOptions;
    }

}
