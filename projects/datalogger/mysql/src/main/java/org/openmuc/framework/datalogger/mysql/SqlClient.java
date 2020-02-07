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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.datalogger.mysql.SqlChannel;
import org.openmuc.framework.datalogger.mysql.TimeType;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.Device;
import org.openmuc.framework.driver.spi.ChannelContainer;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlClient {
	private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

	private final ComboPooledDataSource source;

	private final String user;
	private final String password;
	private final String database;

	private final TimeType timeType;
	private final Double timeScale;
	private final String timeFormat;

	private final List<String> tables = new ArrayList<String>();

	public SqlClient(SqlConfigs configs) throws IOException {
		user = configs.getDatabaseUser();
		password = configs.getDatabasePassword();
		database = configs.getDatabaseName();

		timeType = configs.getTimeType();
		timeScale = configs.getTimeScale();
		timeFormat = configs.getTimeFormat();
		logger.info("Connecting to MySQL database \"{}\"", database);
		try {
			source = new ComboPooledDataSource();
			source.setDriverClass(SqlLogger.DB_DRIVER);
			source.setJdbcUrl(configs.getDatabase());
			source.setUser(user);
			source.setPassword(password);

			showTables();

		} catch (PropertyVetoException e) {
			throw new IOException(e);
		}
	}

	public void close() {
		source.close();
		tables.clear();
	}

	private void showTables() {
		try (Connection connection = source.getConnection(user, password)) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet result = statement.executeQuery(String.format("SHOW tables FROM %s", database))) {
					while (result.next()) {
						tables.add(result.getString("tables_in_" + database));
						logger.debug("Found table: {}", result.getString("tables_in_" + database));
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error retrieving table list for database {}: {}", source.getJdbcUrl(), e.getMessage());
		}
	}

	public void write(List<SqlChannel> channels, long timestamp) throws IOException {
		if (source == null) {
			throw new IOException("Connection to database not open: " + source.getJdbcUrl());
		}
		for (SqlChannel channel : channels) {

		}
	}

	public List<Record> read(SqlChannel channel, long startTime, long endTime) throws IOException {
		List<Record> record = new ArrayList<Record>();
		Date currentDate = new Date(System.currentTimeMillis());
		DateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String date = simpleFormatter.format(currentDate);
		Date start = new Date(startTime);
		Date end = new Date(endTime);
		DateFormat formatter = new SimpleDateFormat(timeFormat);
		String startFormatted = formatter.format(start);
		String endFormatted = formatter.format(end);
		String query;
		Double value = null;
		switch (timeType) {
		case TIMESTAMP:
			query = String.format(SqlChannel.QUERY_SELECT_SINGLEROW, channel.data, channel.getTable(), 
					startFormatted, endFormatted);
			break;
		case TIMESTAMP_MULTIPLEROW:
			query = String.format(SqlChannel.QUERY_SELECT_MULTIPLEROW, channel.data, channel.getTable(), channel.column,
					startFormatted, endFormatted);
			break;
		default:
			DateFormat extraFormatter = new SimpleDateFormat("hh:mm:ss");
			startFormatted = extraFormatter.format(start);
			endFormatted = extraFormatter.format(end);
			query = String.format(SqlChannel.QUERY_SELECT_DATETIME, channel.data, database, channel.getTable(),
					startFormatted, endFormatted, date, date);		
		}
		logger.info(query);
		try (Connection connection = source.getConnection()) {
			// connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement()) {
				try (ResultSet result = statement.executeQuery(query)) {
					result.last();
					int i = result.getRow();
					result.beforeFirst();
					for (int j = 0; j < i; j++) {
						result.next();
						logger.info("Result: {}", result.getString(1));
						value = Double.valueOf(result.getString(1));
						record.add(new Record(new DoubleValue(value), endTime, Flag.VALID));
					}
				}
			}
			// connection.commit();

		} catch (SQLException e) {
			throw new IOException(e);
		}
		return record;
	}

}
