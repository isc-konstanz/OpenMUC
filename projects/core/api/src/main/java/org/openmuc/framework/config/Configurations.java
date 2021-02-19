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
package org.openmuc.framework.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openmuc.framework.config.option.Option;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.data.Value;

public abstract class Configurations {

    private final Map<String, Value> configurations = new HashMap<String, Value>();

    private final String separator;
    private final String assignment;
    private final Boolean keyValue;

    public static Address parseAddress(String configuration, Class<? extends Configurable> configurable) throws ArgumentSyntaxException {
        return new Address(configuration, configurable);
    }

    public static Settings parseSettings(String configuration, Class<? extends Configurable> configurable) throws ArgumentSyntaxException {
        return new Settings(configuration, configurable);
    }

    protected Configurations(String separator, String assignment, boolean keyValue) throws ArgumentSyntaxException {
        super();
        
        this.separator = separator;
        this.assignment = assignment;
        this.keyValue = keyValue;
    }

    protected void parse(String parameterStr, Options options) throws ArgumentSyntaxException {
        if (parameterStr != null && !parameterStr.trim().isEmpty()) {
            String[] parameterArr = parameterStr.trim().split(options.getSeparator());
            
            if (parameterArr.length >= options.getMandatoryCount()) {
                if (options.hasKeyValuePairs()) {
                    for (Option option : options) {
                        String key = option.getId();
                        Value value = null;
                        
                        boolean mandatoryOptMissing = option.isMandatory() ? true : false;
                        
                        parameterLoop:
                        for (String parameter : parameterArr) {
                            String[] keyValue = parameter.trim().split(options.getAssignmentOperator(), 2);
                            if (keyValue.length != 2) {
                                throw new ArgumentSyntaxException("Parameter is not a key value pair of type " 
                                        + "<key>" + options.getAssignmentOperator() + "<value> in parsed options: " + parameter);
                            }
                            if (keyValue[1].trim().isEmpty()) {
                                throw new ArgumentSyntaxException("Parameter " + parameter + "is empty");
                            }
                            for (String id : option.getIds()) {
                                if (keyValue[0].trim().equalsIgnoreCase(id)) {
                                    mandatoryOptMissing = false;
                                    
                                    value = Options.parseValue(option.getType(), keyValue[1].trim());
                                    break parameterLoop;
                                }
                            }
                        }
                        if (mandatoryOptMissing) {
                            throw new ArgumentSyntaxException("Mandatory parameter " + key + " is not present in parsed Settings");
                        }
                        
                        if (value == null) {
                            value = option.getValueDefault();
                        }
                        if (value != null) {
                            if (option.getValueSelection() != null && 
                                    option.getValueSelection().hasValidation() &&
                                   !option.getValueSelection().contains(value)) {
                                throw new ArgumentSyntaxException("Parameter value not a valid selection: " + value.toString());
                            }
                            configurations.put(key, value);
                        }
                    }
                }
                else {
                    int optionalOptCount = 0;
                    
                    int i = 0;
                    for (Option option : options) {
                        Value value = null;
                        
                        if (i >= parameterArr.length) {
                            break;
                        }
                        else if (option.isMandatory() || options.getMandatoryCount()+optionalOptCount < parameterArr.length) {
                            value = Options.parseValue(option.getType(), parameterArr[i].trim());
                            
                            if (!option.isMandatory()) {
                                optionalOptCount++;
                            }
                            i++;
                        }
                        
                        if (value == null) {
                            value = option.getValueDefault();
                        }
                        if (value != null) {
                            if (option.getValueSelection() != null && 
                                    option.getValueSelection().hasValidation() &&
                                   !option.getValueSelection().contains(value)) {
                                throw new ArgumentSyntaxException("Parameter value not a valid selection: " + value.toString());
                            }
                            configurations.put(option.getId(), value);
                        }
                    }
                }
            }
            else {
                throw new ArgumentSyntaxException("Mandatory parameters not configured in parameter string: " + parameterStr);
            }
        }
        else if (options.getMandatoryCount() > 0) {
            throw new ArgumentSyntaxException("Mandatory parameters not configured in empty string");
        }
    }

    public boolean contains(String key) {
        return configurations.containsKey(key);
    }

    public Value get(String key) {
        return configurations.get(key);
    }

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        boolean first = true;
        for (Entry<String, Value> entry : configurations.entrySet()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(separator);
            }
            if (keyValue) {
                sb.append(entry.getKey());
                sb.append(assignment);
            }
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

}
