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
package org.openmuc.framework.datalogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.datalogger.annotation.DataLogger;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingChannelContext extends Reflectable {

    private static final Logger logger = LoggerFactory.getLogger(LoggingChannelContext.class);

    Class<? extends LoggingChannel> channelClass;

    final Map<String, LoggingChannel> channels = new HashMap<String, LoggingChannel>();

    LoggingChannelContext() {
        channelClass = getChannelClass();
    }

    DataLogger getLoggerAnnotation() {
    	DataLogger logger = getClass().getAnnotation(DataLogger.class);
        if (logger == null) {
            throw new RuntimeException("Implementation invalid without annotation");
        }
        return logger;
    }

    @SuppressWarnings("unchecked")
    Class<? extends LoggingChannel> getChannelClass() {
        if (this instanceof LoggingChannelFactory) {
            try {
                Method method = getClass().getMethod("newChannel", Settings.class);
                return (Class<? extends LoggingChannel>) method.getReturnType();
                
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        DataLogger logger = getLoggerAnnotation();
        return logger.channel();
    }

    @SuppressWarnings("unchecked")
    final <C extends LoggingChannel> LoggingChannel newChannel(LogChannel configs) 
            throws RuntimeException, ArgumentSyntaxException {
        
        Settings settings = Configurations.parseSettings(configs.getLoggingSettings(), channelClass);
        
        C channel;
        if (this instanceof LoggingChannelFactory) {
            channel = (C) ((LoggingChannelFactory) this).newChannel(settings);
        }
        else {
            channel = (C) newInstance(channelClass);
        }
        return channel;
    }

    final LoggingChannel getChannel(LogChannel configs) throws ArgumentSyntaxException {
        String id = configs.getId();
        LoggingChannel channel = channels.get(id);
        try {
            if (channel == null) {
                channel = newChannel(configs);
                channels.put(id, channel);
            }
            channel.invokeConfigure(this, configs);
            
        } catch (ArgumentSyntaxException e) {
            
            channels.remove(id);
            
            throw e;
        }
        return channel;
    }

    public LoggingChannel getChannel(String id) {
        return channels.get(id);
    }

    public List<LoggingChannel> getChannels() {
        return (List<LoggingChannel>) channels.values();
    }

	final List<LoggingChannel> getChannels(List<? extends LoggingRecord> containers) {
        List<LoggingChannel> channels = new ArrayList<LoggingChannel>();
        if (containers == null || containers.isEmpty()) {
            logger.trace("Logger received empty container list");
            return channels;
        }
        for (LoggingRecord container : containers) {
        	LoggingChannel channel = getChannel(container.getChannelId());
            if (channel == null) {
                logger.trace("Failed to log record for unconfigured channel \"{}\"", container.getChannelId());
                continue;
            }
            try {
                if (channel.update(container.getRecord())) {
                    channels.add(channel);
                }
            } catch (TypeConversionException e) {
                logger.warn("Failed to prepare record to log to channel \"{}\": {}", container.getChannelId(), e.getMessage());
            }
        }
        return channels;
    }

}
