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

package org.openmuc.framework.datalogger.mqtt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.openmuc.framework.lib.mqtt.MqttConnection;
import org.openmuc.framework.lib.mqtt.MqttSettings;
import org.openmuc.framework.lib.mqtt.MqttWriter;
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

@Component
public class MqttLogger implements DataLoggerService {
    private static final Logger logger = LoggerFactory.getLogger(MqttLogger.class);
    private static final List<String> LOGGED_CHANNELS = new LinkedList<>();
    private static final HashMap<String, ParserService> PARSERS = new HashMap<>();
    private String parser;
    private boolean logMultiple;
    private MqttWriter writer;

    @Activate
    public void activate(ComponentContext context) {
        logger.info("Activating MQTT logger");
        getSettings();
        connect();
        listenForParser(context.getBundleContext());
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        logger.info("Deactivating MQTT logger");
        writer.getConnection().disconnect();
    }

    @Override
    public String getId() {
        return "mqttlogger";
    }

    @Override
    public void setChannelsToLog(List<LogChannel> channels) {
        LOGGED_CHANNELS.clear();

        for (LogChannel channel : channels) {
            LOGGED_CHANNELS.add(channel.getId());
        }
    }

    @Override
    public void log(List<LogRecordContainer> containers, long timestamp) {
        List<LogRecordContainer> loggedContainers = new ArrayList<>();

        for (LogRecordContainer container : containers) {
            if (LOGGED_CHANNELS.contains(container.getChannelId())) {
                if (logMultiple && container.getRecord().getValue() != null) {
                    loggedContainers.add(container);
                    continue;
                }
                parse(Collections.singletonList(container));
            }
        }
        if (logMultiple && !loggedContainers.isEmpty()) {
            parse(loggedContainers);
        }
    }

    @Override
    public void logEvent(List<LogRecordContainer> containers, long timestamp) {
        log(containers, timestamp);
    }

    /**
     * Parses a message with {@link ParserService} and publishes it
     *
     * @param containers
     *            List of {@link LogRecordContainer}
     */
    private void parse(List<LogRecordContainer> containers) {
        byte[] message;
        if (PARSERS.containsKey(parser)) {
            try {
                if (logMultiple) {
                    message = PARSERS.get(parser).serialize(containers);
                }
                else {
                    message = PARSERS.get(parser).serialize(containers.get(0));
                }
                writer.write(message);
                if (logger.isTraceEnabled()) {
                    logger.trace(new String(message));
                }
            } catch (SerializationException e) {
                logger.error(e.getMessage());
            }
            // joern: is a retrying a useful solution here?
            // catch (IOException e) {
            // logger.error("Couldn't write message to file buffer, retrying");
            // parse(containers);
            // }
        }
        else {
            logger.error("No parser available!");
        }
    }

    @Override
    public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads settings from system.properties
     */
    void getSettings() {
        String packageName = MqttLogger.class.getPackage().getName().toLowerCase();
        String host = System.getProperty(packageName + ".host");
        int port = Integer.parseInt(System.getProperty(packageName + ".port"));
        boolean ssl = Boolean.parseBoolean(System.getProperty(packageName + ".ssl"));
        String username = System.getProperty(packageName + ".username");
        String password = System.getProperty(packageName + ".password");
        String topic = System.getProperty(packageName + ".topic");
        parser = System.getProperty(packageName + ".parser");
        logMultiple = Boolean.parseBoolean(System.getProperty(packageName + ".multiple"));
        int maxFileCount = Integer.parseInt(System.getProperty(packageName + ".maxFileCount"));
        long maxFileSize = Long.parseLong(System.getProperty(packageName + ".maxFileSize")) * 1024;
        long maxBufferSize = Long.parseLong(System.getProperty(packageName + ".maxBufferSize")) * 1024;

        MqttSettings settings = new MqttSettings(host, port, username, password, ssl, maxBufferSize, maxFileSize,
                maxFileCount, topic);
        MqttConnection connection = new MqttConnection(settings);
        writer = new MqttWriter(connection);
    }

    /**
     * Connects to MQTT broker
     */
    void connect() {
        writer.getConnection().connect();
    }

    private void listenForParser(BundleContext context) {
        String filter = '(' + Constants.OBJECTCLASS + '=' + ParserService.class.getName() + ')';
        try {
            context.addServiceListener(event -> {
                ServiceReference<?> serviceReference = event.getServiceReference();
                String parserId = (String) serviceReference.getProperty("parserID");
                ParserService parser = (ParserService) context.getService(serviceReference);

                if (event.getType() == ServiceEvent.UNREGISTERING) {
                    logger.info("{} unregistering, removing Parser", parser.getClass().getName());
                    PARSERS.remove(parserId);
                }
                else {
                    logger.info("{} changed, updating Parser", parser.getClass().getName());
                    PARSERS.put(parserId, parser);
                }
            }, filter);
        } catch (InvalidSyntaxException e) {
            logger.error("Service listener can't be added to framework", e);
        }
    }
}
