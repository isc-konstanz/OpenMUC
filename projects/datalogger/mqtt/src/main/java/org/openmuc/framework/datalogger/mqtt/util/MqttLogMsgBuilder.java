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

package org.openmuc.framework.datalogger.mqtt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openmuc.framework.datalogger.mqtt.dto.MqttLogChannel;
import org.openmuc.framework.datalogger.mqtt.dto.MqttLogMsg;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttLogMsgBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MqttLogMsgBuilder.class);

    private final HashMap<String, MqttLogChannel> channelsToLog;
    private final ParserService parserService;

    public MqttLogMsgBuilder(HashMap<String, MqttLogChannel> channelsToLog, ParserService parserService) {
        this.channelsToLog = channelsToLog;
        this.parserService = parserService;
    }

    public List<MqttLogMsg> buildLogMsg(List<LoggingRecord> loggingRecordList, boolean isLogMultiple) {
        if (isLogMultiple) {
            return logMultiple(loggingRecordList);
        }
        else {
            return logSingle(loggingRecordList);
        }
    }

    private List<MqttLogMsg> logSingle(List<LoggingRecord> loggingRecords) {

        List<MqttLogMsg> logMessages = new ArrayList<>();

        for (LoggingRecord loggingRecord : loggingRecords) {
            try {
                String topic = channelsToLog.get(loggingRecord.getChannelId()).topic;
                byte[] message = parserService.serialize(loggingRecord);
                logMessages.add(new MqttLogMsg(loggingRecord.getChannelId(), message, topic));
            } catch (SerializationException e) {
                logger.error("failed to parse records {}", e.getMessage());
            }
        }

        return logMessages;
    }

    private List<MqttLogMsg> logMultiple(List<LoggingRecord> loggingRecords) {
        List<MqttLogMsg> logMessages = new ArrayList<>();

        Map<String, List<LoggingRecord>> loggingTopicRecords = new HashMap<String, List<LoggingRecord>>();
        for (LoggingRecord loggingRecord : loggingRecords) {
            String topic = channelsToLog.get(loggingRecord.getChannelId()).topic;
        	List<LoggingRecord> topicRecords = loggingTopicRecords.get(topic);
        	if (topicRecords == null) {
        		topicRecords = new ArrayList<LoggingRecord>();
        		loggingTopicRecords.put(topic, topicRecords);
        	}
        	topicRecords.add(loggingRecord);
        }
        for (String topic : loggingTopicRecords.keySet()) {
        	List<LoggingRecord> topicRecords = loggingTopicRecords.get(topic);
            try {
                byte[] message = parserService.serialize(topicRecords);
                String channelIds = topicRecords.stream()
                        .map(record -> record.getChannelId())
                        .collect(Collectors.toList())
                        .toString();
                logMessages.add(new MqttLogMsg(channelIds, message, topic));
                
            } catch (SerializationException e) {
                logger.error("failed to parse records {}", e.getMessage());
            }
        }
        return logMessages;
    }

}
