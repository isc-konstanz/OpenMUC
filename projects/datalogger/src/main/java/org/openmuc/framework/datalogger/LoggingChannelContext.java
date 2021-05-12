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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingChannelContext extends Configurable implements LoggingChannelFactory {

    private static final Logger logger = LoggerFactory.getLogger(LoggingChannelContext.class);

    Class<? extends LoggingChannel> channelClass;

    final Map<String, LoggingChannel> channels = new HashMap<String, LoggingChannel>();

    LoggingChannelContext() {
        channelClass = getChannelClass();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends LoggingChannel> getChannelClass() {
        Class<?> loggerClass = getClass();
        while (loggerClass.getSuperclass() != null) {
            if (loggerClass.getSuperclass().equals(DataLoggerActivator.class)) {
                break;
            }
            loggerClass = loggerClass.getSuperclass();
        }
        // This operation is safe. Because deviceClass is a direct sub-class, getGenericSuperclass() will
        // always return the Type of this class. Because this class is parameterized, the cast is safe
        ParameterizedType superClass = (ParameterizedType) loggerClass.getGenericSuperclass();
        return (Class<? extends LoggingChannel>) superClass.getActualTypeArguments()[0];
    }

    public LoggingChannel getChannel(String id) {
        return channels.get(id);
    }

    final LoggingChannel getChannel(LogChannel configs) throws ArgumentSyntaxException {
        String id = configs.getId();
        LoggingChannel channel = channels.get(id);
        try {
            if (channel == null) {
                channel = newChannel(configs);
                channel.doCreate(this);
                channel.doConfigure(configs);
                
                channels.put(id, channel);
            }
            else {
                channel.doConfigure(configs);
            }
        } catch (ArgumentSyntaxException e) {
            
            channels.remove(id);
            
            throw e;
        }
        return channel;
    }

    final LoggingChannel newChannel(LogChannel channel) throws ArgumentSyntaxException {
        return this.newChannel(channel.getLoggingSettings());
    }

    public LoggingChannel newChannel(String settings) throws ArgumentSyntaxException {
        return this.newChannel(Configurations.parseSettings(settings, channelClass));
    }

    @Override
    public LoggingChannel newChannel(Settings settings) throws ArgumentSyntaxException {
        return this.newChannel();
    }

    protected LoggingChannel newChannel() {
        try {
            return channelClass.getDeclaredConstructor().newInstance();
            
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(MessageFormat.format("Unable to instance {0}: {1}", 
                    channelClass.getSimpleName(), e.getMessage()));
        }
    }

    final void bindChannel(Class<? extends LoggingChannel> channelClass) {
        this.channelClass = channelClass;
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
        	LoggingChannel channel = (LoggingChannel) getChannel(container.getChannelId());
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
