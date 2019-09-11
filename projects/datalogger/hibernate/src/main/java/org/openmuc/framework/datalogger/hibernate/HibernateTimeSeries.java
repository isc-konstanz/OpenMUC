package org.openmuc.framework.datalogger.hibernate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.BasicType;
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

public class HibernateTimeSeries {

	protected static final String VALUE_COLUMN = "value"; 
	protected static final String TIME_COLUMN = "timestamp"; 
	protected static final String FLAG_COLUMN = "code"; 
	
	protected static final String CONFIG_PATH = "hibernate.configPath";
	protected static final String DEFAULT_CONFIG_PATH = "conf/";
	protected static final String MAPPING_TEMPLATE_FILE = "hibernate.record.template";
	protected static final String DEFAULT_MAPPING_TEMPLATE = "hibernate.record.hbm.xml";

	protected static String MAPPING_TEMPLATE = null;

	protected final String id;
	protected final ValueType type;
	
	public HibernateTimeSeries(String id, ValueType type) {
		this.id = id;
		this.type = type;

		if (MAPPING_TEMPLATE == null) {
			loadMappingTemplate();
		}
	}
	
	protected void loadMappingTemplate() {
		String configPath = System.getProperty(CONFIG_PATH, DEFAULT_CONFIG_PATH);
		String mappingTemplateFile = System.getProperty(MAPPING_TEMPLATE_FILE, DEFAULT_MAPPING_TEMPLATE);
		String templateFileStr = configPath + mappingTemplateFile;
		try {
			MAPPING_TEMPLATE = new String(Files.readAllBytes(Paths.get(templateFileStr)));
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean containsUserType(String type) {
		return MAPPING_TEMPLATE.contains(type); 
	}

	public InputStream createMappingInputStream() {
		String mapping = MAPPING_TEMPLATE.replace("entity-name=\"entity\"", "entity-name=\""+id+"\"");
		switch (type) {
		case BOOLEAN:
			mapping = mapping.replace("java.lang.Object", "java.lang.Boolean");
			break;
		case BYTE:
			mapping = mapping.replace("java.lang.Object", "java.lang.Byte");
			break;
		case DOUBLE:
			mapping = mapping.replace("java.lang.Object", "java.lang.Double");
			break;
		case FLOAT:
			mapping = mapping.replace("java.lang.Object", "java.lang.Float");
			break;
		case INTEGER:
			mapping = mapping.replace("java.lang.Object", "java.lang.Integer");
			break;
		case LONG:
			mapping = mapping.replace("java.lang.Object", "java.lang.Long");
			break;
		case SHORT:
			mapping = mapping.replace("java.lang.Object", "java.lang.Short");
			break;
		case STRING:
			mapping = mapping.replace("java.lang.Object", "java.lang.String");
			break;
		default:
			mapping = mapping.replace("java.lang.Object", "java.lang.String");
			break;
		}
		return new ByteArrayInputStream(StandardCharsets.UTF_16.encode(mapping).array());		
		
	}
	
	protected Map<String, Object> buildMap(long timestamp, Value value, byte code) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(TIME_COLUMN, timestamp);
        map.put(FLAG_COLUMN, code);
        if (value == null) {
        	map.put(VALUE_COLUMN, null);
        	return map;
        }        
        switch (type) {
		case BOOLEAN:
			map.put(VALUE_COLUMN, value.asBoolean());
			break;
		case BYTE:
			map.put(VALUE_COLUMN, value.asByte());
			break;
		case DOUBLE:
			map.put(VALUE_COLUMN, value.asDouble());
			break;
		case FLOAT:
			map.put(VALUE_COLUMN, value.asFloat());
			break;
		case INTEGER:
			map.put(VALUE_COLUMN, value.asInt());
			break;
		case LONG:
			map.put(VALUE_COLUMN, value.asLong());
			break;
		case SHORT:
			map.put(VALUE_COLUMN, value.asShort());
			break;
		case STRING:
			map.put(VALUE_COLUMN, value.asString());
			break;
		default:
			map.put(VALUE_COLUMN, value.asString());
			break;
        }
		return map;
	}
	
    public void log(SessionFactory factory, Record record, long timestamp) {
    	//Get Record Values
    	long time = timestamp;
        if (record.getFlag().equals(Flag.VALID) ||
        	record.getTimestamp() != null) {
        	time = record.getTimestamp();
        }
        Value value = record.getValue();
        byte code = record.getFlag().getCode();

    	// Build Map
        Map<String, Object> map = buildMap(time, value, code);
        
        //Save Dynamic Map with Hibernate
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
        
    	session.save(id, map);
		
		t.commit();
		session.close();
     }
    
    protected Record createRecord(@SuppressWarnings("rawtypes") Map map) {
		Flag flag = null;
		Object fl = map.get(FLAG_COLUMN);
		if (fl != null) {
			flag = Flag.newFlag(Byte.hashCode((byte)fl));
		}
		Object val = map.get(VALUE_COLUMN);
		Value value = null;
		if (val instanceof Boolean) value = new BooleanValue((Boolean)val);
		else if (val instanceof Byte) value = new ByteValue((Byte)val);
		else if (val instanceof Double) value = new DoubleValue((Double)val);
		else if (val instanceof Float) value = new FloatValue((Float)val);
		else if (val instanceof Integer) value = new IntValue((Integer)val);
		else if (val instanceof Long) value = new LongValue((Long)val);
		else if (val instanceof Short) value = new ShortValue((Short)val);
		else if (val instanceof String) value = new StringValue((String)val);
		return new Record(value, (long)map.get(TIME_COLUMN), flag);
    }

	public List<Record> getRecords(SessionFactory factory, String channelId, BasicType userType, long startTime, long endTime) {
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
		Query<?> query;
		if (userType == null) {
			query = session.createQuery("from " + channelId + 
					" where timestamp <= " + startTime + " and timestamp >= " + endTime);
		}
		else {
			query = session.createQuery("from " + channelId + 
					" c where c.timestamp <= :start and c.timestamp >= :end");
			query.setParameter("start", startTime, userType)
				 .setParameter("end", endTime, userType);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		List<Record> records = new ArrayList<Record>(list.size());
		for (@SuppressWarnings("rawtypes") Map map: list) {
			Record rec = createRecord(map);
			records.add(rec);
		}
		
//        System.out.println(records);
        t.commit();
        session.close();
		return records;
	}
}
