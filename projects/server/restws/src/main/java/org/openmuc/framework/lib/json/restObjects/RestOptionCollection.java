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

import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.data.ValueType;

public class RestOptionCollection {
    
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
    
    public static RestOptionCollection setOptionCollection(OptionCollection options) {

        RestOptionCollection restOptions = new RestOptionCollection();
        restOptions.setOptions(RestOption.setOptions(options.getOptions()));
        
        RestOptionSyntax restSyntax = restOptions.new RestOptionSyntax();
        restSyntax.setDelimiter(options.getDelimiter());
        restSyntax.setKeyValueSeparator(options.getKeyValueSeperator());
        restSyntax.setKeyValue(options.hasKeyValuePairs());
        restOptions.setSyntax(restSyntax);
        
        return restOptions;
    }
    
    public static RestOptionCollection setOptionCollection(String syntax) {

        RestOptionCollection restOptions = new RestOptionCollection();
        
        List<Option> options = new ArrayList<Option>();
        Option option = new Option("settings", "Settings", ValueType.STRING);
        option.setDescription(syntax);
        restOptions.setOptions(RestOption.setOptions(options));
        
        RestOptionSyntax restSyntax = restOptions.new RestOptionSyntax();
        restSyntax.setDelimiter(";");
        restSyntax.setKeyValueSeparator(null);
        restSyntax.setKeyValue(false);
        restOptions.setSyntax(restSyntax);
        
        return restOptions;
    }

    class RestOptionSyntax {

        String delimiter = null;
        String keyValueSeparator = null;
        Boolean keyValue = null;

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        public void setKeyValueSeparator(String keyValueSeparator) {
            this.keyValueSeparator = keyValueSeparator;
        }

        public void setKeyValue(Boolean keyValue) {
            this.keyValue = keyValue;
        }
    }
}
