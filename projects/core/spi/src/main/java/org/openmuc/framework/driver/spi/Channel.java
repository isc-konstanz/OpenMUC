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
package org.openmuc.framework.driver.spi;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;

public class Channel extends ChannelContext implements ChannelRecordContainer, ChannelValueContainer {

	private ChannelContainer container;

	protected Channel() {
    }

    protected Channel(ChannelContainer container) throws ArgumentSyntaxException {
    	doConfigure(container);
    }

    void doConfigure(ChannelContainer container) throws ArgumentSyntaxException {
        if (this.container == null ||
        		!this.container.getChannelSettings().equals(container.getChannelSettings()) ||
                !this.container.getChannelAddress().equals(container.getChannelAddress())) {
            configure(container.getChannelAddress(), container.getChannelSettings());
        }
        this.container = container;
    	onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public final ChannelContext getContext() {
        return this;
    }

    final void doStartListening(RecordsReceivedListener listener) 
            throws ConnectionException, UnsupportedOperationException {
        onStartListening(listener);
    }

    protected void onStartListening(RecordsReceivedListener listener) 
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    final void doRead(long timestamp) throws ConnectionException, UnsupportedOperationException {
        setRecord(onRead(timestamp));
    }

    protected Record onRead() throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        return onRead(System.currentTimeMillis());
    }

    protected Record onRead(long timestamp)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    final void doWrite() throws ConnectionException, UnsupportedOperationException {
        setFlag(onWrite(getValue()));
    }

    protected Flag onWrite(Value value)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    public final String getId() {
        return container.getChannel().getId();
    }

    public final String getDescription() {
        return container.getChannel().getDescription();
    }

    public final String getUnit() {
        return container.getChannel().getUnit();
    }

    public final ValueType getValueType() {
        return container.getChannel().getValueType();
    }

	@Override
	public org.openmuc.framework.dataaccess.Channel getChannel() {
		return container.getChannel();
	}

	@Override
	@Deprecated
	public String getChannelAddress() {
		return container.getChannelAddress();
	}

	@Override
	@Deprecated
	public String getChannelSettings() {
		return container.getChannelSettings();
	}

	@Override
	public Object getChannelHandle() {
        return container.getChannelHandle();
	}

	@Override
	public void setChannelHandle(Object handle) {
		this.container.setChannelHandle(handle);
	}

	@Override
    public final Value getValue() {
		if (container instanceof ChannelRecordContainer) return ((ChannelRecordContainer) container).getRecord().getValue();
		if (container instanceof ChannelValueContainer) return ((ChannelValueContainer) container).getValue();
		return null;
    }

	@Override
    public final Flag getFlag() {
		if (container instanceof ChannelRecordContainer) return ((ChannelRecordContainer) container).getRecord().getFlag();
		if (container instanceof ChannelValueContainer) return ((ChannelValueContainer) container).getFlag();
		return Flag.UNKNOWN_ERROR;
    }

	@Override
    public final void setFlag(Flag flag) {
		if (container instanceof ChannelRecordContainer) ((ChannelRecordContainer) container).setRecord(new Record(flag));
		if (container instanceof ChannelValueContainer) ((ChannelValueContainer) container).setFlag(flag);
    }

	@Override
	public Record getRecord() {
		if (container instanceof ChannelRecordContainer) return ((ChannelRecordContainer) container).getRecord();
		if (container instanceof ChannelValueContainer) return new Record(getValue(), System.currentTimeMillis());
		return null;
	}

	@Override
    public final void setRecord(Record record) {
		if (container instanceof ChannelRecordContainer) ((ChannelRecordContainer) container).setRecord(record);
		if (container instanceof ChannelValueContainer) ((ChannelValueContainer) container).setFlag(record.getFlag());
    }

	@Override
	public ChannelRecordContainer copy() {
		try {
			return new Channel(container);
			
		} catch (ArgumentSyntaxException e) {
			// Cannot happen, as only containers with valid syntax will be instanced
			return null;
		}
	}

}
