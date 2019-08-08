package org.openmuc.framework.datalogger.hibernate;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.type.BasicType;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.core.datamanager.LogRecordContainerImpl;
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
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;

class HibernateTest {

	HibernateDataLogger log = new HibernateDataLogger();
	
	@Test
	void testInsertBoolean() {
		String tableName = "testBoolean";
		boolean val = true;
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.BOOLEAN));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		BooleanValue booleanVal = new BooleanValue(val);
		Record record = new Record(booleanVal, time, Flag.CHANNEL_DELETED);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asBoolean(), new Boolean(val));
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.CHANNEL_DELETED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertByte() {
		String tableName = "testByte";
		byte val = 10;
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.BYTE));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		ByteValue byteVal = new ByteValue(val);
		Record record = new Record(byteVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asByte(), val);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertDouble() {
		String tableName = "testDouble";
		double val = 125.6;
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.DOUBLE));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		DoubleValue doubleVal = new DoubleValue(val);
		Record record = new Record(doubleVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asDouble(), val, new Double(0));
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertFloat() {
		String tableName = "testFloat";
		float val = new Float(125.7);
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.FLOAT));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		FloatValue floatVal = new FloatValue(val);
		Record record = new Record(floatVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		List<LogChannel> logChannels2 = new ArrayList<LogChannel>();
		logChannels2.add(new LogChannelTestImpl(tableName, ValueType.BOOLEAN));
		log.setChannelsToLog(logChannels2);
		Long time2 = System.currentTimeMillis();
		List<LogRecordContainer> containers2 = new ArrayList<LogRecordContainer>();
		BooleanValue boolVal = new BooleanValue(true);
		Record record2 = new Record(boolVal, time2, Flag.VALID);
		LogRecordContainer container2 = new LogRecordContainerImpl(tableName, record2);
		containers2.add(container2);
		log.log(containers2, time2);

		log.setChannelsToLog(logChannels);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asFloat(), new Float(val), new Float(0));
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertInteger() {
		String tableName = "testInteger";
		int val = 333;
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.INTEGER));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		IntValue intVal = new IntValue(val);
		Record record = new Record(intVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asInt(), val);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkTimestamp(Long timestamp, Long time) {
		BasicType userType = log.getUserType();
		if (userType instanceof  LongIntegerType) {
			Integer i = ((LongIntegerType)userType).getJavaTypeDescriptor().unwrap(time, Integer.class, null);
			time = ((LongIntegerType)userType).getJavaTypeDescriptor().wrap(i, null);
		}
		assertEquals(timestamp, time);
		
	}

	@Test
	void testInsertLong() {
		String tableName = "testLong";
		long val = 666666;
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.LONG));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		LongValue longVal = new LongValue(val);
		Record record = new Record(longVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asLong(), val);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertShort() {
		String tableName = "testShort";
		short val = 7;
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.SHORT));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		ShortValue shortVal = new ShortValue(val);
		Record record = new Record(shortVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asShort(), val);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertString() {
		String tableName = "testString";
		String val = "Hello";
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		logChannels.add(new LogChannelTestImpl(tableName, ValueType.STRING));
		log.setChannelsToLog(logChannels);
		
		Long time = System.currentTimeMillis();
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		StringValue stringVal = new StringValue(val);
		Record record = new Record(stringVal, time, Flag.VALID);
		LogRecordContainer container = new LogRecordContainerImpl(tableName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(tableName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asString(), val);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testInsertMultiRecords() {
		List<LogChannel> logChannels = new ArrayList<LogChannel>();
		String booleanName = "testBoolean";
		logChannels.add(new LogChannelTestImpl(booleanName, ValueType.BOOLEAN));
		String byteName = "testByte";
		logChannels.add(new LogChannelTestImpl(byteName, ValueType.BYTE));
		String doubleName = "testDouble";
		logChannels.add(new LogChannelTestImpl(doubleName, ValueType.DOUBLE));
		String floatName = "testFloat";
		logChannels.add(new LogChannelTestImpl(floatName, ValueType.FLOAT));
		String integerName = "testInteger";
		logChannels.add(new LogChannelTestImpl(integerName, ValueType.INTEGER));
		String longName = "testLong";
		logChannels.add(new LogChannelTestImpl(longName, ValueType.LONG));
		String shortName = "testShort";
		logChannels.add(new LogChannelTestImpl(shortName, ValueType.SHORT));
		String stringName = "testString";
		logChannels.add(new LogChannelTestImpl(stringName, ValueType.STRING));
		log.setChannelsToLog(logChannels);
		
		List<LogRecordContainer> containers = new ArrayList<LogRecordContainer>();
		Long time = System.currentTimeMillis();

		boolean boolVal = false;
		BooleanValue booleanVal = new BooleanValue(boolVal);
		Record record = new Record(booleanVal, time, Flag.CHANNEL_DELETED);
		LogRecordContainer container = new LogRecordContainerImpl(booleanName, record);
		containers.add(container);
		
		byte bytVal = 11;
		ByteValue byteVal = new ByteValue(bytVal);
		record = new Record(byteVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(byteName, record);
		containers.add(container);
		
		double dVal = 125.7;
		DoubleValue doubleVal = new DoubleValue(dVal);
		record = new Record(doubleVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(doubleName, record);
		containers.add(container);
		
		float fVal = new Float(125.8);
		FloatValue floatVal = new FloatValue(fVal);
		record = new Record(floatVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(floatName, record);
		containers.add(container);

		int iVal = 334;
		IntValue intVal = new IntValue(iVal);
		record = new Record(intVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(integerName, record);
		containers.add(container);

		long lVal = 666667;
		LongValue longVal = new LongValue(lVal);
		record = new Record(longVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(longName, record);
		containers.add(container);

		short sVal = 8;
		ShortValue shortVal = new ShortValue(sVal);
		record = new Record(shortVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(shortName, record);
		containers.add(container);

		String strVal = "Bye";
		StringValue stringVal = new StringValue(strVal);
		record = new Record(stringVal, time, Flag.VALID);
		container = new LogRecordContainerImpl(stringName, record);
		containers.add(container);
		log.log(containers, time);

		try {
			List<Record> recs = log.getRecords(booleanName, time, time - 5);
			Record rec = recs.get(0);
			assertEquals(rec.getValue().asBoolean(), new Boolean(boolVal));
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.CHANNEL_DELETED);

			recs = log.getRecords(byteName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asByte(), bytVal);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);

			recs = log.getRecords(doubleName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asDouble(), dVal, new Double(0));
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);

			recs = log.getRecords(floatName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asFloat(), new Float(fVal), new Float(0));
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);

			recs = log.getRecords(integerName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asInt(), iVal);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);

			recs = log.getRecords(longName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asLong(), lVal);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);

			recs = log.getRecords(shortName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asShort(), sVal);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);

			recs = log.getRecords(stringName, time, time - 5);
			rec = recs.get(0);
			assertEquals(rec.getValue().asString(), strVal);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getFlag(), Flag.VALID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
