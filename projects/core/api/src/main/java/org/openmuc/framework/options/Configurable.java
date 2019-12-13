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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Value;

public abstract class Configurable {

    protected Configurable() {
    }

    protected Configurable(String address, String settings) throws ArgumentSyntaxException {
    	configure(address, settings);
    }

    public void configure(String address, String settings) throws ArgumentSyntaxException {
        configureAddress(address);
        configureSettings(settings);
    }

    protected void configureAddress(String addressStr) throws ArgumentSyntaxException {
        Map<String, Value> address = parse(addressStr, Options.parseAddress(this.getClass()));
        
        List<Field> fields = new LinkedList<Field>();
        Class<?> clazz = this.getClass();
        while(clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        for (Field field : fields) {
            Address option = field.getAnnotation(Address.class);
            if (option == null) {
                continue;
            }
            String id = option.value();
            if (id.isEmpty() || id.equals(Address.VALUE_DEFAULT)) {
            	String[] ids = option.id();
    			for (String i : ids) {
    				if (i.isEmpty() || i.equals(Address.OPTION_DEFAULT)) {
    					continue;
    				}
    				if (address.containsKey(i)) {
    					id = i;
    					break;
    				}
    			}
			}
            if (id.isEmpty() || id.equals(Address.OPTION_DEFAULT)) {
            	id = field.getName();
			}
            
            Value value = address.get(id);
            if (value != null) {
                try {
                    field.set(this, extractValue(field, value));
                    
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new ArgumentSyntaxException(MessageFormat.format("Error parsing address of \"{0}\": {1}", 
                            this.getClass().getSimpleName(), e.getMessage()));
                }
            }
        }
    }

    protected void configureSettings(String settingsStr) throws ArgumentSyntaxException {
        Map<String, Value> settings = parse(settingsStr, Options.parseSettings(this.getClass()));
        
        List<Field> fields = new LinkedList<Field>();
        Class<?> clazz = this.getClass();
        while(clazz.getSuperclass() != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        for (Field field : fields) {
            Setting option = field.getAnnotation(Setting.class);
            if (option == null) {
                continue;
            }
            String id = option.value();
            if (id.isEmpty() || id.equals(Setting.VALUE_DEFAULT)) {
            	String[] ids = option.id();
    			for (String i : ids) {
    				if (i.isEmpty() || i.equals(Setting.OPTION_DEFAULT)) {
    					continue;
    				}
    				if (settings.containsKey(i)) {
    					id = i;
    					break;
    				}
    			}
			}
            if (id.isEmpty() || id.equals(Setting.OPTION_DEFAULT)) {
            	id = field.getName();
			}
            
            Value value = settings.get(id);
            if (value != null) {
                try {
                    field.set(this, extractValue(field, value));
                    
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new ArgumentSyntaxException(MessageFormat.format("Error parsing settings of \"{0}\": {1}", 
                            this.getClass().getSimpleName(), e.getMessage()));
                }
            }
        }
    }

    private Map<String, Value> parse(String parameterStr, Options options) throws UnsupportedOperationException, ArgumentSyntaxException {
        Map<String, Value> result = new HashMap<String, Value>();
        if (parameterStr != null && !parameterStr.trim().isEmpty()) {
            String[] parameterArr = parameterStr.trim().split(options.getSeparator());
            
            if (parameterArr.length >= options.getMandatoryCount() && parameterArr.length <= options.size()) {
                if (options.hasKeyValuePairs()) {
                    for (Option option : options) {
                        boolean mandatoryOptMissing = option.isMandatory() ? true : false;
                        
                        String key = option.getId();
                        Value value = null;
                        for (String parameter : parameterArr) {
                            String[] keyValue = parameter.trim().split(options.getAssignmentOperator(), 2);
                            if (keyValue.length != 2) {
                                throw new ArgumentSyntaxException("Parameter is not a key value pair of type " 
                                        + "<key>" + options.getAssignmentOperator() + "<value> in parsed options: " + parameter);
                            }
                            if (keyValue[0].trim().equalsIgnoreCase(key)) {
                                if (keyValue[1].trim().isEmpty()) {
                                    throw new ArgumentSyntaxException("Parameter " + key + "is empty");
                                }
                                mandatoryOptMissing = false;
                                
                                value = Options.parseValue(option.getType(), keyValue[1].trim());
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
                        result.put(key, value);
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
                        result.put(option.getId(), value);
                    }
                }
            }
            else if (parameterArr.length < options.getMandatoryCount()) {
                throw new ArgumentSyntaxException("Mandatory parameters not configured in parameter string: " + parameterStr);
            }
            else if (parameterArr.length > options.size()) {
                throw new ArgumentSyntaxException("Too many parameters in passed string to be parsed: " + parameterStr);
            }
        }
        else if (options.getMandatoryCount() > 0) {
            throw new ArgumentSyntaxException("Mandatory parameters not configured in empty string");
        }
        return result;
    }

    private Object extractValue(Field field, Value value)
            throws ArgumentSyntaxException, IllegalAccessException, NoSuchFieldException {
        
        field.setAccessible(true);
        
        Class<?> type = field.getType();
        if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            return value.asBoolean();
        }
        else if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
            return value.asByte();
        }
        else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
            return value.asShort();
        }
        else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            return value.asInt();
        }
        else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            return value.asLong();
        }
        else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
            return value.asFloat();
        }
        else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
            return value.asDouble();
        }
        else if (type.isAssignableFrom(String.class)) {
            return value.asString();
        }
        else if (type.isAssignableFrom(byte[].class)) {
            return value.asByteArray();
        }
        else if (type.isAssignableFrom(InetAddress.class)) {
            return extractInetAddress(value);
        }
        else {
            return extractValueOf(type, value);
        }
    }

    private InetAddress extractInetAddress(Value value) throws ArgumentSyntaxException {
        try {
            return InetAddress.getByName(value.asString());
        } catch (UnknownHostException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Value of {0} in {1} is not type of {2}.", 
                    value.asString(), this.getClass().getSimpleName(), InetAddress.class.getSimpleName()));
        }
    }

    private Object extractValueOf(Class<?> type, Value value) throws ArgumentSyntaxException, NoSuchFieldException {
        Method method = null;
        try {
            method = type.getMethod("valueOf", String.class);
            
        } catch (NoSuchMethodException | SecurityException e) {
            // check if method is null and procees
        }
        if (method == null) {
            throw new NoSuchFieldException(
                    type + "  Driver implementation error not supported data type. Report driver developer\n");
        }
        
        try {
            if (type.isEnum()) {
                return method.invoke(null, value.asString().toUpperCase());
            }
            return method.invoke(null, value.asString());
            
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Value of {0} in {1} is not type of {2}.", 
                    value.asString(), this.getClass().getSimpleName(), type.getSimpleName()));
        }
    }

}