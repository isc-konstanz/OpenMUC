/*
 * Copyright 2011-18 Fraunhofer ISE
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
package org.openmuc.framework.datalogger;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.options.Setting;

public class Channel extends ChannelContext {

    @Setting(id={"intervalMax", "loggingMaxInterval"}, mandatory = false)
    private int intervalMax = 0;

    @Setting(id= {"tolerance", "loggingTolerance"}, mandatory = false)
    private double tolerance = 0;

    @Setting(mandatory = false)
    private boolean average = false;

    private String settings = "";

    Record record = new Record(Flag.DATA_LOGGING_NOT_ACTIVE);

    protected void doConfigure() throws ArgumentSyntaxException {
        doConfigure(channel.getLoggingSettings());
        onConfigure();
    }

    protected void doConfigure(String settings) throws ArgumentSyntaxException {
        if (!this.settings.equals(settings)) {
            configureSettings(settings);
        }
        this.settings = settings;
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
        if (intervalMax < 0) {
            throw new ArgumentSyntaxException("Invalid maximum logging interval for channel: " + getId());
        }
        else {
            this.intervalMax = Math.max(getLoggingInterval(), intervalMax);
        }
    }

    public final ChannelContext getContext() {
        return this;
    }

    public final int getLoggingIntervalMax() {
        return intervalMax;
    }

    public final double getLoggingTolerance() {
        return tolerance;
    }

    public final boolean isAveraging() {
        return average;
    }

    public final Record getRecord() {
        return record;
    }

    public Value getValue() {
        if (record == null) {
            return null;
        }
        return record.getValue();
    }

    public Long getTime() {
        if (record == null) {
            return null;
        }
        return record.getTimestamp();
    }

    public Flag getFlag() {
        if (record == null) {
            return null;
        }
        return record.getFlag();
    }

    public boolean isValid() {
        if (record != null && record.getFlag() == Flag.VALID && record.getValue() != null) {
            return true;
        }
        return false;
    }

    final List<Record> doRead(long startTime, long endTime) throws IOException {
        return onRead(startTime, endTime);
    }

    protected List<Record> onRead(long startTime, long endTime) throws IOException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    final void doWrite(long timestamp) throws IOException {
        onWrite(record, timestamp);
    }

    protected void onWrite(Record record, long timestamp) throws IOException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

}
