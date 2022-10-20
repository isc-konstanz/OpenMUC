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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.ConfigWriteException;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.DriverNotAvailableException;
import org.openmuc.framework.config.IdCollisionException;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.DeviceState;
import org.openmuc.framework.lib.rest.Const;
import org.openmuc.framework.lib.rest.FromJson;
import org.openmuc.framework.lib.rest.ToJson;
import org.openmuc.framework.lib.rest.exceptions.MissingJsonObjectException;
import org.openmuc.framework.lib.rest.exceptions.RestConfigIsNotCorrectException;
import org.openmuc.framework.lib.rest.objects.RestDeviceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class DeviceResourceServlet extends GenericServlet {
    private static final long serialVersionUID = 4619892734239871891L;

    private static final Logger logger = LoggerFactory.getLogger(DeviceResourceServlet.class);

    private static final String REQUESTED_REST_PATH_IS_NOT_AVAILABLE = "Requested rest path is not available";
    private static final String REQUESTED_ID_IS_NOT_AVAILABLE = "Requested device is not available";
    private static final String REST_PATH = " Rest Path = ";
    private static final String REST_ID = " Device ID = ";

    private DataAccessService dataAccess;
    private ConfigService configService;
    private RootConfig rootConfig;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);

        if (pathAndQueryString == null) {
            return;
        }

        setConfigAccess();

        String pathInfo = pathAndQueryString[0];
        String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

        response.setStatus(HttpServletResponse.SC_OK);

        ToJson json = new ToJson();

        if (pathInfo.equals("/")) {
            boolean details = Boolean.parseBoolean(request.getParameter("details"));
            if (details) {
                doGetDeviceList(json);
            }
            else {
                doGetDeviceIdList(json);
            }
        }
        else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.STATES)) {
            doGetStateList(json);
        }
        else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.CONFIGS)) {
            doGetConfigsList(json);
        }
        else {
            doGetDevice(request, response, pathInfoArray, json);
        }
        sendJson(json, response);
    }

    private void doGetDeviceIdList(ToJson json) throws IOException {
        List<String> deviceList = getDeviceIdList();

        json.addStringList(Const.DEVICES, deviceList);
    }

    private void doGetDeviceList(ToJson json) throws IOException {
        List<RestDeviceWrapper> deviceList = new LinkedList<RestDeviceWrapper>();

        Collection<DeviceConfig> deviceConfigs = getConfigsList();
        for (DeviceConfig config : deviceConfigs) {
            deviceList.add(RestDeviceWrapper.getDevice(config, configService, dataAccess));
        }
        json.addDeviceList(deviceList);
    }

    private void doGetDevice(HttpServletRequest request, HttpServletResponse response, String[] pathInfoArray,
            ToJson json) throws IOException {

        String deviceId = pathInfoArray[0];

        List<String> deviceList = getDeviceIdList();
        if (deviceList.contains(deviceId)) {
            if (pathInfoArray.length == 1) {
                boolean details = Boolean.parseBoolean(request.getParameter("details"));
                if (details) {
                    doGetDevice(json, deviceId, response);
                }
                else {
                    json.addRecordList(getChannelList(deviceId));
                    json.addDeviceState(configService.getDeviceState(deviceId));
                }
            }
            else if (pathInfoArray[1].equalsIgnoreCase(Const.STATE)) {
                json.addDeviceState(configService.getDeviceState(deviceId));
            }
            else if (pathInfoArray.length > 1 && pathInfoArray[1].equals(Const.CHANNELS)) {
                json.addChannelIdList(getChannelList(deviceId));
                json.addDeviceState(configService.getDeviceState(deviceId));
            }
            else if (pathInfoArray.length == 2 && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                doGetConfigs(json, deviceId, response);
            }
            else if (pathInfoArray.length == 3 && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                String configField = pathInfoArray[2];
                doGetConfigField(json, deviceId, configField, response);
            }
            else if (pathInfoArray[1].equalsIgnoreCase(Const.SCAN)) {
                String settings = request.getParameter(Const.SETTINGS);
                List<ChannelScanInfo> channelScanInfoList = scanForAllChannels(deviceId, settings, response);
                json.addChannelScanInfoList(channelScanInfoList);
            }
            else {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_ID, deviceId);
            }
        }
        else {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    REQUESTED_ID_IS_NOT_AVAILABLE, REST_ID, deviceId);
        }
    }

    private void doGetDevice(ToJson json, String deviceId, HttpServletResponse response) {
        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

        json.addDevice(RestDeviceWrapper.getDevice(deviceConfig, configService, dataAccess));
    }

    private void doGetStateList(ToJson json) {
        Map<String, DeviceState> deviceStates = new HashMap<String, DeviceState>();

        Collection<DeviceConfig> deviceConfigs = getConfigsList();
        for (DeviceConfig deviceConfig : deviceConfigs) {
            String deviceId = deviceConfig.getId();
            deviceStates.put(deviceId, configService.getDeviceState(deviceId));
        }
        json.addDeviceStateList(deviceStates);
    }

    private void doGetConfigsList(ToJson json) {
        List<DeviceConfig> deviceConfigs = getConfigsList();

        json.addDeviceConfigList(deviceConfigs);
    }

    private void doGetConfigs(ToJson json, String deviceId, HttpServletResponse response) throws IOException {
        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

        if (deviceConfig != null) {
            json.addDeviceConfig(deviceConfig);
        }
        else {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    REQUESTED_ID_IS_NOT_AVAILABLE, REST_ID, deviceId);
        }
    }

    private void doGetConfigField(ToJson json, String deviceId, String configField, HttpServletResponse response)
            throws IOException {
        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

        if (deviceConfig != null) {
            JsonObject jsoConfigAll = ToJson.getDeviceConfigAsJsonObject(deviceConfig);
            if (jsoConfigAll == null) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Could not find JSON object \"configs\"");
            }
            else {
                JsonElement jseConfigField = jsoConfigAll.get(configField);

                if (jseConfigField == null) {
                    ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                            "Requested rest config field is not available.", " configField = ", configField);
                }
                else {
                    JsonObject jso = new JsonObject();
                    jso.add(configField, jseConfigField);
                    json.addJsonObject(Const.CONFIGS, jso);
                }
            }
        }
        else {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    REQUESTED_ID_IS_NOT_AVAILABLE, REST_ID, deviceId);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
            FromJson json = ServletLib.getFromJson(request, logger, response);
            if (json == null) {
                return;
            }
            
            if (pathInfoArray.length == 1) {
                String deviceId = pathInfoArray[0];

                doSetConfigs(deviceId, response, json, false);
            }
            else {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }

        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
            FromJson json = ServletLib.getFromJson(request, logger, response);
            if (json == null) {
                return;
            }
            
            if (pathInfoArray.length < 1) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }
            else {
                String deviceId = pathInfoArray[0];

                DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

                if (deviceConfig != null && pathInfoArray.length == 2
                        && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                    doSetConfigs(deviceId, response, json, true);
                }
                else {
                    ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                            REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
                }
            }
        }
    }

    private boolean doSetConfigs(String deviceId, HttpServletResponse response, FromJson json,
            boolean isHttpPut) {

        try {
            if (isHttpPut) {
                return doPutConfigs(deviceId, response, json);
            }
            else {
                return doPostConfigs(deviceId, response, json);
            }
        } catch (JsonSyntaxException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_CONFLICT, logger,
                    "JSON syntax is wrong.");
        } catch (ConfigWriteException e) {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                    "Could not write device \"", deviceId, "\".");
        } catch (RestConfigIsNotCorrectException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    "Not correct formed device config json.", " JSON = ", json.getJsonObject().toString());
        } catch (Error e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    e.getMessage());
        } catch (MissingJsonObjectException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_CONFLICT, logger, e.getMessage());
        }
        return false;
    }

    private synchronized boolean doPutConfigs(String deviceId, HttpServletResponse response, FromJson json)
            throws JsonSyntaxException, ConfigWriteException, RestConfigIsNotCorrectException,
            MissingJsonObjectException, IllegalStateException {

        boolean ok = false;

        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);
        if (deviceConfig != null) {
            try {
                json.setDeviceConfig(deviceConfig, deviceId);
            } catch (IdCollisionException e) {
            }
            configService.setConfig(rootConfig);
            configService.writeConfigToFile();
            response.setStatus(HttpServletResponse.SC_OK);
            ok = true;
        }
        else {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Not able to access to device ", deviceId);
        }
        return ok;
    }

    private synchronized boolean doPostConfigs(String deviceId, HttpServletResponse response,
            FromJson json) throws JsonSyntaxException, ConfigWriteException, RestConfigIsNotCorrectException, Error,
            MissingJsonObjectException, IllegalStateException {

        boolean ok = false;
        DriverConfig driverConfig;

        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

        JsonObject jso = json.getJsonObject();
        String driverId = jso.get(Const.DRIVER).getAsString();

        if (driverId != null) {
            driverConfig = rootConfig.getDriver(driverId);
        }
        else {
            throw new Error("No driver ID in JSON");
        }

        if (driverConfig == null) {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                    "Driver does not exists: ", driverId);
        }
        else if (deviceConfig != null) {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                    "Device already exists: ", deviceId);
        }
        else {
            try {
                deviceConfig = driverConfig.addDevice(deviceId);
                json.setDeviceConfig(deviceConfig, deviceId);
            } catch (IdCollisionException e) {
            }
            
            configService.setConfig(rootConfig);
            configService.writeConfigToFile();
            response.setStatus(HttpServletResponse.SC_OK);
            ok = true;
        }
        return ok;
    }

    @Override
    public synchronized void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[0];
            String deviceId = null;
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

            deviceId = pathInfoArray[0];
            DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

            if (pathInfoArray.length != 1) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }
            else if (deviceConfig == null) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Device \"" + deviceId + "\" does not exist.");
            }
            else {
                try {
                    deviceConfig.delete();
                    configService.setConfig(rootConfig);
                    configService.writeConfigToFile();

                    if (rootConfig.getDriver(deviceId) == null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    else {
                        ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                logger, "Not able to delete driver ", deviceId);
                    }
                } catch (ConfigWriteException e) {
                    ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                            "Not able to write into config.");
                }
            }
        }
    }

    private List<ChannelScanInfo> scanForAllChannels(String deviceId, String settings, HttpServletResponse response) {
        List<ChannelScanInfo> channelList = new ArrayList<>();
        List<ChannelScanInfo> scannedDevicesList;

        try {
            scannedDevicesList = configService.scanForChannels(deviceId, settings);

            for (ChannelScanInfo scannedDevice : scannedDevicesList) {
                channelList.add(scannedDevice);
            }

        } catch (UnsupportedOperationException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Device does not support scanning.", REST_ID, deviceId);
        } catch (DriverNotAvailableException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    REQUESTED_ID_IS_NOT_AVAILABLE, REST_ID, deviceId);
        } catch (ArgumentSyntaxException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    "Argument syntax was wrong.", REST_ID, deviceId, " Settings = ", settings);
        } catch (ScanException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Error while scan device channels", REST_ID, deviceId, " Settings = ", settings);
        }
        return channelList;
    }

    private List<String> getDeviceIdList() {
        List<String> deviceList = new ArrayList<>();

        Collection<DeviceConfig> deviceConfigs = getConfigsList();
        for (DeviceConfig deviceConfig : deviceConfigs) {
            deviceList.add(deviceConfig.getId());
        }
        return deviceList;
    }

    private List<DeviceConfig> getConfigsList() {
        List<DeviceConfig> deviceConfigs = new ArrayList<DeviceConfig>();

        Collection<DriverConfig> driverConfigs;
        driverConfigs = rootConfig.getDrivers();
        
        for (DriverConfig driverConfig : driverConfigs) {
            String driverId = driverConfig.getId();
            deviceConfigs.addAll(rootConfig.getDriver(driverId).getDevices());
        }
        return deviceConfigs;
    }

    private List<Channel> getChannelList(String deviceId) {
        List<Channel> channels = new ArrayList<>();

        Collection<ChannelConfig> channelConfigs = rootConfig.getDevice(deviceId).getChannels();
        for (ChannelConfig channelConfig : channelConfigs) {
            channels.add(dataAccess.getChannel(channelConfig.getId()));
        }
        return channels;
    }

    private void setConfigAccess() {
        this.dataAccess = handleDataAccessService(null);
        this.configService = handleConfigService(null);
        this.rootConfig = handleRootConfig(null);
    }

}
