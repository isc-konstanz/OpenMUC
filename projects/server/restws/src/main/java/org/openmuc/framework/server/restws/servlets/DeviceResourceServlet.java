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
import org.openmuc.framework.lib.json.Const;
import org.openmuc.framework.lib.json.FromJson;
import org.openmuc.framework.lib.json.ToJson;
import org.openmuc.framework.lib.json.exceptions.MissingJsonObjectException;
import org.openmuc.framework.lib.json.exceptions.RestConfigIsNotCorrectException;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class DeviceResourceServlet extends GenericServlet {

    private static final String APPLICATION_JSON = "application/json";
    private static final long serialVersionUID = 4619892734239871891L;
    private static final Logger logger = LoggerFactory.getLogger(DeviceResourceServlet.class);

    private DataAccessService dataAccess;
    private ConfigService configService;
    private RootConfig rootConfig;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);

        if (pathAndQueryString != null) {

            setConfigAccess();

            String deviceId, configField;
            String pathInfo = pathAndQueryString[0];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
            List<String> deviceList = doGetDeviceList();

            response.setStatus(HttpServletResponse.SC_OK);

            ToJson json = new ToJson();

            if (pathInfo.equals("/")) {
                json.addStringList(Const.DEVICES, deviceList);
            }
            else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.STATES)) {
                doGetStateList(json);
            }
            else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.CONFIGS)) {
                doGetConfigsList(json);
            }
            else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.DETAILS)) {
                doGetDetailsList(json);
            }
            else {
                deviceId = pathInfoArray[0].replace("/", "");

                if (deviceList.contains(deviceId)) {

                    List<Channel> deviceChannelList = doGetDeviceChannelList(deviceId);
                    DeviceState deviceState = configService.getDeviceState(deviceId);

                    if (pathInfoArray.length == 1) {
                        json.addChannelRecordList(deviceChannelList);
                        json.addDeviceState(deviceState);
                    }
                    else if (pathInfoArray[1].equalsIgnoreCase(Const.STATE)) {
                        json.addDeviceState(deviceState);
                    }
                    else if (pathInfoArray.length > 1 && pathInfoArray[1].equals(Const.CHANNELS)) {
                        json.addChannelList(deviceChannelList);
                        json.addDeviceState(deviceState);
                    }
                    else if (pathInfoArray.length == 2 && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                        doGetConfigs(json, deviceId, response);
                    }
                    else if (pathInfoArray.length == 3 && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                        configField = pathInfoArray[2];
                        doGetConfigField(json, deviceId, configField, response);
                    }
                    else if (pathInfoArray.length == 2 && pathInfoArray[1].equalsIgnoreCase(Const.DETAILS)) {
                        doGetDetails(json, deviceId, response);
                    }
                    else if (pathInfoArray[1].equalsIgnoreCase(Const.SCAN)) {
                        String settings = request.getParameter(Const.SETTINGS);
                        List<ChannelScanInfo> channelScanInfoList = scanForAllChannels(deviceId, settings, response);
                        json.addChannelScanInfoList(channelScanInfoList);
                    }
                    else {
                        ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                                "Requested rest device is not available, DeviceID = " + deviceId);
                    }
                }
                else {
                    ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                            "Requested rest device is not available, DeviceID = " + deviceId);
                }
            }
            sendJson(json, response);
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
            String deviceId = pathInfoArray[0].replace("/", "");
            FromJson json = new FromJson(ServletLib.getJsonText(request));

            if (pathInfoArray.length == 1) {
                setAndWriteDeviceConfig(deviceId, response, json, false);
            }
            else {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Requested rest path is not available.", " Rest Path = ", request.getPathInfo());
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
            String deviceId = pathInfoArray[0].replace("/", "");
            FromJson json = new FromJson(ServletLib.getJsonText(request));

            if (pathInfoArray.length < 1) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Requested rest path is not available.", " Rest Path = ", request.getPathInfo());
            }
            else {

                DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

                if (deviceConfig != null && pathInfoArray.length == 2
                        && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                    setAndWriteDeviceConfig(deviceId, response, json, true);
                }
                else {
                    ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                            "Requested rest path is not available.", " Rest Path = ", request.getPathInfo());
                }
            }
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[0];
            String deviceId = null;
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

            deviceId = pathInfoArray[0].replace("/", "");
            DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

            if (pathInfoArray.length != 1) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Requested rest path is not available", " Path Info = ", request.getPathInfo());
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
        String deviceIdString = " deviceId = ";

        try {
            scannedDevicesList = configService.scanForChannels(deviceId, settings);

            for (ChannelScanInfo scannedDevice : scannedDevicesList) {
                channelList.add(scannedDevice);
            }

        } catch (UnsupportedOperationException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Device does not support scanning.", deviceIdString, deviceId);
        } catch (DriverNotAvailableException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    "Requested rest device is not available.", deviceIdString, deviceId);
        } catch (ArgumentSyntaxException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    "Argument syntax was wrong.", deviceIdString, deviceId, " Settings = ", settings);
        } catch (ScanException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Error while scan device channels", deviceIdString, deviceId, " Settings = ", settings);
        }
        return channelList;
    }

    private List<Channel> doGetDeviceChannelList(String deviceId) {
        List<Channel> deviceChannelList = new ArrayList<>();
        Collection<ChannelConfig> channelConfig;

        channelConfig = rootConfig.getDevice(deviceId).getChannels();
        for (ChannelConfig chCf : channelConfig) {
            deviceChannelList.add(dataAccess.getChannel(chCf.getId()));
        }
        return deviceChannelList;
    }

    private List<DeviceConfig> getConfigsList() {
        List<DeviceConfig> deviceConfigs = new ArrayList<DeviceConfig>();

        Collection<DriverConfig> driverConfigs;
        driverConfigs = rootConfig.getDrivers();
        
        for (DriverConfig drvCfg : driverConfigs) {
            String driverId = drvCfg.getId();
            deviceConfigs.addAll(rootConfig.getDriver(driverId).getDevices());
        }
        return deviceConfigs;
    }

    private List<String> doGetDeviceList() {
        List<String> deviceList = new ArrayList<>();

        Collection<DeviceConfig> deviceConfig = getConfigsList();
        for (DeviceConfig devCfg : deviceConfig) {
            deviceList.add(devCfg.getId());
        }
        return deviceList;
    }

    private void doGetStateList(ToJson json) {
        Map<String, DeviceState> deviceStates = new HashMap<String, DeviceState>();

        Collection<DeviceConfig> deviceConfigs = getConfigsList();
        for (DeviceConfig devCfg : deviceConfigs) {
            String deviceId = devCfg.getId();
            deviceStates.put(deviceId, configService.getDeviceState(deviceId));
        }
        json.addDeviceStateList(deviceStates);
    }

    private void doGetConfigsList(ToJson json) {

        List<DeviceConfig> deviceConfigs = getConfigsList();
        json.addDeviceConfigList(deviceConfigs);
    }

    private void doGetDetailsList(ToJson json) throws IOException {
        List<RestDeviceDetail> deviceDetails = new LinkedList<RestDeviceDetail>();
        try {
            Collection<DeviceConfig> deviceConfigs = getConfigsList();
            for (DeviceConfig config : deviceConfigs) {
                RestDeviceDetail restDetails = RestDeviceDetail.getRestDeviceDetail(
                        configService.getDeviceState(config.getId()), config, 
                        configService.getDriverInfo(config.getDriver().getId()));
                
                deviceDetails.add(restDetails);
            }
            json.addDeviceDetailList(deviceDetails);
            
        } catch (DriverNotAvailableException e) {
            throw new IOException(e);
        }
    }

    private void doGetConfigs(ToJson json, String deviceId, HttpServletResponse response) throws IOException {
        DeviceConfig deviceConfig;
        deviceConfig = rootConfig.getDevice(deviceId);

        if (deviceConfig != null) {
            json.addDeviceConfig(deviceConfig);
        }
        else {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    "Requested rest device is not available.", " DeviceID = ", deviceId);
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
                    "Requested rest device is not available.", " DeviceID = ", deviceId);
        }
    }

    private void doGetDetails(ToJson json, String deviceId, HttpServletResponse response) throws IOException {
        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);
        try {
            if (deviceConfig != null) {
                json.addDeviceDetail(configService.getDeviceState(deviceConfig.getId()), deviceConfig, 
                        configService.getDriverInfo(deviceConfig.getDriver().getId()));
            }
            else {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Requested rest device is not available.", " DeviceID = ", deviceId);
            }
        } catch (DriverNotAvailableException e) {
            throw new IOException(e);
        }
    }

    private boolean setAndWriteDeviceConfig(String deviceId, HttpServletResponse response, FromJson json,
            boolean isHTTPPut) {

        try {
            if (isHTTPPut) {
                return setAndWriteHttpPutDeviceConfig(deviceId, response, json);
            }
            else {
                return setAndWriteHttpPostDeviceConfig(deviceId, response, json);
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
        } catch (IllegalStateException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_CONFLICT, logger, e.getMessage());
        }
        return false;
    }

    private boolean setAndWriteHttpPutDeviceConfig(String deviceId, HttpServletResponse response, FromJson json)
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

    private boolean setAndWriteHttpPostDeviceConfig(String deviceId, HttpServletResponse response, FromJson json)
            throws JsonSyntaxException, ConfigWriteException, RestConfigIsNotCorrectException, Error,
            MissingJsonObjectException, IllegalStateException {

        boolean ok = false;
        DriverConfig driverConfig;

        DeviceConfig deviceConfig = rootConfig.getDevice(deviceId);

        JsonObject jso = json.getJsonObject();
        String driverID = jso.get(Const.DRIVER).getAsString();

        if (driverID != null) {
            driverConfig = rootConfig.getDriver(driverID);
        }
        else {
            throw new Error("No driver ID in JSON");
        }

        if (driverConfig == null) {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                    "Driver does not exists: ", driverID);
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

    private void setConfigAccess() {
        this.dataAccess = handleDataAccessService(null);
        this.configService = handleConfigService(null);
        this.rootConfig = handleRootConfig(null);
    }

}
