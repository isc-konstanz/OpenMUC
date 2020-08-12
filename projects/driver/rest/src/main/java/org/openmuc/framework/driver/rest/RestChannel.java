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
package org.openmuc.framework.driver.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.Channel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.lib.json.Const;
import org.openmuc.framework.lib.json.FromJson;
import org.openmuc.framework.lib.json.ToJson;
import org.openmuc.framework.lib.json.rest.objects.RestRecord;
import org.openmuc.framework.options.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

public class RestChannel extends Channel {
    private static final Logger logger = LoggerFactory.getLogger(RestRemote.class);

    @Address(id = "id",
            name = "Channel ID",
            description = "The ID of the remote OpenMUC channel")
    private String id;
    private String uri;

    @Override
    protected void onConfigure() throws ArgumentSyntaxException {
        try {
			uri = URLEncoder.encode(id, RestDriver.CHARSET.toString());
			
		} catch (UnsupportedEncodingException e) {
			throw new ArgumentSyntaxException(e.getMessage());
		}
    }

    public boolean checkTimestamp(RestConnection connection) throws ConnectionException {
        @SuppressWarnings("deprecation")
        Record record = getChannel().getLatestRecord();
        
        if (record.getTimestamp() == null || record.getFlag() != Flag.VALID
                || record.getTimestamp() < readTimestamp(connection)) {
            
            return true;
        }
        setRecord(record);
        
        return false;
    }

    public long readTimestamp(RestConnection connection) throws ConnectionException {
    	String jsonStr = connection.get(uri + '/' + Const.TIMESTAMP);
        FromJson json = new FromJson(jsonStr);
    	logger.debug("Received json string: {}", jsonStr);
    	
        JsonElement timestamp = json.getJsonObject().get(Const.TIMESTAMP);
        if (timestamp == null) {
            return -1;
        }
        return timestamp.getAsNumber().longValue();
    }

    public void read(RestConnection connection) throws ConnectionException {
    	String jsonStr = connection.get(uri);
        FromJson json = new FromJson(jsonStr);
    	logger.debug("Received json string: {}", jsonStr);
        
        Record record = json.getRecord(getValueType());
        if (record != null) {
            setRecord(record);
        }
        else {
            setFlag(Flag.DRIVER_ERROR_READ_FAILURE);
        }
    }

    public void setRecord(RestRecord record) {
        if (record != null) {
            setRecord(FromJson.convertRecord(record, getValueType()));
        }
    }

    public void write(RestConnection connection, long timestamp) throws ConnectionException {
        Record record = new Record(getValue(), timestamp, Flag.VALID);
        ToJson json = new ToJson();
        json.addRecord(record, getValueType());
        
        Flag flag = connection.put(uri, json.toString());
        setFlag(flag);
    }

    public boolean equals(org.openmuc.framework.lib.json.rest.objects.RestChannel channel) {
    	return id.equals(channel.getId());
    }

}
