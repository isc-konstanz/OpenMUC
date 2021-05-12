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
package org.openmuc.framework.driver;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.driver.annotation.Listen;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.annotation.Write;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;

public abstract class DriverChannel extends ChannelContainerWrapper {

    DriverChannelContext context;

    protected DriverChannel() {
    }

    void invokeConfigure(DriverChannelContext context, ChannelTaskContainer container) 
    		throws ArgumentSyntaxException {
    	super.invokeConfigure(context, container);
        this.context = context;
    }

    public final DriverChannelContext getContext() {
        return context;
    }

    private final Channel getChannel() {
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

    final void invokeListening(RecordsReceivedListener listener) throws ConnectionException {
    	invokeMethod(Listen.class, this, listener);
    }

    final void invokeRead(long timestamp) throws ConnectionException {
    	Record record = (Record) invokeReturn(Read.class, this, timestamp);
        setRecord(record);
    }

    final void invokeWrite() throws ConnectionException {
    	Flag flag = (Flag) invokeReturn(Write.class, this, getRecord());
        setFlag(flag);
    }

    @Override
    public String toString() {
        return getId() + " (" + getValueType().toString() + "); " + getRecord().toString();
    }

}
