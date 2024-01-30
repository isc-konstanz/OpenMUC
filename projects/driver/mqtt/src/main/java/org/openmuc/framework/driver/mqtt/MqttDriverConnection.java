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

package org.openmuc.framework.driver.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.driver.mqtt.MqttDriverSettings.MqttChannelAddress;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.framework.lib.mqtt.MqttConnection;
import org.openmuc.framework.lib.mqtt.MqttReader;
import org.openmuc.framework.lib.mqtt.MqttWriter;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationException;
import org.openmuc.framework.security.SslManagerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttDriverConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(MqttDriverConnection.class);
    private final MqttDriverSettings mqttSettings;
    private final MqttConnection mqttConnection;
    private final MqttReader mqttReader;
    private final MqttWriter mqttWriter;
    private final Map<String, ParserService> parsers = new HashMap<>();
    private final Map<String, Long> lastLoggedRecords = new HashMap<>();
    private final List<ChannelRecordContainer> recordContainerList = new ArrayList<>();

    public MqttDriverConnection(String host, String settings) throws ArgumentSyntaxException {
        String pid = "mqttdriver";
        mqttSettings = new MqttDriverSettings(host, settings);
        mqttConnection = new MqttConnection(mqttSettings.getSettings());
        mqttReader = new MqttReader(mqttConnection, pid);
        mqttWriter = new MqttWriter(mqttConnection, pid);
        if (!mqttSettings.isSsl()) {
            mqttConnection.connect();
        }
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        List<String> topics = new ArrayList<>();
        for (ChannelRecordContainer container : containers) {
            MqttChannelAddress address;
			try {
				address = new MqttChannelAddress(container.getChannelAddress());
	            topics.add(address.getTopic());
				
			} catch (ArgumentSyntaxException e) {
        		logger.warn("Unable to parse topic \"{}\" in for channel: {}", 
        				container.getChannelAddress(), container.getChannel().getId());
			}
        }

        if (topics.isEmpty()) {
            return;
        }
        mqttReader.listen(topics, (topic, message) -> {
        	if (!topics.contains(topic)) {
        		logger.warn("Unable to find received topic \"{}\" in subscribed list", topic);
        		return;
        	}
        	for (ChannelRecordContainer container : containers) {
        		if (!Stream.of(container.getChannelAddress().split(";"))
                		.findFirst().orElse(ChannelConfig.ADDRESS_DEFAULT).equals(topic)) {
        			continue;
        		}
                Channel channel = container.getChannel();
                Record record = getRecord(message, container);
                
                if (recordIsOld(channel.getId(), record)) {
                	continue;
                }
                addMessageToContainerList(record, container);
        	}
            if (recordContainerList.size() >= mqttSettings.getRecordCollectionSize()) {
                notifyListenerAndPurgeList(listener);
            }
        });
    }

    private void notifyListenerAndPurgeList(RecordsReceivedListener listener) {
        logTraceNewRecord();
        listener.newRecords(recordContainerList);
        recordContainerList.clear();
    }

    private void addMessageToContainerList(Record record, ChannelRecordContainer container) {
        ChannelRecordContainer copiedContainer = container.copy();
        copiedContainer.setRecord(record);

        recordContainerList.add(copiedContainer);
    }

    private boolean recordIsOld(String channelId, Record record) {
        Long lastTimestamp = lastLoggedRecords.get(channelId);

        if (lastTimestamp == null) {
            lastLoggedRecords.put(channelId, record.getTimestamp());
            return false;
        }

        if (record.getTimestamp() == null || record.getTimestamp() <= lastTimestamp) {
            return true;
        }

        lastLoggedRecords.put(channelId, record.getTimestamp());
        return false;
    }

    private Record getRecord(byte[] message, ChannelRecordContainer container) {
        Record record;
        if (parsers.containsKey(mqttSettings.getParser())) {
            record = parsers.get(mqttSettings.getParser()).deserialize(message, container);
        }
        else if (container.getChannel().getScalingFactor() == 1.0) {
            record = new Record(new ByteArrayValue(message), System.currentTimeMillis());
        }
        else {
        	record = new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
        }
        return record;
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        for (ChannelValueContainer container : containers) {
            Record record = new Record(container.getValue(), System.currentTimeMillis());
            if (parsers.containsKey(mqttSettings.getParser())) {
                byte[] message;
                try {
                    message = parsers.get(mqttSettings.getParser()).serialize(record, container);
                } catch (SerializationException e) {
                    logger.error(e.getMessage());
                    continue;
                }
    			try {
                    MqttChannelAddress address = new MqttChannelAddress(container.getChannelAddress());
                    mqttWriter.write(address.getTopic(), message);
                    container.setFlag(Flag.VALID);
    				
    			} catch (ArgumentSyntaxException e) {
            		logger.warn("Unable to parse topic \"{}\" in for channel: {}", 
            				container.getChannelAddress(), container.getChannel().getId());
    			}
            }
            else {
                logger.error("A parser is needed to write messages and none have been registered.");
                throw new UnsupportedOperationException();
            }
        }
        return null;
    }

    @Override
    public void disconnect() {
        mqttWriter.shutdown();
        mqttConnection.disconnect();
    }

    public void setParser(String parserId, ParserService parser) {
        if (parser == null) {
            parsers.remove(parserId);
            return;
        }
        parsers.put(parserId, parser);
    }

    private void logTraceNewRecord() {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("new records");
            for (ChannelRecordContainer container : recordContainerList) {
                sb.append("\ntopic: " + sb.append(container.getChannelAddress()) + "\n");
                sb.append("record: " + container.getRecord().toString());
            }
            logger.trace(sb.toString());
        }
    }

    public void setSslManager(SslManagerInterface instance) {
        if (mqttConnection.getSettings().isSsl()) {
            logger.debug("SSLManager registered in driver");
            mqttConnection.setSslManager(instance);
            if (instance.isLoaded()) {
                mqttConnection.connect();
            }
        }
    }
}
