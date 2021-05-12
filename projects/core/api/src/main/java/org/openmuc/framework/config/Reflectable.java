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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Reflectable extends Configurable {

    private static final Logger logger = LoggerFactory.getLogger(Reflectable.class);

    public static <A extends Annotation> List<Method> getMethods(Class<A> annot, Class<?> type) throws RuntimeException {
        List<Method> methods = new ArrayList<Method>();
        while(type.getSuperclass() != null) {
            methods.addAll(Arrays.asList(type.getDeclaredMethods()));
            type = type.getSuperclass();
        }
        methods.removeIf(m -> !m.isAnnotationPresent(annot));
        
        return methods;
    }

    protected <A extends Annotation> boolean hasMethod(Class<A> annot, Class<?> type) throws RuntimeException {
    	return getMethods(annot, type).size() > 0;
    }

    protected <A extends Annotation> boolean hasMethod(Class<A> annot, Object obj) throws RuntimeException {
    	return hasMethod(annot, obj.getClass());
    }

    protected <A extends Annotation> void invokeMethod(Class<A> annot, Object obj, Object... args) 
            throws RuntimeException {
        List<Method> methods = getMethods(annot, obj.getClass());
        if (methods.size() < 1) {
            logger.trace("Skipping invocation of nonexisting method with annotation: {}", annot.getSimpleName());
            return;
        }
        for (Method method : methods) {
            if (method.getParameterCount() != args.length) {
                logger.trace("Skipping invocation of method \"{}\" with nonmatching amount of arguments: {}", method.getName(), 
                        method.getParameterCount());
                
                continue;
            }
            try {
                method.invoke(obj, args);
                
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected <A extends Annotation> Object invokeReturn(Class<A> annot, Object obj, Object... args) 
            throws RuntimeException {
        List<Method> methods = getMethods(annot, obj.getClass());
        if (methods.size() > 1) {
            throw new RuntimeException(MessageFormat.format("More than one method present in {0} with annotation: {1}", 
                    obj.getClass().getSimpleName(), annot.getSimpleName()));
        }
        try {
            return methods.get(0).invoke(obj, args);
            
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static <C extends Configurable> C newInstance(Class<C> configurable) throws RuntimeException {
        try {
            return (C) configurable.getDeclaredConstructor().newInstance();
            
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(MessageFormat.format("Unable to instance {0}: {1}", 
                    configurable.getSimpleName(), e.getMessage()));
        }
    }

}
