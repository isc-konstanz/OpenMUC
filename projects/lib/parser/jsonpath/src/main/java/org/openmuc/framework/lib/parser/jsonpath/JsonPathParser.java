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

package org.openmuc.framework.lib.parser.jsonpath;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationContainer;
import org.openmuc.framework.parser.spi.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

/**
 * JsonPath parser library for the OpenMUC framework.
 */
public class JsonPathParser implements ParserService {
    private final Logger logger = LoggerFactory.getLogger(JsonPathParser.class);

    @Override
    public byte[] serialize(List<LoggingRecord> openMucRecords) throws SerializationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serialize(Record record, SerializationContainer container) throws SerializationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Record deserialize(byte[] byteArray, SerializationContainer container) {
        String path = Stream.of(container.getChannelAddress().split(";")).skip(1).collect(Collectors.joining(";"));
        return deserializeJson(new String(byteArray), path, container.getValueType());
    }

    protected Record deserializeJson(String jsonStr, String jsonPath, ValueType valueType) {
        Value value;
    	try {
	    	DocumentContext jsonContext = JsonPath.parse(jsonStr);

	        switch (valueType) {
	        case BOOLEAN:
	            value = new BooleanValue(jsonContext.read(jsonPath, boolean.class));
	            break;
	        case BYTE:
	            value = new ByteValue(jsonContext.read(jsonPath, byte.class));
	            break;
	        case SHORT:
	            value = new ShortValue(jsonContext.read(jsonPath, short.class));
	            break;
	        case INTEGER:
	            value = new IntValue(jsonContext.read(jsonPath, int.class));
	            break;
	        case LONG:
	            value = new LongValue(jsonContext.read(jsonPath, long.class));
	            break;
	        case FLOAT:
	            value = new FloatValue(jsonContext.read(jsonPath, float.class));
	            break;
	        case DOUBLE:
	            value = new DoubleValue(jsonContext.read(jsonPath, double.class));
	            break;
	        case STRING:
	            value = new StringValue(jsonContext.read(jsonPath, String.class));
	            break;
	        default:
	            logger.warn("Unsupported ValueType: {}", valueType);
	            return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
	        }
    	} catch (JsonPathException e) {
            logger.warn("Error decoding JSON string: {}", e.getMessage());
            return new Record(Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
    	}
        return new Record(value, System.currentTimeMillis());
    }

}
