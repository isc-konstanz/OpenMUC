/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.driver.csv.test.utils;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.csv.CsvFile;
import org.openmuc.framework.driver.csv.channel.CsvChannelHHMMSS;
import org.openmuc.framework.driver.csv.channel.CsvChannelUnixtimestamp;
import org.openmuc.framework.driver.spi.ConnectionException;


public class CsvTestFile extends CsvFile {

	public CsvTestFile(String addressStr, String settingsStr, Supplier<Long> currentMillisSupplier) 
    		throws ArgumentSyntaxException, ConnectionException {
		super(currentMillisSupplier);
		this.configure(addressStr, settingsStr);
        this.connect();
	}

	public CsvTestFile(String addressStr, String settingsStr) 
    		throws ArgumentSyntaxException, ConnectionException {
    	this(addressStr, settingsStr, () -> System.currentTimeMillis());
	}

    private void configure(String addressStr, String settingsStr) 
    		throws ArgumentSyntaxException, ConnectionException {
        Address address = Configurations.parseAddress(addressStr, CsvFile.class);
        configure(address);
        
        Settings settings = Configurations.parseSettings(settingsStr, CsvFile.class);
        configure(settings);
    }

    public static CsvChannelUnixtimestamp newChannelUnixtimestamp(List<String> data, long[] timestamps, boolean rewind) {
        try {
            return new CsvChannelUnixtimestamp("test", timestamps, Collections.singletonMap("test", data), rewind);
        
        } catch (ArgumentSyntaxException e) {
            return null;
        }
    }

    public static CsvChannelHHMMSS newChannelHHMMSS(List<String> data, long[] timestamps, boolean rewind) {
        try {
            return new CsvChannelHHMMSS("test", timestamps, Collections.singletonMap("test", data), rewind);
        
        } catch (ArgumentSyntaxException e) {
            return null;
        }
    }

}
