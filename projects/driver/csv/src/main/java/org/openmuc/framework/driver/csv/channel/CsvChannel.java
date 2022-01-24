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
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.DriverChannel;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.csv.exceptions.CsvException;
import org.openmuc.framework.driver.csv.exceptions.NoValueReceivedYetException;
import org.openmuc.framework.driver.csv.exceptions.TimeTravelException;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;

public abstract class CsvChannel extends DriverChannel {

    private static final Logger logger = LoggerFactory.getLogger(CsvChannel.class);

    public static final String COLUMN = "column";

    @Option(id = COLUMN,
            type = ADDRESS,
            name = "Column header",
            description = "The title of the header, defining the column."
    )
    private String column;

    protected final List<String> data;
    protected final boolean rewind;

    protected final int maxIndex;

    /** remember index of last valid sampled value */
    protected int lastIndexRead = 0;

    public CsvChannel(String column, Map<String, List<String>> csv, boolean rewind) throws ArgumentSyntaxException {
        if (!csv.containsKey(column)) {
            throw new ArgumentSyntaxException("Unknown column header specified: " + column);
        }
        this.data = csv.get(column);
        this.maxIndex = data.size() - 1;
        this.rewind = rewind;
    }

    public String getColumnHeader() {
        return column;
    }

    @Read
    public Record read(long samplingTime) throws ConnectionException {
        try {
            String valueAsString = readValue(samplingTime);

            if (getValueType().equals(ValueType.STRING)) {
                return new Record(new StringValue(valueAsString), samplingTime, Flag.VALID);
            }
            else {
                // In all other cases try parsing as double
                double value = Double.parseDouble(valueAsString);
                return new Record(new DoubleValue(value), samplingTime, Flag.VALID);
            }
        } catch (NoValueReceivedYetException e) {
            logger.warn("NoValueReceivedYetException: {}", e.getMessage());
            return new Record(new DoubleValue(Double.NaN), samplingTime, Flag.NO_VALUE_RECEIVED_YET);

        } catch (TimeTravelException e) {
            logger.warn("TimeTravelException: {}", e.getMessage());
            return new Record(new DoubleValue(Double.NaN), samplingTime, Flag.DRIVER_ERROR_READ_FAILURE);

        } catch (CsvException e) {
            logger.error("CsvException: {}", e.getMessage());
            return new Record(new DoubleValue(Double.NaN), samplingTime, Flag.DRIVER_THREW_UNKNOWN_EXCEPTION);
        }
    }

    protected abstract String readValue(long sampleTime) throws CsvException;

}
