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
package org.openmuc.framework.driver.csv.channel;

import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.csv.exceptions.CsvException;

public class CsvChannelUnixtimestamp extends CsvChannelTime {

    public static final String INDEX = "unixtimestamp";

    public CsvChannelUnixtimestamp(String column, Map<String, List<String>> csv, boolean rewind) 
            throws ArgumentSyntaxException {
        super(column, csv, rewind);
    }

    public CsvChannelUnixtimestamp(String column, long[] index, Map<String, List<String>> csv, boolean rewind) 
            throws ArgumentSyntaxException {
        super(column, index, csv, rewind);
    }

    @Override
    protected long[] parseIndex(Map<String, List<String>> csv) throws ArgumentSyntaxException {
        List<String> timestampsList = csv.get(INDEX);
        
        long[] timestamps = new long[timestampsList.size()];
        for (int i = 0; i < timestampsList.size(); i++) {
            timestamps[i] = Long.parseLong(timestampsList.get(i));
        }
        return timestamps;
    }

    @Override
    public String readValue(long samplingTime) throws CsvException {
        lastIndexRead = searchNextIndex(samplingTime);
        return data.get(lastIndexRead);
    }

}
