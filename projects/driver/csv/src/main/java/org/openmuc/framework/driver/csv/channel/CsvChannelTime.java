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
package org.openmuc.framework.driver.csv.channel;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.csv.exceptions.CsvException;
import org.openmuc.framework.driver.csv.exceptions.NoValueReceivedYetException;
import org.openmuc.framework.driver.csv.exceptions.TimeTravelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CsvChannelTime extends CsvChannel {

    private static final Logger logger = LoggerFactory.getLogger(CsvChannelTime.class);

    protected boolean isInitialised = false;

    protected List<Long> timestamps;
    protected long firstTimestamp;
    protected long lastTimestamp;

    public CsvChannelTime(String column, Map<String, List<String>> csv, boolean rewind) 
            throws ArgumentSyntaxException {
        super(column, csv, rewind);
        Supplier<ArgumentSyntaxException> e = () -> new ArgumentSyntaxException("Index column invalid");
        
        timestamps = parseIndex(csv);
        firstTimestamp = timestamps.stream().mapToLong(i -> i).min().orElseThrow(e);
        lastTimestamp = timestamps.stream().mapToLong(i -> i).max().orElseThrow(e);
        
        logger.info("Read index for column \"{}\" of {} values from {} to {}", 
        		column, timestamps.size(), firstTimestamp, lastTimestamp);
    }

    protected CsvChannelTime(String column, List<Long> index, Map<String, List<String>> csv, boolean rewind) 
            throws ArgumentSyntaxException {
        super(column, csv, rewind);
        Supplier<ArgumentSyntaxException> e = () -> new ArgumentSyntaxException("Index column invalid");
        
        timestamps = index;
        firstTimestamp = index.stream().mapToLong(i -> i).min().orElseThrow(e);
        lastTimestamp = index.stream().mapToLong(i -> i).max().orElseThrow(e);
    }

    protected abstract List<Long> parseIndex(Map<String, List<String>> csv) throws ArgumentSyntaxException;

    protected int searchNextIndex(long samplingTime) throws CsvException {
        int index;

        if (isWithinTimeperiod(samplingTime)) {
            index = handleWithinTimeperiod(samplingTime);
        }
        else { // is outside time period
            index = handleOutsideTimeperiod(samplingTime);
        }

        if (!isInitialised) {
            isInitialised = true;
        }
        return index;
    }

    private int handleWithinTimeperiod(long samplingTime) throws CsvException {
        if (isBehindLastReadIndex(samplingTime)) {
            return getIndexByRegularSearch(samplingTime);
        }
        else if (isBeforeLastReadIndex(samplingTime)) {
            return handleBeforeLastReadIndex(samplingTime);
        }
        else { // is same timestamp
            return lastIndexRead;
        }
    }

    private int handleBeforeLastReadIndex(long samplingTime) throws CsvException {
    	long lastTimeRead = timestamps.get(lastIndexRead);
        if (lastIndexRead < timestamps.lastIndexOf(lastTimeRead)) {
            logger.info(
            		"Current sampling time is behind maximum available timestamp of csv file. " +
            		"Skipping forward to next day.");
        	advanceIndex();
            return getIndexByRegularSearch(samplingTime);
        }
        else if (rewind) {
            rewindIndex();
            return getIndexByRegularSearch(samplingTime);
        }
        else { // rewind disabled
            throw new TimeTravelException(
                    "Current sampling time is before the last sampling time. " +
                    "Since rewind is disabled, driver can't get value for current sampling time.");
        }
    }

    private int handleOutsideTimeperiod(long samplingTime) throws CsvException {
        if (isBeforeFirstTimestamp(samplingTime)) {
            return handleOutsideTimeperiodEarly(samplingTime);
        }
        else { // is after last timestamp
            logger.warn(
            		"Current sampling time is behind last available timestamp of csv file." +
            		"Returning value corresponding to last timestamp of day.");
            
        	long lastTimeRead = timestamps.get(lastIndexRead);
            if (lastIndexRead < timestamps.lastIndexOf(lastTimeRead)) {
            	advanceIndex();
            	return lastIndexRead;
            }
            return maxIndex;
        }
    }

    /**
     * Search in chronological order beginning from last read index. This is the regular case since the samplingTime
     * will normally increase with each read called*
     */
    private int getIndexByRegularSearch(long samplingTime) {
        logger.trace("Searching next index for time: {}", samplingTime);
    	int lastIndex = lastIndexRead;
        int nextIndex;
        do {
            nextIndex = lastIndex + 1;
            if (nextIndex > maxIndex) {
                return maxIndex;
            }
            lastIndex = nextIndex;
            
        } while (isBehindIndex(nextIndex, samplingTime));

        if (samplingTime == timestamps.get(nextIndex)) {
            return nextIndex;
        }
        else {
            return nextIndex - 1;
        }

    }

    private void advanceIndex() {
    	long lastTime;
        int nextIndex;
        do {
        	lastTime = timestamps.get(lastIndexRead);
            nextIndex = lastIndexRead + 1;
            if (lastTime < timestamps.get(nextIndex)) {
                return;
            }
            lastIndexRead = nextIndex;
        } while (lastIndexRead < maxIndex);
    }

    private void rewindIndex() {
        lastIndexRead = 0;
    }

    private boolean isBeforeLastReadIndex(long samplingTime) {
    	long lastTimeRead = timestamps.get(lastIndexRead);
        if (samplingTime < lastTimeRead) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isBehindLastReadIndex(long samplingTime) {
    	return this.isBehindIndex(lastIndexRead, samplingTime);
    }

    private boolean isBehindIndex(int index, long samplingTime) {
    	long time = timestamps.get(index);
        if (samplingTime > time) {
            return true;
        }
        return false;
    }

    private int handleOutsideTimeperiodEarly(long samplingTime) throws CsvException {
        if (isInitialised) {
            throw new TimeTravelException(
                    "Illogical time jump for sampling time. Driver can't find corresponding value in csv file.");
        }
        else {
            throw new NoValueReceivedYetException("Sampling time before first timestamp of csv file.");
        }
    }

    private boolean isWithinTimeperiod(long samplingTime) {
        if (samplingTime >= firstTimestamp && samplingTime <= lastTimestamp) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isBeforeFirstTimestamp(long samplingTime) {
        if (samplingTime < firstTimestamp) {
            return true;
        }
        else {
            return false;
        }
    }

}
