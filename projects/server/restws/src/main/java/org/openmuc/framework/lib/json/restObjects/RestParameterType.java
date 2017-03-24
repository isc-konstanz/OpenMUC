/*
 * Copyright 2011-16 Fraunhofer ISE 
 * Copyright 2016 ISC Konstanz 
 *
 * This file is part of EmonMUC, a project based heavily on OpenMUC by 
 * Fraunhofer ISE (http://www.openmuc.org). 
 * For more information visit https://bitbucket.org/isc-konstanz/emonmuc.
 *
 * EmonMUC is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * EmonMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmonMUC. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.lib.json.restObjects;

import com.google.gson.annotations.SerializedName;


public enum RestParameterType {
	
	@SerializedName("text")
	TEXT,
	
	@SerializedName("time")
	TIME,
	
	@SerializedName("value")
	VALUE,
	
	@SerializedName("boolean")
	BOOLEAN;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}

	public static RestParameterType getEnum(String type) {
		switch (type.toLowerCase()) {
		case "text":
			return TEXT;
		case "time":
			return TIME;
		case "value":
			return VALUE;
		case "boolean":
			return BOOLEAN;
		default:
			throw new IllegalArgumentException("Unknown parameter type: " + type);
		}
	}
}