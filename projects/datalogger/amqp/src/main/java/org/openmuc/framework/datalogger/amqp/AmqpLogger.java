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

package org.openmuc.framework.datalogger.amqp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.lib.amqp.AmqpConnection;
import org.openmuc.framework.lib.amqp.AmqpSettings;
import org.openmuc.framework.lib.amqp.AmqpWriter;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Component
public class AmqpLogger implements DataLoggerService {

    private static final Logger logger = LoggerFactory.getLogger(AmqpLogger.class);
    private final HashMap<String, LogChannel> channelsToLog = new HashMap<>();
    private final HashMap<String, ParserService> parsers = new HashMap<>();
    private AmqpConnection connection;
    private AmqpWriter writer;
    private Settings settings;

    @Activate
    protected void activate(ComponentContext context) throws Exception {
        logger.info("Activating Amqp logger");
        connect();
        String filter = '(' + Constants.OBJECTCLASS + '=' + ParserService.class.getName() + ')';
        BundleContext bundleContext = context.getBundleContext();
        try {
            bundleContext.addServiceListener(event -> {
                ServiceReference<?> serviceReference = event.getServiceReference();
                String parserId = (String) serviceReference.getProperty("parserID");
                ParserService parserService = (ParserService) bundleContext.getService(serviceReference);
                String parserServiceName = parserService.getClass().getName();

                if (event.getType() == ServiceEvent.UNREGISTERING) {
                    logger.info("{} unregistering, removing Parser", parserServiceName);
                    parsers.remove(parserId);
                }
                else {
                    logger.info("{} changed, updating Parser", parserServiceName);
                    parsers.put(parserId, parserService);
                }
            }, filter);
        } catch (InvalidSyntaxException e) {
            logger.error("Service listener can't be added to framework", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) throws IOException, TimeoutException {
        logger.info("Deactivating Amqp logger");
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public String getId() {
        return "amqplogger";
    }

    @Override
    public void setChannelsToLog(List<LogChannel> logChannels) {
        channelsToLog.clear();

        for (LogChannel logChannel : logChannels) {
            String channelId = logChannel.getId();
            channelsToLog.put(channelId, logChannel);
        }
    }

    @Override
    public synchronized void log(List<LogRecordContainer> containers, long timestamp) {
        for (LogRecordContainer container : containers) {
            if (channelsToLog.containsKey(container.getChannelId())) {
                byte[] message;
                if (parsers.containsKey(settings.getParser())) {
                    try {
                        message = parsers.get(settings.getParser()).serialize(container);
                    } catch (SerializationException e) {
                        logger.error(e.getMessage());
                        return;
                    }
                }
                else {
                    Gson gson = new Gson();
                    message = gson.toJson(container.getRecord()).getBytes();
                }
                writer.write(settings.getFramework() + settings.getSeparator() + container.getChannelId(), message);
            }
        }
    }

    @Override
    public void logEvent(List<LogRecordContainer> containers, long timestamp) {
        log(containers, timestamp);
    }

    @Override
    public List<Record> getRecords(String channelId, long startTime, long endTime) {
        throw new UnsupportedOperationException();
    }

    private void connect() throws ConnectionException {
        settings = new Settings();

        AmqpSettings amqpSettings = new AmqpSettings(settings.getHost(), settings.getPort(), settings.getVirtualHost(),
                settings.getUsername(), settings.getPassword(), settings.isSsl(), settings.getExchange());
        try {
            connection = new AmqpConnection(amqpSettings);
        } catch (Exception e) {
            throw new ConnectionException(e);
        }
        writer = new AmqpWriter(connection);
    }
}
