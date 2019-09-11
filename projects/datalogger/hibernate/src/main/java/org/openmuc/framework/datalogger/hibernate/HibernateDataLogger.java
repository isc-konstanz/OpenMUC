package org.openmuc.framework.datalogger.hibernate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.BasicType;
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
	
	public static final String SCALE_INTEGER_TYPE = "ScaleInteger";
	
	private static final String CONFIG_PATH = "hibernate.configPath";
	private static final String DEFAULT_CONFIG_PATH = "conf/";
	private static final String HIBERNATE_CONFIG = "hibernate.config.file";
	private static final String DEFAULT_HIBERNATE_CONFIG = "hibernate.cfg.xml";

	protected final File hibernatePropsFile;
	protected SessionFactory factory;
	protected Map<String, HibernateTimeSeries> idTimeSeriesMap;
	protected BasicType userType;
	protected boolean isInitialized = false;
	
	public HibernateDataLogger() {
		String configPath = System.getProperty(CONFIG_PATH, DEFAULT_CONFIG_PATH);
		String hibernateConfigFile = System.getProperty(HIBERNATE_CONFIG, DEFAULT_HIBERNATE_CONFIG);
		String hibernatePropsFilePath = configPath + hibernateConfigFile;
		hibernatePropsFile = new File(hibernatePropsFilePath);
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void setChannelsToLog(List<LogChannel> logChannels) {
        logger.trace("channels to log:");
		Thread initThread = new Thread() {
			@Override
			public void run() {
				try {
			        idTimeSeriesMap = new HashMap<String, HibernateTimeSeries>();
			        Configuration config = new Configuration().configure(hibernatePropsFile);
					for (LogChannel logChannel : logChannels) {
			
			            if (logger.isTraceEnabled()) {
			                logger.trace("channel.getId() " + logChannel.getId());
			                logger.trace("channel.getLoggingInterval() " + logChannel.getLoggingInterval());
			            }
			            
				        HibernateTimeSeries ts = new HibernateTimeSeries(logChannel.getId(), logChannel.getValueType());
				        if (ts.containsUserType(SCALE_INTEGER_TYPE)) {
				        	userType = ScaleIntegerType.INSTANCE;
				        }
			        	InputStream inputStream = ts.createMappingInputStream();
			        	idTimeSeriesMap.put(logChannel.getId(), ts);
			    		config.addInputStream(inputStream);
			        }
					if (factory != null) {
						factory.close();
					}
					
					if (userType !=  null) {
						config.registerTypeContributor( (typeContributions, serviceRegistry) -> {
								typeContributions.contributeType(userType, SCALE_INTEGER_TYPE);
						} );
					}
			
					factory = config.buildSessionFactory();
					isInitialized = true;
					logger.info("Hibernate is initialized");
				} catch (Exception e) {
					logger.warn("Error while configuring channels:", e);
				}
			}
		};
		initThread.start();
	}
	
	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		
		for (LogRecordContainer container : containers) {
	        Record record = container.getRecord();
	        
	        HibernateTimeSeries ts = idTimeSeriesMap.get(container.getChannelId());
			if (isInitialized) {
				ts.log(factory, record, timestamp);
			}
        }
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
		
		 HibernateTimeSeries ts = idTimeSeriesMap.get(channelId);
		 List<Record> records = null;
		 if (isInitialized) {
			 records = ts.getRecords(factory, channelId, userType, startTime, endTime);
		 }
		
		return records;
	}
	
	public BasicType getUserType() {
		return userType;
	}
}
