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
package org.openmuc.framework.server.spi;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.RecordListener;

public class Channel extends ChannelContext {

	private org.openmuc.framework.dataaccess.Channel channel;

	private String settings = null;

	protected Channel() {
    }

    protected Channel(ServerMappingContainer container) throws ArgumentSyntaxException {
    	doConfigure(container);
    }

    void doConfigure(ServerMappingContainer container) throws ArgumentSyntaxException {
        if (this.settings == null || !settings.equals(container.getServerMapping().getServerAddress())) {
            configureSettings(container.getServerMapping().getServerAddress());
        }
        this.settings = container.getServerMapping().getServerAddress();
        this.channel = container.getChannel();
    	onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public final ChannelContext getContext() {
        return this;
    }

    public final String getId() {
        return channel.getId();
    }

    public final String getDescription() {
        return channel.getDescription();
    }

    public final String getUnit() {
        return channel.getUnit();
    }

    public final ValueType getValueType() {
        return channel.getValueType();
    }

	public org.openmuc.framework.dataaccess.Channel getChannel() {
		return channel;
	}

    public void addListener(RecordListener listener) {
    	channel.addListener(listener);
    }

    public void removeListener(RecordListener listener) {
    	channel.removeListener(listener);
    }

    public Record getLatestRecord() {
    	return channel.getLatestRecord();
    }

}
