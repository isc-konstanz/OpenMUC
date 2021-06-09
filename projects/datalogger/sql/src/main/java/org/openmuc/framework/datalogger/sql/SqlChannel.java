package org.openmuc.framework.datalogger.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.datalogger.LoggingChannel;
import org.openmuc.framework.datalogger.LoggingSyntax;
import org.openmuc.framework.datalogger.annotation.Configure;
import org.openmuc.framework.lib.osgi.config.PropertyHandler;
import org.openmuc.framework.lib.sql.IndexType;
import org.openmuc.framework.lib.sql.SqlData;
import org.openmuc.framework.lib.sql.SqlSettings;
import org.openmuc.framework.lib.sql.TableType;
import org.openmuc.framework.lib.sql.properties.PropertyHandlerProvider;
import org.openmuc.framework.lib.sql.properties.PropertySettings;
import org.openmuc.framework.lib.sql.properties.SqlProperties;


public class SqlChannel extends LoggingChannel implements SqlData, SqlSettings {

	private static final String VAL_COL_DEFAULT = "value";
	private static final String KEY_COL_DEFAULT = "channelid";

    static int TYPE_LENGTH_DEFAULT = 10;
    static String TYPE_INDEX_DEFAULT = "INT UNSIGNED";
    static String TYPE_DATA_DEFAULT = "FLOAT";
    static String TYPE_NOT_NULL = "NOT NULL";
    static String[] TYPES = new String[] {
        "FLOAT",
        "REAL",
        "BIGINT",
        "INT",
        "SMALLINT",
        "TINYINT",
        "BIT",
        "VARBINARY",
        "VARCHAR"
    };

    //@Option(mandatory = false)
    protected String url;

    @Option(mandatory = false)
    private String host = SqlProperties.HOST;

    @Option(mandatory = false)
    private Integer port = Integer.valueOf(SqlProperties.PORT);

    @Option(mandatory = false)
    private String database = SqlProperties.DATABASE;

    @Option(mandatory = false)
    protected String table = null;

    @Option(mandatory = false)
    protected TableType tableType = TableType.valueOf(SqlProperties.TABLE_TYPE.toUpperCase());;

    @Option(mandatory = false)
    protected String driver = SqlProperties.DRIVER;

    @Option(mandatory = false)
    protected String type = SqlProperties.TYPE;

    @Option(mandatory = false)
    protected String user = SqlProperties.USER;

    @Option(mandatory = false)
    protected String password = SqlProperties.PASSWORD;

    @Option(mandatory = false)
    protected int timeResolution = Integer.valueOf(SqlProperties.TIME_RES);

    @Option(mandatory = false)
    protected String timeFormat = SqlProperties.TIME_FORMAT;

    @Option(mandatory = false)
    protected IndexType indexType = IndexType.valueOf(SqlProperties.INDEX_TYPE.toUpperCase());

    @Option(mandatory = false)
    protected String indexColumn = SqlProperties.INDEX_COL;

    @Option(mandatory = false)
    protected String valueColumn = null;

    @Option(mandatory = false)
    protected String keyColumn = null;

    @Option(mandatory = false)
    protected String key = null;

    public void configure() throws ArgumentSyntaxException {
        PropertyHandler propertyHandler = PropertyHandlerProvider.getInstance().getPropertyHandler();
        
        url = null;
        host = propertyHandler.getString(PropertySettings.HOST);
        port = propertyHandler.getInt(PropertySettings.PORT);
        database = propertyHandler.getString(PropertySettings.DATABASE);
        
        table = propertyHandler.getString(PropertySettings.TABLE);
        tableType = TableType.valueOf(
        		propertyHandler.getString(PropertySettings.TABLE_TYPE).toUpperCase());;
        
        driver = propertyHandler.getString(PropertySettings.DRIVER);
        type = propertyHandler.getString(PropertySettings.TYPE);
        
        user = propertyHandler.getString(PropertySettings.USER);
        password = propertyHandler.getString(PropertySettings.PASSWORD);
        
        timeResolution = propertyHandler.getInt(PropertySettings.TIME_RES);
        timeFormat = propertyHandler.getString(PropertySettings.TIME_FORMAT);
        
        indexType = IndexType.valueOf(
        		propertyHandler.getString(PropertySettings.INDEX_TYPE).toUpperCase());
        indexColumn = propertyHandler.getString(PropertySettings.INDEX_COL);
        
        valueColumn = propertyHandler.getString(PropertySettings.DATA_COL);
        
        Settings settings = new Settings(getSettings(), getClass(), new LoggingSyntax());
        configure(settings);
    }

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
            
            settings = new Settings(String.join(",", list), getClass(), new LoggingSyntax());
            super.configure(settings);
        }
        
        if (database == null || database.isEmpty()) {
            throw new ArgumentSyntaxException("Database name needs to be configured");
        }
        if (url == null || url.isEmpty()) {
            url = type + "://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        }
        
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            throw new ArgumentSyntaxException("Database login credentials need to be configured");
        }
        
        if (table == null || table.isEmpty()) {
            table = SqlProperties.TABLE;
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
        if (valueColumn == null || valueColumn.isEmpty()) {
        	valueColumn = VAL_COL_DEFAULT;
        }
        if (keyColumn == null || keyColumn.isEmpty()) {
        	keyColumn = KEY_COL_DEFAULT;
        }
    }

    @Override
    public String getDatabaseUrl() {
        return url;
    }

    @Override
    public String getDatabaseName() {
        return database;
    }

    @Override
    public String getDatabaseDriver() {
        return driver;
    }

    @Override
    public String getDatabaseType() {
        return type;
    }

    @Override
    public String getDatabaseUser() {
        return user;
    }

    @Override
    public String getDatabasePassword() {
        return password;
    }

	@Override
	public String getTable() {
		return table;
	}

	@Override
	public TableType getTableType() {
		return tableType;
	}

    @Override
    public int getTimeResolution() {
        return timeResolution;
    }

    @Override
    public String getTimeFormat() {
        return timeFormat;
    }

    @Override
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public String getIndexColumn() {
        return indexColumn;
    }

    @Override
    public String getValueColumn() {
        return valueColumn;
    }

    @Override
    public String getKeyColumn() {
        return keyColumn;
    }

    public String getKey() {
    	return key;
    }

}
