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
package org.openmuc.framework.lib.json.restObjects;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.data.ValueType;

public class RestOptionCollection {
	
	public final static String ADDRESS = "address";
	public final static String SETTINGS = "settings";
	public final static String SCAN_SETTINGS = "scanSettings";
    
    private List<RestOption> options = null;
    private RestOptionSyntax syntax = null;

    public List<RestOption> getOptions() {
        return options;
    }

    public void setOptions(List<RestOption> options) {
        this.options = options;
    }

    public RestOptionSyntax getSyntax() {
        return syntax;
    }

    public void setSyntax(RestOptionSyntax syntax) {
        this.syntax = syntax;
    }

    public static RestOptionCollection parseOptionCollection(OptionCollection options) {
        RestOptionCollection restOptions = null;
        
        if (!options.isEmpty() && !options.isDisabled()) {
            restOptions = new RestOptionCollection();
            restOptions.setOptions(RestOption.getOptions(options));
            
            RestOptionSyntax restSyntax = restOptions.new RestOptionSyntax();
            restSyntax.setSeparator(options.getSeparator());
            restSyntax.setAssignmentOperator(options.getAssignmentOperator());
            restSyntax.setKeyValue(options.hasKeyValuePairs());
            restOptions.setSyntax(restSyntax);
        }
        return restOptions;
    }

    public static RestOptionCollection parseOptionCollection(String id, String syntax) {
        RestOptionCollection restOptions = new RestOptionCollection();
        
        String name;
        switch(id) {
        case ADDRESS:
        	name = "Address";
        	break;
        case SETTINGS:
        	name = "Settings";
        	break;
        case SCAN_SETTINGS:
        	name = "Scan settings";
        	break;
        default:
        	name = id;
        	break;
        }
        
        List<Option> options = new ArrayList<Option>();
        Option option = new Option(id, name, ValueType.STRING);
        option.setDescription(syntax);
        option.setMandatory(false);
        options.add(option);
        restOptions.setOptions(RestOption.getOptions(options));
        
        RestOptionSyntax restSyntax = restOptions.new RestOptionSyntax();
        restSyntax.setSeparator(";");
        restSyntax.setAssignmentOperator(null);
        restSyntax.setKeyValue(false);
        restOptions.setSyntax(restSyntax);
        
        return restOptions;
    }

    class RestOptionSyntax {

        String separator = null;
        String assignment = null;
        Boolean keyValue = null;

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public void setAssignmentOperator(String assignment) {
            this.assignment = assignment;
        }

        public void setKeyValue(Boolean keyValue) {
            this.keyValue = keyValue;
        }
    }
}
