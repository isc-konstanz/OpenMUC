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

import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.data.Value;

public class Preferences {

    Map<String, Value> parameters = new HashMap<String, Value>();

    public Value getValue(String key) {
        return parameters.get(key);
    }

    public boolean getBoolean(String key) {
        return parameters.get(key).asBoolean();
    }

    public byte getByte(String key) {
        return parameters.get(key).asByte();
    }

    public double getDouble(String key) {
        return parameters.get(key).asDouble();
    }

    public float getFloat(String key) {
        return parameters.get(key).asFloat();
    }

    public int getInteger(String key) {
        return parameters.get(key).asInt();
    }

    public long getLong(String key) {
        return parameters.get(key).asLong();
    }

    public short getShort(String key) {
        return parameters.get(key).asShort();
    }

    public String getString(String key) {
        return parameters.get(key).asString();
    }
    
    public boolean contains(String key) {
        return parameters.containsKey(key);
    }

    @Override
    public String toString() {
        return parameters.toString();
    }

}
