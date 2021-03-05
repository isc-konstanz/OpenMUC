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

package org.openmuc.framework.datalogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.spi.DataLoggerActivator;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataLogger<C extends DataChannel> extends ChannelContext implements DataLoggerActivator {

    private static final Logger logger = LoggerFactory.getLogger(DataLogger.class);

    public DataLogger() {
    	super();
        try {
	        doCreate();
	        
		} catch (Exception e) {
            logger.warn("Error instancing data logger {}: {}", getId(), e.getMessage());
		}
    }

    void doCreate() throws Exception {
    	onCreate();
    }

    protected void onCreate() throws Exception {
        // Placeholder for the optional implementation
    }

    @Override
    public final void activate(DataAccessService dataAccess) {
        try {
            doActivate(dataAccess);
            
        } catch (Exception e) {
            logger.warn("Error activating data logger {}: {}", getId(), e.getMessage());
        }
    }

    void doActivate(DataAccessService dataAccess) throws Exception {
        onActivate(dataAccess);
        onActivate();
    }

    protected void onActivate(DataAccessService dataAccess) throws Exception {
        // Placeholder for the optional implementation
    }

    protected void onActivate() throws Exception {
        // Placeholder for the optional implementation
    }

    @Override
    public final void deactivate() {
        try {
            doDeactivate();
            doDestroy();
            
        } catch (Exception e) {
            logger.warn("Error deactivating data logger {}: {}", getId(), e.getMessage());
        }
    }

    void doDeactivate() throws Exception {
        onDeactivate();
    }

    protected void onDeactivate() throws Exception {
        // Placeholder for the optional implementation
    }

    void doDestroy() throws Exception {
        for (DataChannel channel : channels.values()) {
            channel.onDestroy();
        }
        channels.clear();
        onDestroy();
    }

    protected void onDestroy() throws Exception {
        // Placeholder for the optional implementation
    }

	@Override
    @SuppressWarnings("unchecked")
    public final void setChannelsToLog(List<LogChannel> logChannels) {
        // Will be called if OpenMUC receives new logging configurations
        channels.clear();
        try {
            List<C> channels = new LinkedList<C>();
            for (LogChannel logChannel : logChannels) {
                try {
    				channels.add((C) getChannel(logChannel));
    				
    			} catch (ArgumentSyntaxException | NullPointerException e) {
                    logger.warn("Unable to configure channel \"{}\": {}", logChannel.getId(), e.getMessage());
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
    @SuppressWarnings("unchecked")
    public final void log(List<LogRecordContainer> containers, long timestamp) {
        try {
            synchronized(channels) {
            	onWrite((List<C>) getChannels(containers), timestamp);
            }
        } catch (IOException e) {
            logger.error("Failed to log channels: {}", e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void logEvent(List<LogRecordContainer> containers, long timestamp) {
        try {
            synchronized(channels) {
            	onWriteEvent((List<C>) getChannels(containers), timestamp);
            }
        } catch (IOException e) {
            logger.error("Failed to log channels: {}", e.getMessage());
        }
    }

    protected void onWriteEvent(List<C> channels, long timestamp) throws IOException {
        // Placeholder for the optional implementation
        this.onWrite(channels, timestamp);
    }

    protected void onWrite(List<C> channels, long timestamp) throws IOException {
        // Placeholder for the optional implementation
        for (C channel : channels) {
            channel.doWrite(timestamp);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Record> getRecords(String id, long startTime, long endTime) throws IOException {
        synchronized(channels) {
            C channel = (C) getChannel(id);
            if (channel == null) {
                logger.warn("Failed to retrieve records for unconfigured channel \"{}\"", id);
                return null;
            }
            
            return onRead(channel, startTime, endTime);
        }
    }

    protected List<Record> onRead(C channel, long startTime, long endTime) throws IOException {
        // Placeholder for the optional implementation
        return channel.doRead(startTime, endTime);
    }

}
