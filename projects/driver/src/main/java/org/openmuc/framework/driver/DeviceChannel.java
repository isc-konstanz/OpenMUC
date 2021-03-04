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
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;

public abstract class DeviceChannel extends ChannelContainerWrapper {

    ChannelContext context;

    protected DeviceChannel() {
    }

    final void doCreate(ChannelContext context) throws ArgumentSyntaxException {
        this.context = context;
        this.onCreate(context);
        this.onCreate();
    }

    protected void onCreate(ChannelContext context) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onCreate() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    protected void onDestroy() {
        // Placeholder for the optional implementation
    }

    protected void doConfigure(ChannelTaskContainer container) throws ArgumentSyntaxException {
        if (!equals(container)) {
            doConfigure(container.getChannel().getAddress(), container.getChannel().getSettings());
            onConfigure();
        }
        setTaskContainer(container);
    }

    protected void doConfigure(String address, String settings) throws ArgumentSyntaxException {
        configure(address, settings);
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
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

    protected Record onRead(long timestamp)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    final void doWrite() throws ConnectionException, UnsupportedOperationException {
        setFlag(onWrite(getRecord()));
    }

    protected Flag onWrite(Record record)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    public final ChannelContext getContext() {
        return context;
    }

    private Channel getChannel() {
    	return container.getChannel();
    }

	public String getId() {
		return getChannel().getId();
	}

	public String getDescription() {
		return getChannel().getDescription();
	}

	public String getUnit() {
		return getChannel().getUnit();
	}

	public ValueType getValueType() {
		return getChannel().getValueType();
	}

	public int getValueTypeLength() {
		return getChannel().getValueTypeLength();
	}

    @Override
    public String toString() {
        return getId() + " (" + getValueType().toString() + "); " + getRecord().toString();
    }

//	@Override
//	public Channel copy() {
//		Channel channel = context.newChannel();
//		try {
//			channel.doCreate(context);
//			channel.doConfigure(container);
//			
//		} catch (ArgumentSyntaxException e) {
//			// Impossible to occur and may be ignored
//		}
//		return channel;
//	}

}
