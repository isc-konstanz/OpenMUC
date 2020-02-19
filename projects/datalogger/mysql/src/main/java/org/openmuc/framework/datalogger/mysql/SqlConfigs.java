package org.openmuc.framework.datalogger.mysql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.datalogger.Channel;
import org.openmuc.framework.options.Setting;
import org.openmuc.framework.options.SettingsSyntax;
import org.osgi.service.component.annotations.Component;

@Component
public class SqlConfigs extends Channel {

    protected String url;

    @Setting(mandatory = false)
    private String host = SqlLogger.DB_HOST;

    @Setting(mandatory = false)
    private Integer port = Integer.valueOf(SqlLogger.DB_PORT);

    @Setting(mandatory = false)
    private String database = SqlLogger.DB_NAME;

    @Setting(mandatory = false)
    protected String user = SqlLogger.DB_USER;

    @Setting(mandatory = false)
    protected String password = SqlLogger.DB_PASSWORD;

    @Setting(mandatory = false)
    protected String table = SqlLogger.TABLE;

    @Setting(mandatory = false)
    protected TimeType timeType = TimeType.valueOf(SqlLogger.TIME_TYPE.toUpperCase());

    @Setting(mandatory = false)
    protected Double timeScale;

    @Setting(mandatory = false)
    protected String timeFormat = SqlLogger.TIME_FORMAT;

    public String getDatabase() {
        return url;
    }

    public String getDatabaseName() {
        return database;
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

    public TimeType getTimeType() {
        return timeType;
    }

    public Double getTimeScale() {
        return timeScale = Double.valueOf(SqlLogger.TIME_SCALE);
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    @Override
    protected void doConfigure(String settings) throws ArgumentSyntaxException {
        if (settings != null && settings.equals("*")) {
            if (!getDriverId().equals("mysql")) {
                throw new ArgumentSyntaxException("Unable to copy logging settings if not mysql driver");
            }
            List<String> list = new ArrayList<String>(4);
            list.add(getDeviceAddress());
            list.add(getDeviceSettings());
            list.add(getAddress());
            list.add(getSettings());
            list.removeAll(Arrays.asList("", null));
            
            // TODO: verify if a more generic way to use a separator is necessary
            settings = String.join(SettingsSyntax.SEPARATOR_DEFAULT, list);
        }
        super.doConfigure(settings);
    }

    @Override
    protected void onConfigure() throws ArgumentSyntaxException {
        super.onConfigure();
        if (database == null || database.isEmpty()) {
            throw new ArgumentSyntaxException("Database name needs to be configured");
        }
        url = SqlLogger.DB_TYPE + "://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            throw new ArgumentSyntaxException("Database login credentials need to be configured");
        }
        
        if (table == null || table.isEmpty()) {
            table = getId().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        } else {
            String valid = table.replaceAll("[^a-zA-Z0-9]", "_");
            if (!table.equals(valid)) {
                throw new ArgumentSyntaxException(
                        "Table name invalid. Only alphanumeric letters separated by underscore are allowed: " + valid);
            }
        }
    }

}
