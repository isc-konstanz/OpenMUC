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
package org.openmuc.framework.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openmuc.framework.config.option.OptionSyntax;
import org.openmuc.framework.config.option.OptionValue;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.config.option.annotation.OptionType;
import org.openmuc.framework.data.Value;

public abstract class Configurations {

    private final Map<String, Value> configurations = new HashMap<String, Value>();

    private final OptionSyntax syntax;

    public static Configurations parse(OptionType type, String configuration, Class<? extends Configurable> configurable) 
    		throws ArgumentSyntaxException {
    	switch (type) {
		case ADDRESS:
			return parseAddress(configuration, configurable);
		case SETTING:
		default:
			return parseSettings(configuration, configurable);
    	}
    }

    public static Address parseAddress(String configuration, Class<? extends Configurable> configurable) throws ArgumentSyntaxException {
        return new Address(configuration, configurable);
    }

    public static Settings parseSettings(String configuration, Class<? extends Configurable> configurable) throws ArgumentSyntaxException {
        return new Settings(configuration, configurable);
    }

    protected Configurations(OptionSyntax syntax) {
    	super();
    	this.syntax = syntax;
    }

    protected void parse(String parameterStr, Options options) throws ArgumentSyntaxException {
        if (parameterStr != null && !parameterStr.trim().isEmpty()) {
            String[] parameterArr = parameterStr.trim().split(options.getSyntax().getSeparator());
            
            if (parameterArr.length >= options.getMandatoryCount()) {
                if (options.getSyntax().hasKeyValuePairs()) {
                    for (OptionValue option : options) {
                        String key = option.getId();
                        Value value = null;
                        
                        boolean mandatoryOptMissing = option.isMandatory() ? true : false;
                        
                        parameterLoop:
                        for (String parameter : parameterArr) {
                            String[] keyValue = parameter.trim().split(options.getSyntax().getAssignment(), 2);
                            if (keyValue.length != 2) {
                                throw new ArgumentSyntaxException("Parameter is not a key value pair of type " 
                                        + "<key>" + options.getSyntax().getAssignment() + "<value> in parsed options: " + parameter);
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
                    for (OptionValue option : options) {
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

    public Value get(String key) {
        return configurations.get(key);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asString();
    }

    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    public Double getDouble(String key, Double defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asDouble();
    }

    public Float getFloat(String key) {
        return getFloat(key, null);
    }

    public Float getFloat(String key, Float defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asFloat();
    }

    public Long getLong(String key) {
        return getLong(key, null);
    }

    public Long getLong(String key, Long defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asLong();
    }

    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    public Integer getInteger(String key, Integer defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asInt();
    }

    public Short getShort(String key) {
        return getShort(key, null);
    }

    public Short getShort(String key, Short defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asShort();
    }

    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asBoolean();
    }

    public Byte getByte(String key) {
        return getByte(key, null);
    }

    public Byte getByte(String key, Byte defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asByte();
    }

    public byte[] getByteArray(String key) {
        return getByteArray(key, null);
    }

    public byte[] getByteArray(String key, byte[] defaultValue) {
    	if (!contains(key)) {
    		return defaultValue;
    	}
        return configurations.get(key).asByteArray();
    }

    public boolean contains(String key) {
        return configurations.containsKey(key);
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
                sb.append(syntax.getSeparator());
            }
            if (syntax.hasKeyValuePairs()) {
                sb.append(entry.getKey());
                sb.append(syntax.getAssignment());
            }
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

}
