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
package org.openmuc.framework.driver.revpi.dio;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.driver.DriverChannel;
import org.openmuc.framework.driver.annotation.Configure;

public class RevPiDigitalIoChannel extends DriverChannel {

    @Option(id = "address",
            type = ADDRESS,
            name = "Address",
            description = "The channel identifier of the digital IO."
    )
    private int address;

    @Option(type = SETTING,
            name = "Inverted state logic",
            description = "Use an inverted state logic.",
            valueDefault = "false",
            mandatory = false
    )
    private boolean inverted = false;

    public boolean isInverted() {
        return inverted;
    }

    public int getAddress() {
    	return address;
    }

    @Configure
    public void configure() throws ArgumentSyntaxException {
    	if (address < 1 || address > 14) {
    		throw new ArgumentSyntaxException("Invalid DIO channel: " + address);
    	}
    }

}
