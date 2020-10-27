/*
 * Copyright 2011-2020 Fraunhofer ISE
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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.csv.CsvDriver;
import org.openmuc.framework.driver.csv.CsvFile;
import org.openmuc.framework.driver.csv.channel.CsvChannelHHMMSS;
import org.openmuc.framework.driver.csv.channel.CsvChannelUnixtimestamp;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ConnectionException;

public class CsvTestFactory {

	private static CsvDriver driver = new CsvDriver();

    public static CsvFile newConnection(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
        return (CsvFile) driver.connect(address, settings);
    }

    public static CsvChannelUnixtimestamp newChannelUnixtimestamp(List<String> data, long[] timestamps, boolean rewind) {
        try {
			return new CsvChannelUnixtimestamp(newRecodContainer("test"), timestamps, Collections.singletonMap("test", data), rewind);
		
        } catch (ArgumentSyntaxException e) {
        	return null;
        }
    }

    public static CsvChannelHHMMSS newChannelHHMMSS(List<String> data, long[] timestamps, boolean rewind) {
        try {
        	return new CsvChannelHHMMSS(newRecodContainer("test"), timestamps, Collections.singletonMap("test", data), rewind);
		
        } catch (ArgumentSyntaxException e) {
        	return null;
        }
    }

    public static ChannelRecordContainer newRecodContainer(String address) {
    	return (ChannelRecordContainer) new CsvTestChannel(address).getReadContainer();
    }

}
