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
package org.openmuc.framework.lib.parser.openmuc;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteArrayValue;
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
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Parser implementation for OpenMUC to OpenMUC communication e.g. for the AMQP driver.
 */
public class OpenmucParserServiceImpl implements ParserService {

    private final Logger logger = LoggerFactory.getLogger(OpenmucParserServiceImpl.class);

    private final Gson gson;
    private ValueType valueType;

    public OpenmucParserServiceImpl() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Record.class, new RecordInstanceCreator());
        gsonBuilder.registerTypeAdapter(Value.class, new ValueDeserializer());
        gsonBuilder.registerTypeAdapter(Record.class, new RecordAdapter());
        gson = gsonBuilder.create();
    }

    @Override
    public byte[] serialize(LogRecordContainer openMucRecord) {
        String serializedString = gson.toJson(openMucRecord.getRecord());

        return serializedString.getBytes();
    }

    @Override
    public byte[] serialize(List<LogRecordContainer> openMucRecords) throws SerializationException {
        throw new SerializationException("This parser cannot serialize multiple records at once");
    }

    @Override
    public Record deserialize(byte[] byteArray, ValueType valueType) {
        this.valueType = valueType;
        String inputJson = new String(byteArray);

        return gson.fromJson(inputJson, Record.class);

    }

    private class RecordInstanceCreator implements InstanceCreator<Record> {

        @Override
        public Record createInstance(Type type) {
            return new Record(Flag.DISABLED);
        }
    }

    private class RecordAdapter implements JsonSerializer<Record> {

        @Override
        public JsonElement serialize(Record record, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject obj = new JsonObject();
            obj.addProperty("timestamp", record.getTimestamp());
            obj.addProperty("flag", record.getFlag().toString());
            Value value = record.getValue();

            if (value instanceof DoubleValue) {
                obj.addProperty("value", record.getValue().asDouble());
            }
            else if (value instanceof StringValue) {
                obj.addProperty("value", record.getValue().asString());
            }
            else if (value instanceof ShortValue) {
                obj.addProperty("value", record.getValue().asShort());
            }
            else if (value instanceof LongValue) {
                obj.addProperty("value", record.getValue().asLong());
            }
            else if (value instanceof IntValue) {
                obj.addProperty("value", record.getValue().asInt());
            }
            else if (value instanceof FloatValue) {
                obj.addProperty("value", record.getValue().asFloat());
            }
            else if (value instanceof ByteValue) {
                obj.addProperty("value", record.getValue().asByte());
            }
            else if (value instanceof ByteArrayValue) {
                obj.addProperty("value", Base64.getEncoder().encodeToString(record.getValue().asByteArray()));
            }
            else if (value instanceof BooleanValue) {
                obj.addProperty("value", record.getValue().asBoolean());
            }

            return obj;
        }
    }

    private class ValueDeserializer implements JsonDeserializer<Value> {
        @Override
        public Value deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            switch (valueType) {
            case BOOLEAN:
                return new BooleanValue(json.getAsBoolean());
            case BYTE_ARRAY:
                return new ByteArrayValue(Base64.getDecoder().decode(json.getAsString()));
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

}
