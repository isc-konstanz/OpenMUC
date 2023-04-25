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
package org.openmuc.framework.server.restws.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.lib.rest.FromJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletLib {

    private static final String COULD_NOT_SEND_HTTP_ERROR_MESSAGE = "Could not send HTTP Error message.";

    private static final Logger logger = LoggerFactory.getLogger(ServletLib.class);

    protected static final int PATH_ARRAY_NR = 0;
    protected static final int QUERRY_ARRAY_NR = 1;

    protected static String buildString(BufferedReader br) {
        StringBuilder text = new StringBuilder();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return text.toString();
    }

    protected static FromJson getFromJson(HttpServletRequest request, Logger logger, HttpServletResponse response) {
        FromJson json = null;
        try {
            json = new FromJson(ServletLib.getJsonText(request));
        } catch (Exception e) {
            ServletLib.sendHTTPErrorAndLogWarn(response, HttpServletResponse.SC_BAD_REQUEST, logger,
                    "Malformed JSON message: ", e.getMessage());
        }
        return json;
    }

    /**
     * Send HTTP Error and log as warning. Only the first String will be sent over HTTP response.
     * 
     * @param response
     *            HttpServletResponse response
     * @param errorCode
     *            error code
     * @param logger
     *            logger
     * @param msg
     *            message array
     */
    protected static void sendHTTPErrorAndLogWarn(HttpServletResponse response, int errorCode, Logger logger,
            String... msg) {
        try {
            response.sendError(errorCode, msg[0]);
        } catch (IOException e) {
            logger.error(COULD_NOT_SEND_HTTP_ERROR_MESSAGE, e);
        }
        StringBuilder warnMessage = new StringBuilder();
        for (String m : msg) {
            warnMessage.append(m);
        }
        if (logger.isWarnEnabled()) {
            logger.warn(warnMessage.toString());
        }
    }

    /**
     * Send HTTP Error and log as debug. Only the first String will be sent over HTTP response.
     * 
     * @param response
     *            HttpServletResponse response
     * @param errorCode
     *            error code
     * @param logger
     *            logger
     * @param msg
     *            message array
     */
    protected static void sendHTTPErrorAndLogDebug(HttpServletResponse response, int errorCode, Logger logger,
            String... msg) {
        try {
            response.sendError(errorCode, msg[0]);
        } catch (IOException e) {
            logger.error(COULD_NOT_SEND_HTTP_ERROR_MESSAGE, e);
        }
        StringBuilder warnMessage = new StringBuilder();
        for (String m : msg) {
            warnMessage.append(m);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(warnMessage.toString());
        }
    }

    /**
     * Send HTTP Error and log as error. Logger and HTTP response are the same message.
     * 
     * @param response
     *            HttpServletResponse response
     * @param errorCode
     *            error code
     * @param logger
     *            logger
     * @param msg
     *            message array
     */
    protected static void sendHTTPErrorAndLogErr(HttpServletResponse response, int errorCode, Logger logger,
            String... msg) {
        try {
            StringBuilder sbErrMessage = new StringBuilder();
            for (String m : msg) {
                sbErrMessage.append(m);
            }
            String errMessage = sbErrMessage.toString();
            response.sendError(errorCode, errMessage);
            logger.error(errMessage);
        } catch (IOException e) {
            logger.error(COULD_NOT_SEND_HTTP_ERROR_MESSAGE, e);
        }
    }

    protected static String getJsonText(HttpServletRequest request) throws IOException {
        return ServletLib.buildString(request.getReader());
    }

    protected static String[] getPathInfoArray(String pathInfo) {
        String[] pathInfoArray;
        if (pathInfo.length() > 1) {
            pathInfoArray = pathInfo.replaceFirst("/", "").split("/");
            for (int i=0; i<pathInfoArray.length; i++) {
                try {
                    pathInfoArray[i] = URLDecoder.decode(pathInfoArray[i], "UTF-8");
                    
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        else {
            pathInfoArray = new String[] { "/" };
        }
        return pathInfoArray;
    }

    private ServletLib() {
    }
}
