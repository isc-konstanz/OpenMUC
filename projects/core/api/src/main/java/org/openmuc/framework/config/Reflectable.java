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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Reflectable extends Configurable {

    private static final Logger logger = LoggerFactory.getLogger(Reflectable.class);

    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP =
            Map.of(boolean.class, Boolean.class,
                   byte.class,    Byte.class,
                   char.class,    Character.class,
                   double.class,  Double.class,
                   float.class,   Float.class,
                   int.class,     Integer.class,
                   long.class,    Long.class,
                   short.class,   Short.class);

    public static <A extends Annotation> List<Method> getMethods(Class<A> annot, Class<?> type, Object[] args)
            throws RuntimeException {

        List<Method> methods = new ArrayList<Method>();
        while(type.getSuperclass() != null) {
            methods.addAll(Arrays.asList(type.getDeclaredMethods()));
            type = type.getSuperclass();
        }
        methods.removeIf(m -> !m.isAnnotationPresent(annot) || !hasParameterTypes(m, args));

        return methods;
    }

    protected <A extends Annotation> boolean hasMethod(Class<A> annot, Class<?> type, Object... args)
            throws RuntimeException {

        return getMethods(annot, type, args).size() > 0;
    }

    protected <A extends Annotation> boolean hasMethod(Class<A> annot, Object obj, Object... args)
            throws RuntimeException {

        return hasMethod(annot, obj.getClass(), args);
    }

    protected <A extends Annotation> void invokeMethod(Class<A> annot, Object obj, Object... args) {
        List<Method> methods = getMethods(annot, obj.getClass(), args);
        if (methods.size() < 1) {
            logger.trace("Skipping invocation of nonexisting {} method for arguments: {}", annot.getSimpleName(),
                    List.of(args).stream().map(a -> a.getClass()).toArray(Class<?>[]::new));
            return;
        }
        for (Method method : methods) {
            try {
                method.invoke(obj, args);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
                
            } catch (InvocationTargetException e) {
                throwException(e.getCause());
            }
        }
    }

    protected <A extends Annotation> Object invokeReturn(Class<A> annot, Object obj, Object... args) {
        List<Method> methods = getMethods(annot, obj.getClass(), args);
        for (Method method : methods) {
            try {
                return method.invoke(obj, args);
                
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
                
            } catch (InvocationTargetException e) {
                throwException(e.getCause());
            }
        }
        throw new UnsupportedOperationException(MessageFormat.format("Unable to find {0} method \"{1}\" for arguments: {2}", 
                annot.getSimpleName(), obj.getClass().getSimpleName(),
                List.of(args).stream().map(a -> a.getClass()).toArray(Class<?>[]::new)));
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

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwException(Throwable exception) throws T {
        throw (T) exception;
    }

    public static boolean hasParameterTypes(Method method, Object... args) {
        if (method.getParameterCount() != args.length) {
            logger.trace("Removing method \"{}\" with nonmatching amount of arguments: {}", method.getName(), 
                    method.getParameterCount());
            
            return false;
        }
        else if (method.getParameterCount() > 0) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i=0; i<args.length; i++) {
                if (!isAssignableTo(args[i].getClass(), parameterTypes[i])) {
                    logger.trace("Removing method \"{}\" with nonmatching arguments: {} != {}", method.getName(), 
                            args[i].getClass().getSimpleName(), parameterTypes[i].getSimpleName());
                    
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAssignableTo(Class<?> targetClass, Class<?> parameterClass) {
        if (parameterClass.isAssignableFrom(targetClass)) {
            return true;
        }
        if (targetClass.isPrimitive()) {
            return isPrimitiveWrapperOf(parameterClass, targetClass);
        }
        if (parameterClass.isPrimitive()) {
            return isPrimitiveWrapperOf(targetClass, parameterClass);
        }
        return false;
    }

    private static boolean isPrimitiveWrapperOf(Class<?> targetClass, Class<?> primitiveClass) {
        if (!primitiveClass.isPrimitive()) {
            throw new IllegalArgumentException("Second argument has to be primitive type");
        }
        return PRIMITIVE_MAP.get(primitiveClass) == targetClass;
    }

}
