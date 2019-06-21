package org.openmuc.framework.datalogger.hibernate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
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
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class HibernateDataLogger  implements DataLoggerService {
	
	private static final Logger logger = LoggerFactory.getLogger(HibernateDataLogger.class);
	
	private static final String VALUE_COLUMN = "value"; 
	private static final String TIME_COLUMN = "timestamp"; 
	private static final String FLAG_COLUMN = "flag"; 
	private static final String DEFAULT_CONFIG_PATH = "conf/";
	private static final String DEFAULT_HIBERNATE_CONFIG = "hibernate.cfg.xml";
	private static final String DEFAULT_MAPPING_TEMPLATE = "DynamicMapping.hbm.xml";
	private static final String DEFAULT_MAPPING_SUFFIX = "Mapping.hbm.xml";

	private final File hibernatePropsFile;
	private SessionFactory factory;
	
	public HibernateDataLogger() {
		String hibernatePropsFilePath = DEFAULT_CONFIG_PATH + DEFAULT_HIBERNATE_CONFIG;
		hibernatePropsFile = new File(hibernatePropsFilePath);
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void setChannelsToLog(List<LogChannel> logChannels) {
        logger.trace("channels to log:");
        Configuration config = new Configuration().configure(hibernatePropsFile);
		for (LogChannel logChannel : logChannels) {

            if (logger.isTraceEnabled()) {
                logger.trace("channel.getId() " + logChannel.getId());
                logger.trace("channel.getLoggingInterval() " + logChannel.getLoggingInterval());
            }
            	File mapFile = new File(DEFAULT_CONFIG_PATH+logChannel.getId()+DEFAULT_MAPPING_SUFFIX);
            	if (!mapFile.exists()) {
            		try {
						mapFile = createMappingFile(logChannel.getId(), logChannel.getValueType());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
            	}
	    		config.addFile(mapFile);
        }
		factory = config.buildSessionFactory();
	}
	
	private File createMappingFile(String id, ValueType type) throws IOException {
		String templatefileStr = DEFAULT_CONFIG_PATH + DEFAULT_MAPPING_TEMPLATE;
		String tmp = new String(Files.readAllBytes(Paths.get(templatefileStr)));
		tmp = tmp.replace("entity-name=\"entity\"", "entity-name=\""+id+"\"");
		switch (type) {
		case BOOLEAN:
			tmp = tmp.replace("java.lang.Object", "java.lang.Boolean");
			break;
		case BYTE:
			tmp = tmp.replace("java.lang.Object", "java.lang.Byte");
			break;
		case DOUBLE:
			tmp = tmp.replace("java.lang.Object", "java.lang.Double");
			break;
		case FLOAT:
			tmp = tmp.replace("java.lang.Object", "java.lang.Float");
			break;
		case INTEGER:
			tmp = tmp.replace("java.lang.Object", "java.lang.Integer");
			break;
		case LONG:
			tmp = tmp.replace("java.lang.Object", "java.lang.Long");
			break;
		case SHORT:
			tmp = tmp.replace("java.lang.Object", "java.lang.Short");
			break;
		case STRING:
			tmp = tmp.replace("java.lang.Object", "java.lang.String");
			break;
		default:
			tmp = tmp.replace("java.lang.Object", "java.lang.String");
			break;
		}
		String mappingFileStr = DEFAULT_CONFIG_PATH + id + DEFAULT_MAPPING_SUFFIX;
		FileOutputStream outputStream = new FileOutputStream(mappingFileStr);
		outputStream.write(tmp.getBytes());
		outputStream.close();
		return new File(mappingFileStr);
		
	}

	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();

		for (LogRecordContainer container : containers) {
	        Record record = container.getRecord();
	        
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put(TIME_COLUMN, record.getTimestamp());
	        Value val = record.getValue();
	        if (val instanceof BooleanValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asBoolean());
	        if (val instanceof ByteValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asByte());
	        if (val instanceof DoubleValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asDouble());
	        if (val instanceof FloatValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asFloat());
	        if (val instanceof IntValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asInt());
	        if (val instanceof LongValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asLong());
	        if (val instanceof ShortValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asShort());
	        if (val instanceof StringValue) 
	        	map.put(VALUE_COLUMN, record.getValue().asString());
	        map.put(FLAG_COLUMN, record.getFlag().getCode());
	        
        	session.save(container.getChannelId(), map);
        }
		
		t.commit();
		session.close();
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
		
		List<Record> records = null;
		
		Session session = factory.openSession();

		Transaction t = session.beginTransaction();
		
		Query<?> query = session.createQuery("from " + channelId + 
				" where timestamp <= " + startTime + " and timestamp >= " + endTime);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		records = new ArrayList<Record>(list.size());
		for (@SuppressWarnings("rawtypes") Map map: list) {
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
			records.add(new Record(value, (long)map.get(TIME_COLUMN), flag));
		}
		
//        System.out.println(records);
        t.commit();
        session.close();
		return records;
	}
}
