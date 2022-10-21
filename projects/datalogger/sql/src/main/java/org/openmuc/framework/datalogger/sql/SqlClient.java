/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.datalogger.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.lib.sql.SqlConnector;
import org.openmuc.framework.lib.sql.SqlData;
import org.openmuc.framework.lib.sql.SqlSettings;

public class SqlClient extends SqlConnector {

    private final LinkedList<SqlChannel> channels = new LinkedList<SqlChannel>();

    public SqlClient(SqlSettings settings) throws IOException {
        super(settings);
    }

    public void open() throws IOException {
        super.open();
        try (Connection connection = connect()) {
            createTables(connection, getChannels());
            
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void close() {
        super.close();
        channels.clear();
    }

    public String getUrl() {
        return settings.getDatabaseUrl();
    }

    @SuppressWarnings("unchecked")
	public List<SqlData> getChannels() {
    	return (List<SqlData>) (List<? extends SqlData>) channels;
    }

	protected List<SqlData> outChannels(List<SqlChannel> channels) {
    	return channels.stream()
                       .filter(c -> hasChannel(c))
                       .collect(Collectors.toList());
    }

	public boolean hasChannel(SqlChannel channel) {
		return channels.stream().anyMatch(c -> c.getId().equals(channel.getId()));
	}

    public void addChannel(SqlChannel channel) {
        channels.add(channel);
    }

    public Record readLatest(SqlChannel channel) throws IOException {
        try (Connection connection = connect()) {
            return read(connection, channel);
            
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public List<Record> read(SqlChannel channel, long startTime, long endTime) throws IOException {
        try (Connection connection = connect()) {
            return read(connection, channel, startTime, endTime);
            
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void write(List<SqlChannel> channels, long timestamp) throws IOException {
        try (Connection connection = connect()) {
            try (Statement statement = connection.createStatement()) {
                this.write(statement, outChannels(channels), timestamp);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
