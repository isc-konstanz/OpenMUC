/*
 * Copyright 2011-2022 Fraunhofer ISE
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

import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.annotation.Configure;
import org.openmuc.framework.datalogger.annotation.Read;
import org.openmuc.framework.datalogger.annotation.Write;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataLoggerActivator extends LoggingChannelContext implements DataLoggerService {

    private static final Logger logger = LoggerFactory.getLogger(DataLoggerActivator.class);

    private final String id;

    public DataLoggerActivator() {
        super();
        this.id = getLoggerAnnotation().id();
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final void setChannelsToLog(List<LogChannel> logChannels) {
        // Will only be called when OpenMUC receives new logging configurations
        // TODO: Don't clear channels, but remove redundant
        channels.clear();
        try {
            List<LoggingChannel> channels = new LinkedList<LoggingChannel>();
            for (LogChannel logChannel : logChannels) {
                try {
                    channels.add(getChannel(logChannel));
                    
                } catch (ArgumentSyntaxException | NullPointerException e) {
                    logger.warn("Unable to configure channel \"{}\": {}", logChannel.getId(), e.getMessage());
                }
            }
            invokeMethod(Configure.class, this, channels);
            invokeMethod(Configure.class, this);
            
        } catch (Exception e) {
            logger.error("Error while configuring logger:", e);
        }
    }

    @Override
    public boolean logSettingsRequired() {
        Options channelSettings = null;
        if (channelClass != LoggingChannel.class) {
            channelSettings = Options.parse(SETTING, channelClass);
            if (channelSettings != null) {
                return channelSettings.size() > 0;
                //return channelSettings.getMandatoryCount() > 0;
            }
        }
        return false;
    }

    @Override
    public final void log(List<LoggingRecord> containers, long timestamp) {
        try {
            synchronized(channels) {
            	List<LoggingChannel> channels = getChannels(containers);
            	
                if (hasMethod(Write.class, this, channels, timestamp)) {
                    invokeMethod(Write.class, this, channels, timestamp);
                }
                else if (hasMethod(Write.class, channelClass, timestamp)) {
                    for (LoggingChannel logChannel : getChannels(containers)) {
                        logChannel.invokeWrite(timestamp);
                    }
                }
                else {
                    throw new UnsupportedOperationException("Logging values unsupported for " + getClass().getSimpleName());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to log channels: {}", e.getMessage());
        }
    }

    @Override
    public void logEvent(List<LoggingRecord> containers, long timestamp) {
        this.log(containers, timestamp);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Record> getRecords(String id, long startTime, long endTime) throws IOException {
        synchronized(channels) {
            LoggingChannel logChannel = getChannel(id);
            if (logChannel == null) {
                logger.warn("Failed to retrieve records for unconfigured channel \"{}\"", id);
                return null;
            }
            if (hasMethod(Read.class, this, logChannel, startTime, endTime)) {
                return (List<Record>) invokeReturn(Read.class, this, logChannel, startTime, endTime);
            }
            else if (hasMethod(Read.class, channelClass, startTime, endTime)) {
                return logChannel.invokeRead(startTime, endTime);
            }
        }
        throw new UnsupportedOperationException("Reading values unsupported for " + getClass().getSimpleName());
    }

	@Override
	public Record getLatestLogRecord(String id) throws IOException {
        synchronized(channels) {
            LoggingChannel logChannel = getChannel(id);
            if (logChannel == null) {
                logger.warn("Failed to retrieve latest record for unconfigured channel \"{}\"", id);
                return null;
            }
            if (hasMethod(Read.class, this, logChannel)) {
                return (Record) invokeReturn(Read.class, this, logChannel);
            }
            else if (hasMethod(Read.class, channelClass)) {
                return logChannel.invokeRead();
            }
        }
        throw new UnsupportedOperationException("Reading values unsupported for " + getClass().getSimpleName());
	}

}
