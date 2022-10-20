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
package org.openmuc.framework.driver.derivator;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;

public class DerivatorChannel {

    protected final Channel sourceChannel;

    protected int derivativeTime;

    protected Record lastRecord;

    public DerivatorChannel(Channel sourceChannel, int derivativeTime) throws DerivationException {
        this.sourceChannel = sourceChannel;
        if (sourceChannel == null) {
            throw new DerivationException("SourceChannel is null");
        }
        this.lastRecord = sourceChannel.getLatestRecord();
        this.derivativeTime = derivativeTime;
    }

    public int getDerivativeTime() {
        return derivativeTime;
    }

    public void setDerivativeTime(int derivativeTime) {
        this.derivativeTime = derivativeTime;
    }

    /**
     * Performs derivation.
     * 
     * @return the derived record value.
     * @throws DerivationException
     *             if an error occurs.
     */
    public Record derive() throws DerivationException {
        Record newRecord = sourceChannel.getLatestRecord();
        if (lastRecord.getFlag() == Flag.NO_VALUE_RECEIVED_YET) {
            lastRecord = newRecord;
            return new Record(Flag.NO_VALUE_RECEIVED_YET);
        }
        if (lastRecord.getFlag() != Flag.VALID || lastRecord.getTimestamp() == null || lastRecord.getValue() == null) {
            lastRecord = newRecord;
            return new Record(Flag.DRIVER_ERROR_CHANNEL_TEMPORARILY_NOT_ACCESSIBLE);
        }
        if (newRecord.getFlag() != Flag.VALID || newRecord.getTimestamp() == null || newRecord.getValue() == null) {
            throw new DerivationException("Unable to derive for invalid record: " + newRecord);
        }
        if (newRecord.getTimestamp() <= lastRecord.getTimestamp()) {
            throw new DerivationException("Unable to derive for invalid record with decreasing time");
        }
        if (newRecord.getValue().asDouble() < lastRecord.getValue().asDouble()) {
            lastRecord = newRecord;
            throw new DerivationException("Unable to derive for invalid record with decreasing value");
        }
        double deltaTime = (newRecord.getTimestamp() - lastRecord.getTimestamp())/(double) getDerivativeTime();
        double deltaValue = newRecord.getValue().asDouble() 
                          - lastRecord.getValue().asDouble();
        
        this.lastRecord = newRecord;
        
        return new Record(new DoubleValue(deltaValue/deltaTime), newRecord.getTimestamp(), Flag.VALID);
    }

}
