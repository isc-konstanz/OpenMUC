/*
 * Copyright 2011-2020 Fraunhofer ISE
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.driver.spi.ConnectionException;

public class RestConnection implements AutoCloseable {

    private final String url;
    private final String authorization;

    private final int timeout;

    private URLConnection connection;

    public RestConnection(RestConfigs configs) {
        this(configs.getUrl(), 
             configs.getAuthorization(),
             configs.getTimeout());
    }

    public RestConnection(String url, String authorization, int timeout) {
        this.url = url;
        this.authorization = authorization;
        this.timeout = timeout;
    }

    private URLConnection open(String suffix) throws IOException {
        URL url = new URL(this.url + suffix);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + authorization);
        
        return connection;
    }

    public RestConnection connect() throws ConnectionException {
        try {
            connection = open("rest/connect/");
            connection.connect();
            
            verifyResponseCode(connection);
            
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        if (connection != null ) {
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).disconnect();
            }
            else {
                ((HttpURLConnection) connection).disconnect();
            }
        }
    }

    public String get() throws ConnectionException {
        try {
            connection = open("rest/channels");
            
            verifyResponseCode(connection);
            return readResponse(connection);
            
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    public String get(String suffix) throws ConnectionException {
        try {
            connection = open("rest/channels/" + suffix);
            
            verifyResponseCode(connection);
            return readResponse(connection);
            
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    public Flag put(String suffix, String output) throws ConnectionException {
        try {
            connection = open("rest/channels/" + suffix);
            connection.setDoOutput(true);
            
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setRequestMethod("PUT");
            }
            else {
                ((HttpURLConnection) connection).setRequestMethod("PUT");
            }
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            try {
                out.write(output);
                
            } finally {
                out.close();
            }
            return verifyResponseCode(connection);
            
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    private String readResponse(URLConnection connection) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        InputStream responseStream = connection.getInputStream();
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, RestDriver.CHARSET));
            String responseLine;
            while ((responseLine = streamReader.readLine()) != null) {
                responseBuilder.append(responseLine);
            }
        }
        finally {
            responseStream.close();
        }
        return responseBuilder.toString();
    }

    private Flag verifyResponseCode(URLConnection connection) throws ConnectionException, IOException {
        int code;
        if (connection instanceof HttpsURLConnection) {
            code = ((HttpsURLConnection) connection).getResponseCode();
            if (!(code >= 200 && code < 300)) {
                throw new ConnectionException(
                        "HTTPS " + code + ":" + ((HttpsURLConnection) connection).getResponseMessage());
            }
        }
        else {
            code = ((HttpURLConnection) connection).getResponseCode();
            if (!(code >= 200 && code < 300)) {
                throw new ConnectionException(
                        "HTTP " + code + ":" + ((HttpURLConnection) connection).getResponseMessage());
            }
        }
        return Flag.VALID;
    }

}
