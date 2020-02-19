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

package org.openmuc.framework.datalogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataLogger<C extends Channel> extends DataLoggerContext {
    private static final Logger logger = LoggerFactory.getLogger(DataLogger.class);

	private final Map<String, ChannelHandler<C>> handlers = new HashMap<String, ChannelHandler<C>>();

	private DataAccessService dataAccess = null;

    @Override
    public final DataLogger<C> getDataLogger() {
    	return this;
    }

    public final DataLoggerContext getContext() {
        return this;
    }

    public final void activate(DataAccessService dataAccess) {
    	this.dataAccess = dataAccess;
    	try {
			onActivate(dataAccess);
	    	onActivate();
	    	
		} catch (Exception e) {
			logger.warn("Error activating data logger {}: {}", getId(), e.getMessage());
		}
    }

    public final void deactivate() {
    	onDeactivate();
    }

    protected void onActivate(DataAccessService dataAccess) throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onActivate() throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onDeactivate() {
        // Placeholder for the optional implementation
    }

	@Override
	public final void setChannelsToLog(List<LogChannel> logChannels) {
		// Will be called if OpenMUC receives new logging configurations
		handlers.clear();
		try {
			List<C> channels = new LinkedList<C>();
			for (LogChannel configs : logChannels) {
				String id = configs.getId();
				try {
					C channel = doCreateChannel(configs);
					
					ChannelHandler<C> handler;
					if (channel.isAveraging()) {
						handler = new ChannelHandlerAverage<C>(channel);
						channel.addListener((ChannelHandlerAverage<C>) handler);
						((ChannelHandlerAverage<C>) handler).setListening(true);
					}
					else if (channel.getLoggingIntervalMax() > channel.getLoggingInterval()) {
						handler = new ChannelHandlerDynamic<C>(channel);
					}
					else {
						handler = new ChannelHandler<C>(channel);
					}
					handlers.put(id, handler);
					channels.add(channel);
					
					logger.debug("{} \"{}\" configured to log every {}s", 
							handler.getClass().getSimpleName(), id, configs.getLoggingInterval()/1000);
				}
				catch (ArgumentSyntaxException e) {
					logger.warn("Failed to configure channel \"{}\": {}", id, e.getMessage());
				}
			}
			onConfigure(channels);
			
		} catch (Exception e) {
			logger.error("Error while configuring channels:", e);
		}
	}

	protected void onConfigure(List<C> channels) throws IOException {
        // Placeholder for the optional implementation
    }

	@Override
	public final void log(List<LogRecordContainer> containers, long timestamp) {
		if (containers == null || containers.isEmpty()) {
			logger.trace("Requested Emoncms logger to log an empty container list");
			return;
		}
        synchronized(handlers) {
    		List<C> channels = new LinkedList<C>();
    		for (LogRecordContainer container : containers) {
    			if (!handlers.containsKey(container.getChannelId())) {
    				logger.trace("Failed to log record for unconfigured channel \"{}\"", container.getChannelId());
    				continue;
    			}
    			try {
    				ChannelHandler<C> handler = handlers.get(container.getChannelId());
    				if (handler.update(container.getRecord())) {
    					channels.add(handler.getChannel());
    				}
    			} catch (TypeConversionException e) {
    				logger.warn("Failed to prepare record to log to channel \"{}\": {}", container.getChannelId(), e.getMessage());
    			}
    		}
    		try {
				onWrite(channels, timestamp);
				
			} catch (IOException e) {
				logger.error("Failed to log channels: {}", e.getMessage());
			}
        }
	}

	protected void onWrite(List<C> channels, long timestamp) throws IOException {
        // Placeholder for the optional implementation
		for (C channel : channels) {
			channel.doWrite(timestamp);
		}
    }

	@Override
	public List<Record> getRecords(String id, long startTime, long endTime) throws IOException {
        synchronized(handlers) {
    		if (!handlers.containsKey(id)) {
    			logger.warn("Failed to retrieve records for unconfigured channel \"{}\"", id);
    			return null;
    		}
    		C channel = handlers.get(id).getChannel();
    		
    		return onRead(channel, startTime, endTime);
        }
	}

	protected List<Record> onRead(C channel, long startTime, long endTime) throws IOException {
        // Placeholder for the optional implementation
		return channel.doRead(startTime, endTime);
    }

    final C doCreateChannel(LogChannel configs) throws ArgumentSyntaxException {
    	C channel = onCreateChannel(configs);
    	channel.doCreate(this, dataAccess.getChannel(configs.getId()));
    	channel.doConfigure();
    	
		return channel;
	}

    protected C onCreateChannel(LogChannel configs) throws ArgumentSyntaxException {
        // Placeholder for the optional implementation
		return super.newChannel();
	}

}
