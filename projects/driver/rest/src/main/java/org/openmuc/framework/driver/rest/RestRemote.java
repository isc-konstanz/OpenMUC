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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.lib.json.FromJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRemote extends RestConfigs {
    private static final Logger logger = LoggerFactory.getLogger(RestRemote.class);
    
    private RestConnection connection;

	@Override
    protected void onConnect() throws ConnectionException {
        if (address.startsWith("https://")) {
            TrustManager[] trustManager = getTrustManager();
            
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustManager, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                
            } catch (KeyManagementException e) {
                throw new ConnectionException(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                throw new ConnectionException(e.getMessage());
            }
            
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = getHostnameVerifier();
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
        logger.info("Connecting to remote OpenMUC: {}", address);

        // This is only used to verify the existence of the remote OpenMUC
    	connection = new RestConnection(this);
    	connection.connect();
    }

	@Override
	protected void onDisconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (IOException e) {
			logger.warn("Unexpected error closing REST connection: {}", e.getMessage());
		}
	}

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    private TrustManager[] getTrustManager() {
        return new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};
    }

    @Override
    public Object onRead(List<RestChannel> channels, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        long timestamp = System.currentTimeMillis();
        try {
        	if (bulkReading) {
        		return readChannels(channels);
        	}
            for (RestChannel channel : channels) {
            	readChannel(channel);
            }
        } finally {
            logger.trace("Read {} channels in {}ms",
                    channels.size(), System.currentTimeMillis() - timestamp);
        }
        return null;
    }

    private Object readChannels(List<RestChannel> channels) throws ConnectionException {
    	String jsonStr = connection.get();
        FromJson json = new FromJson(jsonStr);
    	logger.debug("Received json string: {}", jsonStr);
        
    	List<org.openmuc.framework.lib.json.rest.objects.RestChannel> records = json.getRestChannelList();
        for (RestChannel channel : channels) {
        	readChannel(channel, records);
        }
    	return null;
    }

    private void readChannel(RestChannel channel,
    		List<org.openmuc.framework.lib.json.rest.objects.RestChannel> records) throws ConnectionException {
    	for (org.openmuc.framework.lib.json.rest.objects.RestChannel record : records) {
            if (channel.equals(record)) {
            	channel.setRecord(record.getRecord());
            	return;
            }
    	}
    	channel.setFlag(Flag.DRIVER_ERROR_READ_FAILURE);
    }

    private void readChannel(RestChannel channel) throws ConnectionException {
        if (!checkTimestamp || channel.checkTimestamp(connection)) {
            channel.read(connection);
        }
    }

    @Override
    public Object onWrite(List<RestChannel> channels, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {

        long timestamp = System.currentTimeMillis();
        try {
            for (RestChannel channel : channels) {
                channel.write(connection, timestamp);
            }
        } finally {
            logger.trace("Wrote {} channels in {}ms",
                    channels.size(), System.currentTimeMillis() - timestamp);
        }
        return null;
    }

}
