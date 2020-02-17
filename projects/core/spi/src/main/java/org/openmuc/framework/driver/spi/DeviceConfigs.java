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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DeviceConfigs<C extends Channel> extends DeviceContext {
    private static final Logger logger = LoggerFactory.getLogger(DeviceConfigs.class);

    final Map<String, C> channels = new HashMap<String, C>();

    protected DeviceConfigs() {
    }

    protected DeviceConfigs(String address, String settings) throws ArgumentSyntaxException {
        doConfigure(address, settings);
    }

    final void doConfigure(String address, String settings) throws ArgumentSyntaxException {
        configure(address, settings);
    	onConfigure();
    }

    protected void onConfigure() throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
    }

    public final DeviceContext getContext() {
        return this;
    }

    protected List<C> getChannels() {
    	return (List<C>) channels.values();
    }

    protected <T extends ChannelContainer> List<C> getChannels(List<T> containers) {
        List<C> channels = new LinkedList<C>();
        for (ChannelContainer container : containers) {
            try {
                channels.add(getChannel(container));
                
            } catch (ArgumentSyntaxException e) {
                logger.warn("Unable to configure channel \"{}\": {}", container.getChannel().getId(), e.getMessage());
                if (container instanceof ChannelRecordContainer) {
                    ((ChannelRecordContainer) container).setRecord(new Record(null, 
                            System.currentTimeMillis(), Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE));
                }
                else if (container instanceof ChannelValueContainer) {
                    ((ChannelValueContainer) container).setFlag(Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE);
                }
            }
        }
        return channels;
    }

	protected C getChannel(ChannelContainer container) throws ArgumentSyntaxException {
        String id = container.getChannel().getId();
        C channel = channels.get(id);
        if (channel == null) {
            channel = newChannel(container);
            channel.doCreate(this);
			channel.doConfigure(container);
			
            channels.put(id, channel);
        }
        else {
            channel.doConfigure(container);
        }
        return channel;
    }

    protected C newChannel(ChannelContainer container) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
		return context.newChannel();
	}

}
