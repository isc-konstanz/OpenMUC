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
import java.util.LinkedList;


public class SqlClientChannels extends LinkedList<SqlChannel> {
    private static final long serialVersionUID = 5722160812688011225L;

    SqlClient client;

    SqlClientChannels() {
    }

    SqlClientChannels(SqlClient client) {
        this.client = client;
    }

    public void create(SqlConfigs configs) throws IOException {
        this.client = new SqlClient(configs);
        
        for (SqlChannel channel : this) {
            if (!client.hasTable(channel) && channel.getIndexType() == IndexType.TIMESTAMP_UNIX && SqlLogger.TABLE == null) {
                client.createTable(channel);
            }
        }
    }

    public void write(long timestamp) throws IOException {
        client.write(this, timestamp);
    }

    public boolean equals(SqlConfigs configs) {
        return client.equals(configs);
    }

}
