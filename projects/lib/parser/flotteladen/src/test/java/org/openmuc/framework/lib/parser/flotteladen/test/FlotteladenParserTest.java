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

package org.openmuc.framework.lib.parser.flotteladen.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.lib.parser.flotteladen.FlotteladenParser;
import org.openmuc.framework.parser.spi.SerializationContainer;
import org.openmuc.framework.parser.spi.SerializationException;


public class FlotteladenParserTest {

	private final FlotteladenParser parser = new FlotteladenParser();

	private static byte[] POWER_JSON;
	private static byte[] STATUS_JSON;
	private static byte[] SETCURRENT_JSON;

	@BeforeAll
	public static void loadJson() throws IOException {
		POWER_JSON = FlotteladenParserTest.class.getResourceAsStream("power.json").readAllBytes();
		STATUS_JSON = FlotteladenParserTest.class.getResourceAsStream("status.json").readAllBytes();
		SETCURRENT_JSON = FlotteladenParserTest.class.getResourceAsStream("setcurrent.json").readAllBytes();
	}

	@Test
	public void testJsonPathFailure() {
		assertEquals(parsePower("$['units']['C1']", ValueType.DOUBLE).getFlag(), Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
		assertEquals(parsePower("$['source']", ValueType.DOUBLE).getFlag(), Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
	}

	@Test
	public void testPower() {
		assertEquals(parsePower("$['units']['P1']", ValueType.DOUBLE).getValue().asDouble(), -23.445396);
    }

	@Test
	public void testPowerAlias() {
		assertEquals(parsePower("P1", ValueType.DOUBLE).getValue().asDouble(), -23.445396);
    }

	private Record parsePower(String jsonPath, ValueType valueType) {
		return deserialize(POWER_JSON, String.format("modbus-bridge/powermeter1/power;%s", jsonPath), valueType);
	}

	@Test
	public void testStatus() {
		assertEquals(6000,        deserializeStatus(13, "$['minChargingCurrent']['value']", ValueType.INTEGER).getValue().asInt());
		assertEquals(16000,       deserializeStatus(13, "$['maxChargingCurrent']['value']", ValueType.INTEGER).getValue().asInt());
		assertEquals(6000,        deserializeStatus(13, "$['chargingCurrent']['value']", ValueType.INTEGER).getValue().asInt());
		assertEquals("OPTIMIZED", deserializeStatus(13, "$['chargingMode']['value']", ValueType.STRING).getValue().asString());
		assertEquals(104640.47,   deserializeStatus(13, "$['chargingMode']['requiredChargingQuantity']['value']", ValueType.DOUBLE).getValue().asDouble());
    }

	private Record deserializeStatus(int serialNumber, String jsonPath, ValueType valueType) {
		return deserialize(STATUS_JSON, String.format("lms/chargingpoints/%d/status;%s", serialNumber, jsonPath), valueType);
	}

	private Record deserialize(byte[] jsonBytes, String jsonPath, ValueType valueType) {
		return deserialize(jsonBytes, new FlotteladenParserContainer(jsonPath, valueType));
	}

	private Record deserialize(byte[] jsonBytes, SerializationContainer container) {
		return parser.deserialize(jsonBytes, container);
	}

	@Test
	public void testSetcurrent() throws SerializationException {
		assertArrayEquals(SETCURRENT_JSON, serializeSetcurrent(13, "$['current']['value']", new IntValue(10000), ValueType.INTEGER));
    }

	private byte[] serializeSetcurrent(int serialNumber, String jsonPath, Value value, ValueType valueType) throws SerializationException {
		return serialize(String.format("seal/chargingpoints/%d/setcurrent;%s", serialNumber, jsonPath), value, valueType);
	}

	private byte[] serialize(String jsonPath, Value value, ValueType valueType) throws SerializationException {
		return serialize(new Record(value, System.currentTimeMillis()), new FlotteladenParserContainer(jsonPath, valueType));
	}

	private byte[] serialize(Record record, SerializationContainer container) throws SerializationException {
		return parser.serialize(record, container);
	}

}
