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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.DataLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlLogger extends DataLogger<SqlChannel> {
    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    private static final String PKG = SqlLogger.class.getPackage().getName().toLowerCase();

    static final String DB_TYPE = System.getProperty(PKG + ".type", "jdbc:mysql");
    static final String DB_DRIVER = System.getProperty(PKG + ".driver", "com.mysql.cj.jdbc.Driver");

    static final String DB_HOST = System.getProperty(PKG + ".host", "127.0.0.1");
    static final String DB_PORT = System.getProperty(PKG + ".port", "3306");
    static final String DB_NAME = System.getProperty(PKG + ".database", "openmuc");
    static final String DB_USER = System.getProperty(PKG + ".user", "root");
    static final String DB_PASSWORD = System.getProperty(PKG + ".password", "");

    static final String TABLE = System.getProperty(PKG + ".table", null);

    static final String TIME_TYPE = System.getProperty(PKG + "time.type", "TIMESTAMP_UNIX");
    static final String TIME_SCALE = System.getProperty(PKG + "time.scale", "1");
    static final String TIME_FORMAT = System.getProperty(PKG + "time.format", null);

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
		for (SqlChannel channel : channels) {
			// TODO: configure clients and create missing tables
			
		}
	}

    @Override
	protected void onWrite(List<SqlChannel> channels, long timestamp) throws IOException {
    	List<SqlClientCollection> clients = new ArrayList<SqlClientCollection>();
		for (SqlChannel channel : channels) {
			// TODO: group channels by client and call write(timestamp)
			
		}
		for (SqlClientCollection client : clients) {
			client.write(timestamp);
		}
    }

    @Override
	protected List<Record> onRead(SqlChannel channel, long startTime, long endTime) throws IOException {
    	return getClient(channel).read(channel, startTime, endTime);
    }

	private SqlClient getClient(SqlConfigs configs) throws IOException {
		SqlClient client = clients.get(configs.getDatabase());
		if (client == null) {
			client = new SqlClient(configs);
		}
		return client;
	}

	private class SqlClientCollection extends LinkedList<SqlChannel> {
		private static final long serialVersionUID = 5722160812688011225L;
		
		private final SqlClient client;
		
		public SqlClientCollection(SqlClient client) {
			this.client = client;
		}
		
		public void write(long timestamp) throws IOException {
			client.write(this, timestamp);
		}
		
	}

}
