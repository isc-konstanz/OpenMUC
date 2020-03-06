/*
 * Copyright 2011-18 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.datalogger.mysql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.DataLogger;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DataLoggerService.class)
public class SqlLogger extends DataLogger<SqlChannel> {
    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    private static final String PKG = SqlLogger.class.getPackage().getName().toLowerCase().replace(".datalogger", "");;

    static final String DB_TYPE = System.getProperty(PKG + ".type", "jdbc:mysql");
    static final String DB_DRIVER = System.getProperty(PKG + ".driver", "com.mysql.cj.jdbc.Driver");

    static final String DB_HOST = System.getProperty(PKG + ".host", "127.0.0.1");
    static final String DB_PORT = System.getProperty(PKG + ".port", "3306");
    static final String DB_NAME = System.getProperty(PKG + ".database", "openmuc");
    static final String DB_USER = System.getProperty(PKG + ".user", "root");
    static final String DB_PWD = System.getProperty(PKG + ".password", "");

    static final String TABLE = System.getProperty(PKG + ".table", null);

    static final String TIME_RES = System.getProperty(PKG + ".time.resolution", "1000");
    static final String TIME_FORMAT = System.getProperty(PKG + ".time.format", "yyyy-MM-dd HH:mm:ss");
    static final String DATA_COL = System.getProperty(PKG + ".data.column", "data");
    static final String INDEX_COL = System.getProperty(PKG + ".index.column", "time");
    static final String INDEX_TYPE = System.getProperty(PKG + ".index.type", "TIMESTAMP_UNIX");

    private final Map<String, SqlClient> clients = new HashMap<String, SqlClient>();

    @Override
    public String getId() {
        return "mysql";
    }

    @Override
    public void onDeactivate() {
        for (SqlClient client : clients.values()) {
            client.close();
        }
    }

    @Override
    protected void onConfigure(List<SqlChannel> channels) throws IOException {
        Map<String, SqlClientChannels> clients = new HashMap<String, SqlClientChannels>();
        List<Thread> threads = new ArrayList<Thread>();
        for (SqlChannel channel : channels) {
            SqlClientChannels client = clients.get(channel.getDatabase());
            if (client == null) {
                client = new SqlClientChannels();
                clients.put(channel.getDatabase(), client);
                
                threads.add(onConfigureClient(client, channel));
            }
            client.add(channel);
        }
        for (Thread t : threads) t.start();
    }

    protected Thread onConfigureClient(final SqlClientChannels channels, SqlConfigs configs) {
        // Instantiation needs to be in separate threads, as onConfigure() is called by the regularly
        // interrupted DataManager main thread
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    channels.create(configs);
                    clients.put(configs.getDatabase(), channels.client);
                    
                } catch (IOException e) {
                    logger.error("Error initializing SQL Data Logger: {}", e);
                }
            }
        });
    }

    @Override
    protected void onWrite(List<SqlChannel> channels, long timestamp) throws IOException {
        Map<String, SqlClientChannels> clients = new HashMap<String, SqlClientChannels>();
        for (SqlChannel channel : channels) {
            SqlClientChannels client = clients.get(channel.getDatabase());
            if (client == null) {
                client = new SqlClientChannels(this.clients.get(channel.getDatabase()));
                clients.put(channel.getDatabase(), client);
            }
            client.add(channel);
        }
        for (SqlClientChannels client : clients.values()) {
            client.write(timestamp);
        }
    }

    @Override
    protected List<Record> onRead(SqlChannel channel, long startTime, long endTime) throws IOException {
        return clients.get(channel.getDatabase()).read(channel, startTime, endTime);
    }

}
