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
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.Device;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlClient extends Device<SqlChannel> {
    private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

    @Address
    private String address;

    @Address
    private String port;

    @Setting
    private String driver;

    @Setting
    private String user;

    @Setting
    private String password;

    private ComboPooledDataSource source = null;

    @Override
    protected void onConnect() throws ArgumentSyntaxException, ConnectionException {
    	String url = address+":"+port;
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
            
        } catch (PropertyVetoException e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public void onDisconnect() {
        source.close();
        source = null;
    }

    @Override
    public Object onRead(List<SqlChannel> channels, Object containerListHandle, String samplingGroup) throws ConnectionException {
        try (Connection connection = source.getConnection(user, password)) {
        	// TODO: google better solution to utilize mysql statement pooling
            //connection.setAutoCommit(false);
        	
            for (SqlChannel channel : channels) {
            	try (Statement statement = connection.createStatement()) {
                	// TODO build query with the help of channel configurations
                	String query = channel.readQuery(1, 2, 3);
                	
                	try (ResultSet result = statement.executeQuery(query)) {
                        if (result.first()) {
                            long time = result.getLong(SqlChannel.COLUMN_TIME);
                            double value = result.getInt(SqlChannel.COLUMN_DATA);
                            
                        	channel.setRecord(new Record(new DoubleValue(value), time, Flag.VALID));
                        }
                        else {
                        	// TODO: log more details
                        	channel.setRecord(new Record(Flag.DRIVER_ERROR_CHANNEL_NOT_ACCESSIBLE));
                        }
                    }
                }
            }
            //connection.commit();
        	
        } catch (SQLException e) {
			throw new ConnectionException(e);
		}
        return null;
    }

    @Override
    public Object onWrite(List<SqlChannel> channels, Object containerListHandle) throws ConnectionException {
        for (SqlChannel channel : channels) {
            
        	channel.setFlag(Flag.VALID);
        }
        return null;
    }

}
