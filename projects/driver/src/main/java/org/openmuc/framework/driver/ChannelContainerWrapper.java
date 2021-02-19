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
package org.openmuc.framework.driver;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelContainer;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;

public abstract class ChannelContainerWrapper extends Configurable implements ChannelRecordContainer {

    private enum ChannelTaskType {
        WRITE,
        READ;
    }
    ChannelTaskType containerType;
    ChannelTaskContainer container;

    protected ChannelContainerWrapper() {
    }

    final ChannelContainer getContainer() {
        return container;
    }

    final void setContainer(ChannelTaskContainer container) throws ArgumentSyntaxException {
        this.container = container;
        if (container instanceof ChannelRecordContainer) {
            containerType = ChannelTaskType.READ;
        }
        else if (container instanceof ChannelValueContainer) {
            containerType = ChannelTaskType.WRITE;
        }
        else {
            throw new ArgumentSyntaxException("Invalid channel container: " + container.getClass().getSimpleName());
        }
    }

    @Override
    public final Channel getChannel() {
        return container.getChannel();
    }

    @Override
    public final Object getChannelHandle() {
        return container.getChannelHandle();
    }

    @Override
    public final void setChannelHandle(Object handle) {
    	this.container.setChannelHandle(handle);
    }

    @Override
    public final Record getRecord() {
        switch (containerType) {
        default:
        case READ:
            return getRecord((ChannelRecordContainer) container);
        case WRITE:
            return getRecord((ChannelValueContainer) container);
        }
    }

    private final Record getRecord(ChannelRecordContainer container) {
        return container.getRecord();
    }

    private final Record getRecord(ChannelValueContainer container) {
        return new Record(container.getValue(), System.currentTimeMillis());
    }

    @Override
    public final void setRecord(Record record) {
        switch (containerType) {
        default:
        case READ:
            setRecord((ChannelRecordContainer) container, record);
            break;
        case WRITE:
            setRecord((ChannelValueContainer) container, record);
            break;
        }
    }

    private final void setRecord(ChannelRecordContainer container, Record record) {
        container.setRecord(record);
    }

    private final void setRecord(ChannelValueContainer container, Record record) {
        container.setFlag(record.getFlag());
    }

    public final void setFlag(Flag flag) {
        switch (containerType) {
        default:
        case READ:
            setFlag((ChannelRecordContainer) container, flag);
            break;
        case WRITE:
            setFlag((ChannelValueContainer) container, flag);
            break;
        }
    }

    private final void setFlag(ChannelRecordContainer container, Flag flag) {
        container.setRecord(new Record(flag));
    }

    private final void setFlag(ChannelValueContainer container, Flag flag) {
        container.setFlag(flag);
    }

    public boolean equals(ChannelContainer container) {
        return this.container != null && container != null &&
                this.container.getChannel().getId().equals(container.getChannel().getId()) &&
                this.container.getChannel().getSettings().equals(container.getChannel().getSettings()) &&
                this.container.getChannel().getAddress().equals(container.getChannel().getAddress());
    }

}
