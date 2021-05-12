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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.OptionType;
import org.openmuc.framework.data.Value;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

public abstract class Configurable {

    protected Configurable() {
    }

    public final void configure(OptionType type, String configs) throws ArgumentSyntaxException {
        this.configure(Configurations.parse(type, configs, getClass()));
    }

    public final void configure(Configurations configs) throws ArgumentSyntaxException {
        if (configs instanceof org.openmuc.framework.config.Address) {
            configure(this, (org.openmuc.framework.config.Address) configs);
        }
        if (configs instanceof org.openmuc.framework.config.Settings) {
            configure(this, (org.openmuc.framework.config.Settings) configs);
        }
    }

    public static void configure(Object obj, Address address) throws ArgumentSyntaxException {
        Configurable.configure(obj, address, ADDRESS);
    }

    public static void configure(Object obj, Settings settings) throws ArgumentSyntaxException {
        Configurable.configure(obj, settings, SETTING);
    }

    private static void configure(Object obj, Configurations configs, OptionType type) 
            throws ArgumentSyntaxException {
        
        List<Field> fields = getFields(obj);
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
                ids = new String[] { 
                        field.getName()
                    };
            }
            for (String id : ids) {
                if (configs.contains(id)) {
                    setField(obj, field, configs.get(id));
                    break;
                }
            }
        }
    }

    static List<Field> getFields(Object obj) {
        List<Field> fields = new LinkedList<Field>();
        
        Class<?> type = obj.getClass();
        while(type.getSuperclass() != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    static void setField(Object obj, Field field, Value value) throws ArgumentSyntaxException {
        if (value != null) {
            try {
                Object object = extractValue(obj, field, value);
                
                field.setAccessible(true);
                field.set(obj, object);
                
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new ArgumentSyntaxException(MessageFormat.format("Error parsing parameter of \"{0}\": {1}", 
                        obj.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    static Object extractValue(Object obj, Field field, Value value)
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

    static InetAddress extractInetAddress(Value value) throws ArgumentSyntaxException {
        try {
            return InetAddress.getByName(value.asString());
        } catch (UnknownHostException e) {
            throw new ArgumentSyntaxException(MessageFormat.format("Value {0} not of type {1}.", 
                    value.asString(), InetAddress.class.getSimpleName()));
        }
    }

    static Object extractValueOf(Class<?> type, Value value) throws ArgumentSyntaxException, NoSuchFieldException {
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
            throw new ArgumentSyntaxException(MessageFormat.format("Value {0} not of type {1}.", 
                    value.asString(), type.getSimpleName()));
        }
    }

}
