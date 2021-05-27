package org.openmuc.framework.datalogger.sql2;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.OptionSyntax;
import org.openmuc.framework.datalogger.LoggingChannel;
import org.openmuc.framework.datalogger.annotation.Configure;
import org.openmuc.framework.datalogger.sql2.time.TimestampIndex;
import org.openmuc.framework.datalogger.sql2.time.TimestampSplit;
import org.openmuc.framework.datalogger.sql2.time.TimestampUnix;

@OptionSyntax(separator = ";", assignment= "=", keyValuePairs = {ADDRESS, SETTING})
public class SqlConfigs extends LoggingChannel {

    protected String url;

    @Option(mandatory = false)
    private String host = SqlLogger.DB_HOST;

    @Option(mandatory = false)
    private Integer port = Integer.valueOf(SqlLogger.DB_PORT);

    @Option(mandatory = false)
    private String database = SqlLogger.DB_NAME;

    @Option(mandatory = false)
    protected String table = SqlLogger.TABLE;

    @Option(mandatory = false)
    protected boolean union = false;

    @Option(mandatory = false)
    protected String driver = SqlLogger.DB_DRIVER;

    @Option(mandatory = false)
    protected String type = SqlLogger.DB_TYPE;

    @Option(mandatory = false)
    protected String user = SqlLogger.DB_USER;

    @Option(mandatory = false)
    protected String password = SqlLogger.DB_PWD;

    @Option(mandatory = false)
    protected int timeResolution = Integer.valueOf(SqlLogger.TIME_RES);

    @Option(mandatory = false)
    protected String timeFormat = SqlLogger.TIME_FORMAT;

    @Option(mandatory = false)
    protected IndexType indexType = IndexType.valueOf(SqlLogger.INDEX_TYPE.toUpperCase());

    @Option(mandatory = false)
    protected String indexColumn = SqlLogger.INDEX_COL;

    protected Index index;

    @Configure
    public void configure(Settings settings) throws ArgumentSyntaxException {
        if (settings != null && settings.toString().equals("*")) {
        	ChannelConfig channel = (ChannelConfig) getChannel();
            if (!channel.getDevice().getDriver().getId().equals("mysql")) {
                throw new ArgumentSyntaxException("Unable to copy logging settings if not mysql driver");
            }
            List<String> list = new ArrayList<String>(4);
            list.add(channel.getDevice().getAddress());
            list.add(channel.getDevice().getSettings());
            list.add(getAddress());
            list.add(getSettings());
            list.removeAll(Arrays.asList("", null));
            
            settings = Configurations.parseSettings(String.join(";", list), getClass());

            // TODO: verify if a more generic way to use a separator is necessary
            super.configure(settings);
        }
        
        if (database == null || database.isEmpty()) {
            throw new ArgumentSyntaxException("Database name needs to be configured");
        }
        url = SqlLogger.DB_TYPE + "://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            throw new ArgumentSyntaxException("Database login credentials need to be configured");
        }
        
        if (table == null || table.isEmpty()) {
            table = getId().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        }
        else {
            String valid = table.replaceAll("[^a-zA-Z0-9]", "_");
            if (!table.equals(valid)) {
                throw new ArgumentSyntaxException(
                        "Table name invalid. Only alphanumeric letters separated by underscore are allowed: " + valid);
            }
        }
        
        switch(indexType) {
        case TIMESTAMP:
            index = new TimestampIndex(indexColumn, timeFormat);
            break;
        case TIMESTAMP_SPLIT:
            index = new TimestampSplit(indexColumn, timeFormat);
            break;
        default:
            index = new TimestampUnix(indexColumn, timeResolution);
            break;
        }
    }

    public String getDatabase() {
        return url;
    }

    public String getDatabaseName() {
        return database;
    }

    public String getDatabaseDriver() {
        return driver;
    }

    public String getDatabaseType() {
        return type;
    }

    public String getDatabaseUser() {
        return user;
    }

    public String getDatabasePassword() {
        return password;
    }

    public String getTable() {
        return table;
    }

    public boolean isUnion() {
        return union;
    }

    public int getTimeResolution() {
        return timeResolution;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public String getIndexColumn() {
        return indexColumn;
    }

    public Index getIndex() {
        return index;
    }

}
