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
package org.openmuc.framework.driver.csv.test.utils;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;

public class CsvChannelRecordContainer implements ChannelRecordContainer {

    private Record record;
    private final CsvTestChannel channel;

    CsvChannelRecordContainer(CsvTestChannel channel) {
        this.channel = channel;
    }

    public CsvChannelRecordContainer(String address) {
        this(new CsvTestChannel(address));
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public String getChannelAddress() {
        return this.channel.getAddress();
    }

    @Override
    public String getChannelSettings() {
        return this.channel.getSettings();
    }

    @Override
    public Object getChannelHandle() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setChannelHandle(Object handle) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setRecord(Record record) {
        this.record = record;
    }

    @Override
    public CsvChannelRecordContainer copy() {
    	CsvChannelRecordContainer csvChannelRecordContainer = new CsvChannelRecordContainer(channel);
    	csvChannelRecordContainer.record = record;
    	return csvChannelRecordContainer;
    }

}
