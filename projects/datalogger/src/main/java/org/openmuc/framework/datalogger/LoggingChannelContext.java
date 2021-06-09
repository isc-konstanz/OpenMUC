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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.datalogger.annotation.DataLogger;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoggingChannelContext extends Reflectable {

    private static final Logger logger = LoggerFactory.getLogger(LoggingChannelContext.class);

    Class<? extends LoggingChannel> channelClass;

    final Map<String, LoggingChannel> channels = new HashMap<String, LoggingChannel>();

    LoggingChannelContext() {
        channelClass = getChannelClass();
    }

    abstract String getId();

    DataLogger getLoggerAnnotation() {
    	DataLogger logger = getClass().getAnnotation(DataLogger.class);
        if (logger == null) {
            throw new RuntimeException("Implementation invalid without annotation");
        }
        return logger;
    }

    @SuppressWarnings("unchecked")
    Class<? extends LoggingChannel> getChannelClass() {
        if (LoggingChannelFactory.class.isAssignableFrom(getClass())) {
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
	final <C extends LoggingChannel> C newChannel(LogChannel configs) 
            throws RuntimeException, ArgumentSyntaxException {
        
    	C channel;
        if (this instanceof LoggingChannelFactory) {
            channel = (C) ((LoggingChannelFactory) this).newChannel(parseSettings(channelClass, configs.getLoggingSettings()));
        }
        else {
            channel = (C) newInstance(channelClass);
        }
        return channel;
    }

	final <C extends LoggingChannel> C getChannel(LogChannel configs) throws ArgumentSyntaxException {
        String id = configs.getId();
        C channel = getChannel(id);
        try {
            if (channel == null) {
                channel = newChannel(configs);
                channels.put(id, channel);
            }
            Settings settings = parseSettings(channel.getClass(), configs.getLoggingSettings());
            
            channel.invokeConfigure(this, configs, settings);
            
        } catch (ArgumentSyntaxException e) {
            
            channels.remove(id);
            throw e;
        }
        return channel;
    }

    @SuppressWarnings("unchecked")
	public <C extends LoggingChannel> C getChannel(String id) {
        return (C) channels.get(id);
    }

    @SuppressWarnings("unchecked")
	public <C extends LoggingChannel> List<C> getChannels() {
        return new ArrayList<C>((Collection<C>) channels.values());
    }

	final <C extends LoggingChannel> List<C> getChannels(List<? extends LoggingRecord> containers) {
        List<C> channels = new ArrayList<C>();
        if (containers == null || containers.isEmpty()) {
            logger.trace("Logger received empty container list");
            return channels;
        }
        for (LoggingRecord container : containers) {
        	C channel = getChannel(container.getChannelId());
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

    Settings parseSettings(Class<? extends LoggingChannel> channelClass, String settings) throws ArgumentSyntaxException {
        String loggerSettings = "";
        for (String loggerSegment : settings.split(";")) {
        	String[] loggerSettingsPair = loggerSegment.split(":");
            if (loggerSettingsPair[0].equals(getId()) && loggerSettingsPair.length == 2) {
                loggerSettings = loggerSettingsPair[1];
                break;
            }
        }
        return new Settings(loggerSettings, channelClass, new LoggingSyntax());
    }

}
