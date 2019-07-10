package org.openmuc.framework.datalogger.hibernate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class HibernateDataLogger  implements DataLoggerService {
	
	private static final Logger logger = LoggerFactory.getLogger(HibernateDataLogger.class);
	
	private static final String DEFAULT_CONFIG_PATH = "conf/";
	private static final String DEFAULT_HIBERNATE_CONFIG = "hibernate.cfg.xml";

	private final File hibernatePropsFile;
	private SessionFactory factory;
	private Map<String, HibernateTimeSeries> idTimeSeriesMap = new HashMap<String, HibernateTimeSeries>();
	
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
            
	        HibernateTimeSeries ts = new HibernateTimeSeries(logChannel.getId(), logChannel.getValueType());
        	InputStream inputStream = ts.createMappingInputStream();
        	idTimeSeriesMap.put(logChannel.getId(), ts);
    		config.addInputStream(inputStream);
        }
		factory = config.buildSessionFactory();
	}
	
	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();

		for (LogRecordContainer container : containers) {
	        Record record = container.getRecord();
	        
	        HibernateTimeSeries ts = idTimeSeriesMap.get(container.getChannelId());
	        ts.setSeries(record, timestamp);
	        ts.log(session);
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
		 HibernateTimeSeries ts = idTimeSeriesMap.get(channelId);
		for (@SuppressWarnings("rawtypes") Map map: list) {
			Record rec = ts.createRecord(map);
			records.add(rec);
		}
		
//        System.out.println(records);
        t.commit();
        session.close();
		return records;
	}
}
