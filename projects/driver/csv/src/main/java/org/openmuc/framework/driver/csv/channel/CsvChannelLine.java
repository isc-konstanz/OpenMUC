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
package org.openmuc.framework.driver.csv.channel;

import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;

/**
 * Channel to return value of next line in the file. Timestamps are ignored. It always starts with the first line, which
 * can be useful for simulation since every time the framework is started it starts with the same values.
 */
public class CsvChannelLine extends CsvChannel {

    public CsvChannelLine(String column, Map<String, List<String>> data, boolean rewind) 
            throws ArgumentSyntaxException {
        super(column, data, rewind);
        this.lastIndexRead = -1;
    }

    @Override
    public double readValue(long sampleTime) {

        lastIndexRead++;
        if (lastIndexRead > maxIndex) {
            if (rewind) {
                lastIndexRead = 0;
            }
            else {
                // once maximum is reached it always returns the maximum (value of last line in file)
                lastIndexRead = maxIndex;
            }
        }

        double value = Double.parseDouble(data.get(lastIndexRead));
        return value;
    }

}
