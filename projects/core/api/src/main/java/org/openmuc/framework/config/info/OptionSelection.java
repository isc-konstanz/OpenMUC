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

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

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

public class OptionSelection {

    private static String DELIMITER = ",";
    private static String KEY_VAL_SEP = ":";

    private final Map<String, Value> options;
    private final ValueType type;
    
    private boolean validate = true;

    public OptionSelection(ValueType type) {
        this.options = new LinkedHashMap<String, Value>();
        this.type = type;
    }

    public OptionSelection(ValueType type, boolean verify) {
        this(type);
        this.validate = verify;
    }

    public OptionSelection(ValueType type, String selectionStr) throws ArgumentSyntaxException {
        this(type);

        String[] selectionArray = selectionStr.trim().split(DELIMITER);
        for (String selection : selectionArray) {
            String[] keyValue = selection.trim().split(KEY_VAL_SEP);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                Value val;
                try {
                    switch (type) {
                    case BOOLEAN:
                        val = new BooleanValue(Boolean.valueOf(keyValue[1]));
                        break;
                    case BYTE:
                        val = new ByteValue(Byte.valueOf(keyValue[1]));
                        break;
                    case DOUBLE:
                        val = new DoubleValue(Double.valueOf(keyValue[1]));
                        break;
                    case FLOAT:
                        val = new FloatValue(Float.valueOf(keyValue[1]));
                        break;
                    case INTEGER:
                        val = new IntValue(Integer.valueOf(keyValue[1]));
                        break;
                    case LONG:
                        val = new LongValue(Long.valueOf(keyValue[1]));
                        break;
                    case SHORT:
                        val = new ShortValue(Short.valueOf(keyValue[1]));
                        break;
                    case STRING:
                        val = new StringValue(keyValue[1]);
                        break;
                    default:
                        throw new ArgumentSyntaxException("Selection value type not configured: " + type.name().toLowerCase());
                    }
                } catch (NumberFormatException e) {
                    throw new ArgumentSyntaxException(MessageFormat.format("Selection value \"{0}\" is not of type: {1}.", 
                            selection, type.name().toLowerCase()));
                }
                options.put(key, val);
            }
            else {
                throw new ArgumentSyntaxException("Selection is not a key value par of type "
                            + "<key>" + KEY_VAL_SEP + "<value> in parsed OptionSelection");
            }
        }
    }

    public boolean contains(Value value) {
        if (value != null) {
            for (Value option : options.values()) {
                switch (this.type) {
                case BOOLEAN:
                    if (option.asBoolean() == value.asBoolean()) {
                        return true;
                    }
                    break;
                case BYTE:
                    if (option.asByte() == value.asByte()) {
                        return true;
                    }
                    break;
                case DOUBLE:
                    if (option.asDouble() == value.asDouble()) {
                        return true;
                    }
                    break;
                case FLOAT:
                    if (option.asFloat() == value.asFloat()) {
                        return true;
                    }
                    break;
                case INTEGER:
                    if (option.asInt() == value.asInt()) {
                        return true;
                    }
                    break;
                case LONG:
                    if (option.asLong() == value.asLong()) {
                        return true;
                    }
                    break;
                case SHORT:
                    if (option.asShort() == value.asShort()) {
                        return true;
                    }
                    break;
                case STRING:
                    if (option.asString().equals(value.asString())) {
                        return true;
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return false;
    }

    public boolean hasValidation() {
        return validate;
    }

    public void enableValidation(boolean enable) {
        this.validate = enable;
    }

    public void addValue(String name, Value value) {
        this.options.put(name, value);
    }

    public void addBoolean(String name, boolean value) {
        this.addValue(name, new BooleanValue(value));
    }

    public void addByte(String name, byte value) {
        this.addValue(name, new ByteValue(value));
    }

    public void addDouble(String name, double value) {
        this.addValue(name, new DoubleValue(value));
    }

    public void addFloat(String name, float value) {
        this.addValue(name, new FloatValue(value));
    }

    public void addInteger(String name, int value) {
        this.addValue(name, new IntValue(value));
    }

    public void addLong(String name, long value) {
        this.addValue(name, new LongValue(value));
    }

    public void addShort(String name, short value) {
        this.addValue(name, new ShortValue(value));
    }

    public void addString(String name, String value) {
        this.addValue(name, new StringValue(value));
    }

    @Override
    public String toString() {
        return options.toString();
    }
    
    public static OptionSelection timeSelection() {
        
        OptionSelection selection = new OptionSelection(ValueType.INTEGER, false);
        selection.addInteger("None", 0);
        selection.addInteger("100 miliseconds", 100);
        selection.addInteger("200 miliseconds", 200);
        selection.addInteger("500 miliseconds", 500);
        selection.addInteger("1 second", 1000);
        selection.addInteger("2 second", 2000);
        selection.addInteger("5 seconds", 5000);
        selection.addInteger("10 seconds", 10000);
        selection.addInteger("15 seconds", 15000);
        selection.addInteger("30 seconds", 30000);
        selection.addInteger("45 seconds", 45000);
        selection.addInteger("1 minute", 60000);
        selection.addInteger("2 minutes", 120000);
        selection.addInteger("5 minutes", 300000);
        selection.addInteger("10 minutes", 600000);
        selection.addInteger("15 minutes", 900000);
        selection.addInteger("30 minutes", 1800000);
        selection.addInteger("45 minutes", 2700000);
        selection.addInteger("1 hour", 3600000);
        selection.addInteger("1 day", 86400000);
        
        return selection;
    }

}
