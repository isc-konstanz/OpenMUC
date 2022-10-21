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

package org.openmuc.framework.lib.parser.flotteladen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.lib.parser.jsonpath.JsonPathParser;
import org.openmuc.framework.parser.spi.SerializationContainer;
import org.openmuc.framework.parser.spi.SerializationException;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

/**
 * Parser implementation for OpenMUC to Flotteladen communication.
 */
public class FlotteladenParser extends JsonPathParser {

    public static final HashMap<String, String> ALIAS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L; {
        put("P1", "$['units']['P1']");
        put("P2", "$['units']['P2']");
        put("P3", "$['units']['P3']");
    }};

    @Override
    public byte[] serialize(Record record, SerializationContainer container) throws SerializationException {
    	String channelTopic = Stream.of(container.getChannelAddress().split(";")).findFirst().orElse("");
    	String jsonTopic = new LinkedList<String>(List.of(channelTopic.split("/"))).getLast();
        String jsonPath = Stream.of(container.getChannelAddress().split(";")).skip(1).collect(Collectors.joining(";"));
        if (getClass().getResource(jsonTopic+".json") == null) {
        	throw new SerializationException("Serializing not yet implemented for topic: " + channelTopic);
        }
        int serialNumber = parseSerialNumber(channelTopic);
        return serializeJson(jsonTopic, jsonPath, serialNumber, record.getValue(), container.getValueType()).getBytes();
    }

    protected String serializeJson(String jsonTopic, String jsonPath, int serialNumber, Value value, ValueType valueType) 
    		throws SerializationException {
    	DocumentContext jsonContext = JsonPath.parse(getClass().getResourceAsStream(jsonTopic+".json"));
    	try {
	        jsonContext.set("$['serialNumber']", serialNumber);
	        switch (valueType) {
	        case BOOLEAN:
	            jsonContext.set(jsonPath, value.asBoolean());
	            break;
	        case BYTE:
	            jsonContext.set(jsonPath, value.asByte());
	            break;
	        case SHORT:
	            jsonContext.set(jsonPath, value.asShort());
	            break;
	        case INTEGER:
	            jsonContext.set(jsonPath, value.asInt());
	            break;
	        case LONG:
	            jsonContext.set(jsonPath, value.asLong());
	            break;
	        case FLOAT:
	            jsonContext.set(jsonPath, value.asFloat());
	            break;
	        case DOUBLE:
	            jsonContext.set(jsonPath, value.asDouble());
	            break;
	        case STRING:
	            jsonContext.set(jsonPath, value.asString());
	            break;
	        default:
	            throw new SerializationException("Unsupported ValueType: " + valueType);
	        }
    	} catch (JsonPathException e) {
    		throw new SerializationException("Unable to serialize value: " + e.getMessage());
    	}
        return jsonContext.jsonString();
    }

    private int parseSerialNumber(String channelTopic) throws SerializationException {
    	LinkedList<String> channelTopics = new LinkedList<String>(
    			List.of(channelTopic.split("/"))
		);
    	channelTopics.removeLast();
    	
    	String serialNumber = channelTopics.getLast();
        if (serialNumber == null || serialNumber.isBlank()) {
        	throw new SerializationException("Unable to extract serialNumber from topic: " + channelTopic);
        }
        try {
        	return Integer.parseInt(serialNumber);

        } catch (NumberFormatException e) {
        	throw new SerializationException("Unable to extract serialNumber: " + e.getMessage());
        }
    }

    @Override
    public synchronized Record deserialize(byte[] jsonBytes, SerializationContainer container) {
        String jsonPath = Stream.of(container.getChannelAddress().split(";")).skip(1).collect(Collectors.joining(";"));
        for (String alias : ALIAS.keySet()) {
            if (jsonPath.equals(alias)) {
                jsonPath = jsonPath.replace(alias, ALIAS.get(alias));
            }
        }
        return deserializeJson(new String(jsonBytes), jsonPath, container.getValueType());
    }

}
