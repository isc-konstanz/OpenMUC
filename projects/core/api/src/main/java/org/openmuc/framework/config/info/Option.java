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

import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;

public class Option {

    protected final String key;
    protected final String name;
    protected final ValueType type;

    protected boolean mandatory = true;
    protected String description = null;

    protected Value defaultValue = null;
    protected OptionSelection valueSelection = null;

    public Option(String key, String name, ValueType type, 
            boolean mandatory, String description, 
            Value defaultValue, OptionSelection valueSelection) {
        
        this.key = key;
        this.name = name;
        this.type = type;
        this.mandatory = mandatory;
        this.description = description;
        this.defaultValue = defaultValue;
        this.valueSelection = valueSelection;
    }

    public Option(String key, String name, ValueType type) {
        
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public ValueType getType() {
        return this.type;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Value defaultValue) {
        this.defaultValue = defaultValue;
    }

    public OptionSelection getValueSelection() {
        return valueSelection;
    }

    public void setValueSelection(OptionSelection valueSelection) {
        this.valueSelection = valueSelection;
    }

}