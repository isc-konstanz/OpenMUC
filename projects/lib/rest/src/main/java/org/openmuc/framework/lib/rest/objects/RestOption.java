/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.lib.rest.objects;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.option.OptionValue;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;

public class RestOption {

    private String id;
    private String name = null;
    private String description = null;
    private ValueType type = null;
    private Boolean mandatory = null;

    private String valueDefault = null;
    private Map<String, String> valueSelection = null;

    public String getId() {
        return id;
    }

    public void setId(String key) {
        this.id = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getValueDefault() {
        return valueDefault;
    }

    public void setValueDefault(String valueDefault) {
        this.valueDefault = valueDefault;
    }

    public Map<String, String> getValueSelection() {
        return valueSelection;
    }

    public void setValueSelection(Map<String, String> valueSelection) {
        this.valueSelection = valueSelection;
    }

    public static List<RestOption> getOptions(Options options) {

        List<RestOption> restOptions = new LinkedList<RestOption>();
        for (OptionValue option : options) {
            RestOption restOption = new RestOption();
            restOption.setId(option.getId());
            restOption.setName(option.getName());
            restOption.setDescription(option.getDescription());
            restOption.setType(option.getType());
            restOption.setMandatory(option.isMandatory());
            
            if (option.getValueDefault() != null) {
                restOption.setValueDefault(option.getValueDefault().asString());
            }
            if (option.getValueSelection() != null) {
                Map<String, String> restSelection = new LinkedHashMap<String, String>();
                for (Map.Entry<Value, String> selection : option.getValueSelection().entrySet()) {
                    restSelection.put(selection.getKey().asString(), selection.getValue());
                }
                restOption.setValueSelection(restSelection);
            }
            
            restOptions.add(restOption);
        }
        return restOptions;
    }

}