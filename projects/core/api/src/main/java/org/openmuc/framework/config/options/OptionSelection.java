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
package org.openmuc.framework.config.options;

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

    private final Map<Value, String> options;
    private final ValueType type;
    
    private boolean validate = true;

    public OptionSelection(ValueType type) {
        this.options = new LinkedHashMap<Value, String>();
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
                String desc = keyValue[1];
                Value val;
                try {
                    switch (type) {
                    case BOOLEAN:
                        val = new BooleanValue(Boolean.valueOf(keyValue[0]));
                        break;
                    case BYTE:
                        val = new ByteValue(Byte.valueOf(keyValue[0]));
                        break;
                    case DOUBLE:
                        val = new DoubleValue(Double.valueOf(keyValue[0]));
                        break;
                    case FLOAT:
                        val = new FloatValue(Float.valueOf(keyValue[0]));
                        break;
                    case INTEGER:
                        val = new IntValue(Integer.valueOf(keyValue[0]));
                        break;
                    case LONG:
                        val = new LongValue(Long.valueOf(keyValue[0]));
                        break;
                    case SHORT:
                        val = new ShortValue(Short.valueOf(keyValue[0]));
                        break;
                    case STRING:
                        val = new StringValue(keyValue[0]);
                        break;
                    default:
                        throw new ArgumentSyntaxException("Selection value type not configured: " + type.name().toLowerCase());
                    }
                } catch (NumberFormatException e) {
                    throw new ArgumentSyntaxException(MessageFormat.format("Selection value \"{0}\" is not of type: {1}.", 
                            selection, type.name().toLowerCase()));
                }
                options.put(val, desc);
            }
            else {
                throw new ArgumentSyntaxException("Selection is not a key value par of type "
                            + "<key>" + KEY_VAL_SEP + "<value> in parsed OptionSelection");
            }
        }
    }

    public boolean contains(Value value) {
        if (value != null) {
            for (Value option : options.keySet()) {
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

    public void addValue(Value value, String description) {
        this.options.put(value, description);
    }

    public void addBoolean(boolean value, String description) {
        this.addValue(new BooleanValue(value), description);
    }

    public void addByte(byte value, String description) {
        this.addValue(new ByteValue(value), description);
    }

    public void addDouble(double value, String description) {
        this.addValue(new DoubleValue(value), description);
    }

    public void addFloat(float value, String description) {
        this.addValue(new FloatValue(value), description);
    }

    public void addInteger(int value, String description) {
        this.addValue(new IntValue(value), description);
    }

    public void addLong(long value, String description) {
        this.addValue(new LongValue(value), description);
    }

    public void addShort(short value, String description) {
        this.addValue(new ShortValue(value), description);
    }

    public void addString(String value, String description) {
        this.addValue(new StringValue(value), description);
    }
    
    public Map<Value, String> getOptions() {
    	return options;
    }

    @Override
    public String toString() {
        return options.toString();
    }
    
    public static OptionSelection timeSelection() {
        
        OptionSelection selection = new OptionSelection(ValueType.INTEGER, false);
        selection.enableValidation(false);
        selection.addInteger(0, "None");
        selection.addInteger(100, "100 milliseconds");
        selection.addInteger(200, "200 milliseconds");
        selection.addInteger(500, "500 milliseconds");
        selection.addInteger(1000, "1 second");
        selection.addInteger(2000, "2 second");
        selection.addInteger(5000, "5 seconds");
        selection.addInteger(10000, "10 seconds");
        selection.addInteger(15000, "15 seconds");
        selection.addInteger(30000, "30 seconds");
        selection.addInteger(45000, "45 seconds");
        selection.addInteger(60000, "1 minute");
        selection.addInteger(120000, "2 minutes");
        selection.addInteger(300000, "5 minutes");
        selection.addInteger(600000, "10 minutes");
        selection.addInteger(900000, "15 minutes");
        selection.addInteger(1800000, "30 minutes");
        selection.addInteger(2700000, "45 minutes");
        selection.addInteger(3600000, "1 hour");
        selection.addInteger(86400000, "1 day");
        
        return selection;
    }

}
