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
package org.openmuc.framework.app.node;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.app.node.ChannelListener.NodeCallbacks;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {})
public final class NodeApp implements NodeCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(NodeApp.class);

    private final List<ChannelListener> channels = new ArrayList<ChannelListener>(); 

    @Reference
    private DataAccessService dataAccessService;

    private Channel node;

    @Activate
    private void activate() {
        logger.info("Activating Node App");
        try {
            NodeConfig config = new NodeConfig();
            
            node = initializeChannel(config.getNodeChannel());
            for (String id : config.getPlusChannels()) {
            	registerChannelListener(id,  1);
            }
            for (String id : config.getMinusChannels()) {
            	registerChannelListener(id, -1);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.error("Error while applying configuration: {}", e.getMessage());
        }
    }

    protected Channel initializeChannel(String id) throws IllegalArgumentException {
        Channel channel = dataAccessService.getChannel(id);
        if (channel == null) {
            throw new IllegalArgumentException("Unable to find Channel for id: " + id);
        }
        return channel;
    }

    protected void registerChannelListener(String id, double scale) throws IllegalArgumentException {
        Channel channel = initializeChannel(id);
        ChannelListener listener = new ChannelListener(this, channel, scale);
        channel.addListener(listener);
        channels.add(listener);
    }

    @Deactivate
    private void deactivate() {
        logger.info("Deactivating Node App");
    }

	@Override
	public synchronized void onNodeUpdate() {
		double sum = 0;
		for (ChannelListener channel : channels) {
			sum += channel.getValue();
		}
		this.node.setLatestRecord(new Record(new DoubleValue(sum), System.currentTimeMillis()));
    }

}
