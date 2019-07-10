package org.openmuc.framework.datalogger.hibernate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
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

	private static final String VALUE_COLUMN = "value"; 
	private static final String TIME_COLUMN = "timestamp"; 
	private static final String FLAG_COLUMN = "code"; 
	
	private static final String DEFAULT_CONFIG_PATH = "conf/";
	private static final String DEFAULT_MAPPING_TEMPLATE = "hibernate.record.hbm.xml";

	protected static String MAPPING_TEMPLATE = null;

	protected final String id;
	protected final ValueType type;
	
	private Long timestamp;
    private Byte code;
    private Value value;
    
	public HibernateTimeSeries(String id, ValueType type) {
		this.id = id;
		this.type = type;

		if (MAPPING_TEMPLATE == null) {
			loadMappingTemplate();
		}
	}
	
	protected void loadMappingTemplate() {
		String templateFileStr = DEFAULT_CONFIG_PATH + DEFAULT_MAPPING_TEMPLATE;
		try {
			MAPPING_TEMPLATE = new String(Files.readAllBytes(Paths.get(templateFileStr)));
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
    public void setSeries(Record record, long timestamp) {
        if (record.getFlag().equals(Flag.VALID) ||
        	record.getTimestamp() != null) {
        	this.timestamp = record.getTimestamp();
        }
        else {
        	this.timestamp = timestamp;	        	
        }
        value = record.getValue();
    	code = record.getFlag().getCode();
    }
    
    public void log(Session session) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(TIME_COLUMN, timestamp);
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
        map.put(FLAG_COLUMN, code);
        
    	session.save(id, map);
     }
    
    public Record createRecord(@SuppressWarnings("rawtypes") Map map) {
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
}
