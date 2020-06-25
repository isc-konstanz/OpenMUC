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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.core.datamanager.LogRecordContainerImpl;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.parser.spi.ParserService;
import org.openmuc.framework.parser.spi.SerializationException;

/**
 * ToDo: add more tests for different datatypes
 */
class OpenmucParserServiceImplTest {

    private ParserService parserService;

    @BeforeEach
    private void setupService() {
        parserService = new OpenmucParserServiceImpl();
    }

    @Test
    void serializeDoubleValue() throws SerializationException {
        String controlString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":3.0}";
        Value doubleValue = new DoubleValue(3.0);
        long timestamp = 1582722316;
        Flag flag = Flag.VALID;
        Record record = new Record(doubleValue, timestamp, flag);

        byte[] serializedRecord = parserService.serialize(new LogRecordContainerImpl("test", record));
        String serializedJson = new String(serializedRecord);
        assertEquals(controlString, serializedJson);
    }

    @Test
    void serializeStringValue() throws SerializationException {
        String controlString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":\"test\"}";
        Value doubleValue = new StringValue("test");
        long timestamp = 1582722316;
        Flag flag = Flag.VALID;
        Record record = new Record(doubleValue, timestamp, flag);

        byte[] serializedRecord = parserService.serialize(new LogRecordContainerImpl("test", record));
        String serializedJson = new String(serializedRecord);
        assertEquals(controlString, serializedJson);
    }

    @Test
    void serializeByteArrayValue() throws SerializationException {
        String controlString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":\"dGVzdA==\"}";
        Value byteArrayValue = new ByteArrayValue("test".getBytes());
        long timestamp = 1582722316;
        Flag flag = Flag.VALID;
        Record record = new Record(byteArrayValue, timestamp, flag);

        byte[] serializedRecord = parserService.serialize(new LogRecordContainerImpl("test", record));
        String serializedJson = new String(serializedRecord);
        assertEquals(controlString, serializedJson);
    }

    @Test
    void deserializeTestDoubleValue() {
        String inputString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":3.0}";

        Record recordDes = parserService.deserialize(inputString.getBytes(), ValueType.DOUBLE);
        assertEquals(3.0, recordDes.getValue().asDouble());
    }

    @Test
    void deserializeByteArrayValue() {
        String inputString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":\"dGVzdA==\"}";

        Record recordDes = parserService.deserialize(inputString.getBytes(), ValueType.BYTE_ARRAY);
        assertEquals("test".getBytes(), recordDes.getValue().asByteArray());
    }

    @Test
    void deserializeTimestamp() {
        String inputString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":3.0}";

        Record recordDes = parserService.deserialize(inputString.getBytes(), ValueType.DOUBLE);
        assertEquals(1582722316, recordDes.getTimestamp().longValue());
    }

    @Test
    void deserializeFlag() {
        String inputString = "{\"timestamp\":1582722316,\"flag\":\"VALID\",\"value\":3.0}";

        Record recordDes = parserService.deserialize(inputString.getBytes(), ValueType.DOUBLE);
        assertEquals("VALID", recordDes.getFlag().name());
    }
}
