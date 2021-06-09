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
package org.openmuc.framework.driver.aggregator.types;

import java.util.Collection;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.driver.aggregator.AggregationException;
import org.openmuc.framework.driver.aggregator.AggregatorChannel;
import org.openmuc.framework.driver.aggregator.AggregatorConstants;
import org.openmuc.framework.driver.aggregator.ChannelAddress;
import org.openmuc.framework.driver.aggregator.ChannelRecordDeque;
import org.openmuc.framework.driver.aggregator.ChannelRecordDeque.ChannelRecordDeques;

public class AverageAggregation extends AggregatorChannel {

    private static final int INDEX_MOVING_AVG_WINDOW = 1;

    private ChannelRecordDeque recordDeque;

    public AverageAggregation(ChannelAddress simpleAddress, DataAccessService dataAccessService, ChannelRecordDeques records)
            throws AggregationException {
        super(simpleAddress, dataAccessService);
        
        String[] typeParams = simpleAddress.getAggregationType().split(AggregatorConstants.TYPE_PARAM_SEPARATOR);
        if (typeParams.length > 1) {
            final int movingAverageWindow = timeStringToMillis(typeParams[INDEX_MOVING_AVG_WINDOW]);
            
            this.recordDeque = records.get(sourceChannel);
            if (recordDeque != null) {
                recordDeque.setTimeWindow(movingAverageWindow);
            }
            else {
                recordDeque = records.add(sourceChannel, movingAverageWindow);
            }
        }
    }

    @Override
    protected void checkIntervals() throws AggregationException {
        String[] typeParams = channelAddress.getAggregationType().split(AggregatorConstants.TYPE_PARAM_SEPARATOR);
        if (typeParams.length < 1) {
        	super.checkIntervals();
        }
        else if (typeParams.length > 2) {
            throw new AggregationException("Wrong parameters for AVG.");
        }
    }

    @Override
    public double aggregate(long currentTimestamp, long endTimestamp) throws AggregationException {
        try {
            if (recordDeque != null) {
                if (recordDeque.size() <= channelAddress.getQuality()) {
                    throw new AggregationException("Unable to aggreate average for too few values: " + recordDeque.size());
                }
                return calcAvgOf(recordDeque);
            }
            return calcAvgOf(getLoggedRecords(currentTimestamp, endTimestamp));

        } catch (AggregationException e) {
            throw e;
        } catch (Exception e) {
            throw new AggregationException(e.getMessage());
        }

    }

    /**
     * Calculates the average of the all records
     */
    private static double calcAvgOf(Collection<Record> records) throws AggregationException {
        double sum = calcSumOf(records);
        
        return sum / records.size();
    }

    private static double calcSumOf(Collection<Record> records) {
        double sum = 0;

        for (Record record : records) {
            sum += record.getValue().asDouble();
        }
        return sum;
    }

}
