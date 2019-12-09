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

public abstract class Channel extends ChannelContext {

    protected Channel() {
    }

    protected Channel(ChannelContainer container) throws ArgumentSyntaxException {
    	doConfigure(container);
    }

    public final ChannelContext getContext() {
        return this;
    }

    public void onStartListening(RecordsReceivedListener listener) 
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    public Record onRead() throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        return onRead(System.currentTimeMillis());
    }

    public Record onRead(long timestamp)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    public Flag onWrite(Value value)
            throws UnsupportedOperationException, ConnectionException {
        // Placeholder for the optional implementation
        throw new UnsupportedOperationException();
    }

    final void doStartListening(RecordsReceivedListener listener) 
            throws ConnectionException, UnsupportedOperationException {
        onStartListening(listener);
    }

    final void doRead(long timestamp) throws ConnectionException, UnsupportedOperationException {
        setRecord(onRead(timestamp));
    }

    final void doWrite() throws ConnectionException, UnsupportedOperationException {
        setFlag(onWrite(getValue()));
    }

}
