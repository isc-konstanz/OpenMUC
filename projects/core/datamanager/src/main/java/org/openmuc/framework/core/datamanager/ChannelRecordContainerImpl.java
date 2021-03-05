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

package org.openmuc.framework.core.datamanager;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.ReadRecordContainer;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;

public final class ChannelRecordContainerImpl extends ChannelContainerImpl implements ReadRecordContainer, ChannelRecordContainer {

    private static final Record RECORD_DEFAULT = new Record(Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);

    private Record record;
    private Object handle;

    public ChannelRecordContainerImpl(ChannelImpl channel) {
        this(channel, RECORD_DEFAULT);
    }

    private ChannelRecordContainerImpl(ChannelImpl channel, Record record) {
        super(channel);
        this.record = record;
        this.handle = channel.handle;
    }

    @Override
    public Object getChannelHandle() {
        return handle;
    }

    @Override
    public void setChannelHandle(Object handle) {
        this.handle = handle;
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public void setRecord(Record record) {
        this.record = record;
    }

    @Override
    public ChannelRecordContainer copy() {
        return new ChannelRecordContainerImpl(channel, getRecord());
    }

}
