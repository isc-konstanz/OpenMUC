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
package org.openmuc.framework.driver.mysql;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.driver.mysql.channel.ChannelDateTimeFormat;
import org.openmuc.framework.driver.mysql.channel.ChannelTimestampFormat;
import org.openmuc.framework.driver.spi.ChannelContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.Device;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlClient extends Device<SqlChannel> {
	private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

	@Address(id = "host", description = "IP-Adress of the host")
	private String host;

	@Address(id = "port", mandatory = false, description = "Portnumber at the particular host")
	private int port = 3306;

	@Address(id = "database", description = "Name of the database")
	private String database;

	@Setting(id = "driver", mandatory = false)
	private String driver = "com.mysql.cj.jdbc.Driver";

	@Setting(id = "type", mandatory = false)
	private String type = "jdbc:mysql";

	@Setting(id = "user", description = "Username")
	private String user;

	@Setting(id = "password")
	private String password;

	@Setting(id = "timeType", mandatory = false, description = "Time is differently described in different databases, as a timestamp or in seperate columns for date and time.")
	private TimeType timeType;

	@Setting(id = "timeFormat", mandatory = false, description = "")
	private String timeFormat = null;

	@Setting(id = "table", mandatory = false, description = "Tablename in the database")
	private String table;

	private ComboPooledDataSource source = null;

	private final List<String> tables = new ArrayList<String>();
	
	@Override
	protected void onConnect() throws ArgumentSyntaxException, ConnectionException {
		String url = type + "://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
		logger.info("Initializing MySQL connection \"{}\"", url);
		try {
			if (source != null) {
				source.close();
			}
			source = new ComboPooledDataSource();
			source.setDriverClass(driver);
			source.setJdbcUrl(url);
			source.setUser(user);
			source.setPassword(password);

			showTables();

		} catch (PropertyVetoException e) {
			throw new ConnectionException(e);
		}
	}

	private void showTables() throws ConnectionException {
		try (Connection connection = source.getConnection(user, password)) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet result = statement.executeQuery(String.format("SHOW tables FROM %s;", database))) {
					while (result.next()) {
						tables.add(result.getString("tables_in_" + database));
						logger.debug("Found table: {}", result.getString("tables_in_" + database));
					}
				}
			}
		} catch (SQLException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public void onDisconnect() {
		source.close();
		source = null;
	}

	@Override
	public Record onRead(List<SqlChannel> channels, Object containerListHandle, String samplingGroup)
			throws ConnectionException {
		Record record = null;
		try (Connection connection = source.getConnection(user, password)) {
			// connection.setAutoCommit(false);
			for (SqlChannel channel : channels) {
				String query = channel.readQuery();
				try (Statement statement = connection.createStatement()) {
					try (ResultSet result = statement.executeQuery(query)) {
						while (result.next()) {
							Date date = new Date();
							long time = System.currentTimeMillis();
							String value = String.valueOf(result.getString(channel.data));
							record = new Record(new StringValue(value), time, Flag.VALID);
						}
					}
				}
			}
			// connection.commit();
		} catch (SQLException e) {
			throw new ConnectionException(e);
		}

		return record;
	}

	@Override
	public Object onWrite(List<SqlChannel> channels, Object containerListHandle) throws ConnectionException {
		for (SqlChannel channel : channels) {
			channel.setFlag(Flag.VALID);
		}
		return null;
	}

	@Override
	protected SqlChannel newChannel(ChannelContainer container) throws ArgumentSyntaxException {
		switch (timeType) {
    	case TIMESTAMP_UNIX:
    		return new ChannelTimestampFormat(container); 
		case TIMESTAMP:
			return new ChannelTimestampFormat(container);
		case DATETIME:
			return new ChannelDateTimeFormat(container);
		case TIMESTAMP_MULTIPLEROW:
			return new ChannelTimestampFormat(container);
		default:
			return null;
		}
	}
}
