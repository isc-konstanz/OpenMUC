package org.openmuc.framework.datalogger.mysql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MysqlDataLogger implements DataLoggerService {

	private static final Logger logger = LoggerFactory.getLogger(MysqlDataLogger.class);

	private static String DEFAULT_DB_ADDRESS  = "";
	
    private final Map<String, MysqlChannelHandler> mysqlChannelHandlerMap = new HashMap<>();
	   
    public MysqlDataLogger() {
    }
    
    protected void activate(ComponentContext context) {

        logger.info("Activating MySQL Logger");
    }

    protected void deactivate(ComponentContext context) {

        logger.info("Deactivating MySQL Logger");
    }

	@Override
	public String getId() {
		return "mysql";
	}

    /**
     * Will called if OpenMUC starts the logger
     */
	@Override
	public void setChannelsToLog(List<LogChannel> logChannels) {

		mysqlChannelHandlerMap.clear();
        logger.trace("channels to log:");
        for (LogChannel logChannel : logChannels) {

            if (logger.isTraceEnabled()) {
                logger.trace("channel.getId() " + logChannel.getId());
                logger.trace("channel.getLoggingInterval() " + logChannel.getLoggingInterval());
            }
            createMysqlChannelHandler(logChannel);
        }
	}

	private void createMysqlChannelHandler(LogChannel logChannel) {
		String dbAddress = System.getProperty(MysqlDataLogger.class.getPackage().getName().toLowerCase() + ".dbAddress", 
				DEFAULT_DB_ADDRESS);
		String userName = System.getProperty(MysqlDataLogger.class.getPackage().getName().toLowerCase() + ".userName", 
				null);
		String password = System.getProperty(MysqlDataLogger.class.getPackage().getName().toLowerCase() + ".password", 
				null);
		MysqlChannelHandler mysqlChannelHandler = new MysqlChannelHandler(dbAddress, userName, password, logChannel);
		mysqlChannelHandlerMap.put(logChannel.getId(), mysqlChannelHandler);
	}

	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
        for (LogRecordContainer container : containers) {
            try {
            	MysqlChannelHandler mysqlChannelHandler = mysqlChannelHandlerMap.get(container.getChannelId());
            	if (mysqlChannelHandler == null) {
        			throw new RuntimeException("unsupported channel: " + container.getChannelId());
            	}
				mysqlChannelHandler.log(container);
//				testGetRecords(container);
			} catch (SQLException e) {
				logger.debug("ChannelID (" + container.getChannelId() + ")  SqlException occured: " + e.getMessage());
			}
        }
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
    	MysqlChannelHandler mysqlChannelHandler = mysqlChannelHandlerMap.get(channelId);
    	if (mysqlChannelHandler == null) {
			throw new RuntimeException("unsupported channel: " + channelId);
    	}
    	try {
			List<Record> recs =  mysqlChannelHandler.getRecords(channelId, startTime, endTime);
			if (recs == null) {
				throw new IOException("ChannelID (" + channelId + ") not available. It's not a logging Channel.");				
			}
			return recs;
		} catch (SQLException e) {
			logger.debug("ChannelID (" + channelId + ")  SqlException occured: " + e.getMessage());
			throw new IOException("ChannelID (" + channelId + ") SqlException occured: " + e.getMessage());	
		}
	}
	
	private void testGetRecords(LogRecordContainer container) {
		Long now = System.currentTimeMillis();
		try {
			List<Record> recordList = getRecords(container.getChannelId(), now-20000, now);
			for (Record r : recordList) {
				System.out.println(container.getChannelId() + ": " + r.getTimestamp() + ", " + r.getValue().toString() +
						", " + r.getFlag().toString());
			}
		} catch (IOException e) {
			logger.debug("ChannelID (" + container.getChannelId() + ")  IOException occured: " + e.getMessage());
		}
	}

}
