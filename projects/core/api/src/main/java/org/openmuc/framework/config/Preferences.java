package org.openmuc.framework.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Map;

import org.openmuc.framework.data.Value;


public abstract class Preferences {

    public abstract PreferenceType getPreferenceType();

    public int parseFields(Map<String, Value> settings) throws ArgumentSyntaxException {
        Class<? extends Preferences> settingsClass = this.getClass();

        int setFieldCounter = 0;
        for (Field field : settingsClass.getDeclaredFields()) {

            Option option = field.getAnnotation(Option.class);
            if (option == null) {
                continue;
            }
            String id = option.value();
            if (id.isEmpty() || id.equals(Option.FIELD_NAME)) {
            	id = field.getName();
            }

            Value value = settings.get(id);
            if (value != null) {
                try {
                    setField(field, value, option);
                    ++setFieldCounter;
                    
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        return setFieldCounter;
    }

    private void setField(Field field, Value value, Option option)
            throws IllegalAccessException, NoSuchFieldException, ArgumentSyntaxException {

        Object val = extractValue(field, value);
        field.set(this, val);
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
            throw argumentSyntaxException(value, InetAddress.class.getSimpleName());
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
            throw argumentSyntaxException(value, type.getSimpleName());
		}
    }

    private ArgumentSyntaxException argumentSyntaxException(Value value, String type) {
        return new ArgumentSyntaxException(MessageFormat.format("Value of {0} in {1} is not type of {2}.", 
        		value.asString(), this.getClass().getSimpleName(), type));
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Option {

    	public final static String FIELD_NAME = "FIELD_NAME";

        String value() default FIELD_NAME;
    }

}
