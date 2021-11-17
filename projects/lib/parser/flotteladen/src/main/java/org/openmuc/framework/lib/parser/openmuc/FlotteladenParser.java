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

package org.openmuc.framework.lib.parser.openmuc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Parser implementation for OpenMUC to Flotteladen communication.
 */
public class FlotteladenParser implements ParserService {
    private final Logger logger = LoggerFactory.getLogger(FlotteladenParser.class);

    private final Gson gson;

    public FlotteladenParser() {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeSpecialFloatingPointValues();
        gson = gsonBuilder.create();
    }

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
        
        //{"source":"mainPowerMeter","register":"power","units":{"P1":-1.336044,"P2":-0.232656,"P3":-1.4713321}}
        JsonObject json = gson.fromJson(new String(byteArray), JsonObject.class);
        JsonObject units = json.getAsJsonObject("units");
        String unit = Stream.of(container.getChannelAddress().split(";")).skip(1).collect(Collectors.joining(";"));
        
        logger.info("Received {1} units from register \"{2}\" source \"{3}\": {0}", units, units.size(),
                json.get("register"), json.get("source"));
        
        Value value = deserializeValue(json.getAsJsonObject(unit), container.getValueType());
        return new Record(value, System.currentTimeMillis());
    }

    private Value deserializeValue(JsonObject json, ValueType valueType) {
        switch (valueType) {
        case BOOLEAN:
            return new BooleanValue(json.getAsBoolean());
        case BYTE:
            return new ByteValue(json.getAsByte());
        case DOUBLE:
            return new DoubleValue(json.getAsDouble());
        case FLOAT:
            return new FloatValue(json.getAsFloat());
        case INTEGER:
            return new IntValue(json.getAsInt());
        case LONG:
            return new LongValue(json.getAsLong());
        case SHORT:
            return new ShortValue(json.getAsShort());
        case STRING:
            return new StringValue(json.getAsString());
        default:
            logger.warn("Unsupported ValueType: {}", valueType);
            return null;
        }
    }

}
