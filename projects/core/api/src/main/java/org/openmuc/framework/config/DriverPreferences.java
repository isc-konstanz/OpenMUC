/*
 * Copyright 2011-18 Fraunhofer ISE
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

import java.util.HashMap;
import java.util.Map;

public class DriverPreferences extends DriverInfo {

    final Map<String, ParsedPreferences<?>> prefs = new HashMap<>();

    DriverPreferences(Class<?> driver) {
        super(driver);
    }

    @SuppressWarnings("unchecked")
	public <P extends Preferences> P get(String str, Class<P> type) throws ArgumentSyntaxException {
    	String key = type.getName();
        if (!prefs.containsKey(key)) {
        	prefs.put(key, new ParsedPreferences<P>(this));
        }
        return ((ParsedPreferences<P>) prefs.get(key)).get(str, type);
    }


    private class ParsedPreferences<P extends Preferences> extends HashMap<String, P> {
        private static final long serialVersionUID = 1425429683932361149L;

        private final DriverPreferences prefs;

        public ParsedPreferences(DriverPreferences prefs) {
            super();
            
            this.prefs = prefs;
        }

		public P get(String str, Class<P> type) throws ArgumentSyntaxException {
            P parsed;
            if (containsKey(str)) {
                parsed = get(str);
            }
            else {
                parsed = prefs.parse(str, type);
                put(str, parsed);
            }
            return parsed;
        }
    }

}
