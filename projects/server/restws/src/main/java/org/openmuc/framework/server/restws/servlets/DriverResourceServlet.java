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
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.ConfigWriteException;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverNotAvailableException;
import org.openmuc.framework.config.IdCollisionException;
import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.lib.json.Const;
import org.openmuc.framework.lib.json.FromJson;
import org.openmuc.framework.lib.json.ToJson;
import org.openmuc.framework.lib.json.exceptions.MissingJsonObjectException;
import org.openmuc.framework.lib.json.exceptions.RestConfigIsNotCorrectException;
import org.openmuc.framework.lib.json.rest.objects.RestDriverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class DriverResourceServlet extends GenericServlet {

    private static final String REQUESTED_REST_PATH_IS_NOT_AVAILABLE = "Requested rest path is not available.";
    private static final String REQUESTED_ID_IS_NOT_AVAILABLE = "Requested driver is not available";
    private static final String REST_PATH = " Rest path = ";
    private static final String REST_ID = " Driver ID = ";
    private static final String APPLICATION_JSON = "application/json";
    private static final long serialVersionUID = -2223282905555493215L;

    private static final Logger logger = LoggerFactory.getLogger(DriverResourceServlet.class);

    private DataAccessService dataAccess;
    private ConfigService configService;
    private RootConfig rootConfig;

    private DeviceScanListenerImplementation scanListener = new DeviceScanListenerImplementation();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        java.util.Date time = new java.util.Date(request.getSession().getLastAccessedTime());

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

            response.setStatus(HttpServletResponse.SC_OK);

            ToJson json = new ToJson();

            if (pathInfo.equals("/")) {
                boolean details = Boolean.parseBoolean(request.getParameter("details"));
                if (details) {
                    doGetDriverList(json);
                }
                else {
                    doGetDriverIdList(json);
                }
            }
            else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.RUNNING)) {
                doGetRunningList(json);
            }
            else if (pathInfoArray.length == 1 && pathInfoArray[0].equalsIgnoreCase(Const.CONFIGS)) {
                doGetConfigsList(json);
            }
            else if (pathInfoArray.length > 1 && pathInfoArray[1].equalsIgnoreCase(Const.INFOS)) {
                doGetInfo(request, response, pathInfoArray, json);
            }
            else {
                doGetDriver(request, response, pathInfoArray, json);
            }
            sendJson(json, response);
        }
    }

    private void doGetDriverIdList(ToJson json) {
        List<String> driversList = getDriverIdList();

        json.addStringList(Const.DRIVERS, driversList);
    }

    private void doGetDriverList(ToJson json) {
        List<RestDriverWrapper> drivers = new LinkedList<RestDriverWrapper>();
        Collection<DriverConfig> driverConfigs = rootConfig.getDrivers();

        for (DriverConfig config : driverConfigs) {
            drivers.add(RestDriverWrapper.getDriver(config, configService, dataAccess));
        }
        json.addDriverList(drivers);
    }

    private void doGetRunningList(ToJson json) throws IOException {
        List<DriverInfo> drivers = new ArrayList<DriverInfo>();

        for (String driverId : configService.getIdsOfRunningDrivers()) {
            try {
                drivers.add(configService.getDriverInfo(driverId));

            } catch (DriverNotAvailableException e) {
                // Running drivers can't be unavailable. In the unlikely case, skip the driver
            }
        }
        json.addDriverInfoList(drivers);
    }

    private void doGetDriver(HttpServletRequest request, HttpServletResponse response, String[] pathInfoArray,
            ToJson json) throws IOException {

        String driverId = pathInfoArray[0].replace("/", "");

        List<String> driverList = getDriverIdList();
        if (driverList.contains(driverId)) {
            if (pathInfoArray.length == 1) {
                boolean details = Boolean.parseBoolean(request.getParameter("details"));
                if (details) {
                    doGetDriver(json, driverId);
                }
                else {
                    json.addRecordList(getChannelList(driverId));
                    json.addBoolean(Const.RUNNING, isRunning(driverId));
                }
            }
            else if (pathInfoArray.length > 1) {
                if (pathInfoArray[1].equalsIgnoreCase(Const.RUNNING)) {
                    json.addBoolean(Const.RUNNING, isRunning(driverId));
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.DEVICES)) {
                    json.addStringList(Const.DEVICES, getDeviceIdList(driverId));
                    json.addBoolean(Const.RUNNING, isRunning(driverId));
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.CHANNELS)) {
                    json.addChannelIdList(getChannelList(driverId));
                    json.addBoolean(Const.RUNNING, isRunning(driverId));
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.SCAN)) {
                    String settings = request.getParameter(Const.SETTINGS);
                    json.addDeviceScanInfoList(scanForAllDevices(driverId, settings, response));
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.SCAN_START)) {
                    String settings = request.getParameter(Const.SETTINGS);
                    scanForAllDevicesAsync(driverId, settings, response);
                    json.addDeviceScanProgressInfo(scanListener.getRestScanProgressInfo());
                    json.addDeviceScanInfoList(scanListener.getScannedDevicesList());
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.SCAN_PROGRESS)) {
                    json.addDeviceScanProgressInfo(scanListener.getRestScanProgressInfo());
                    json.addDeviceScanInfoList(scanListener.getScannedDevicesList());
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.SCAN_PROGRESS_INFO)) {
                    json.addDeviceScanProgressInfo(scanListener.getRestScanProgressInfo());
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS) && pathInfoArray.length == 2) {
                    doGetConfigs(json, driverId, response);
                }
                else if (pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS) && pathInfoArray.length == 3) {
                    doGetConfigField(json, driverId, pathInfoArray[2], response);
                }
                else {
                    ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                            REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
                }
            }
            else {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                		REQUESTED_ID_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }
        }
        else {
            driverNotAvailable(response, driverId);
        }
    }

    private void doGetDriver(ToJson json, String driverId) {
        DriverConfig driverConfig = rootConfig.getDriver(driverId);

        json.addDriver(RestDriverWrapper.getDriver(driverConfig, configService, dataAccess));
    }

    private void doGetConfigsList(ToJson json) {
        List<DriverConfig> driverConfigs = new ArrayList<DriverConfig>(rootConfig.getDrivers());
        json.addDriverConfigList(driverConfigs);
    }

    private void doGetConfigs(ToJson json, String driverId, HttpServletResponse response) {
        DriverConfig driverConfig = rootConfig.getDriver(driverId);

        if (driverConfig != null) {
            json.addDriverConfig(driverConfig);
        }
        else {
            driverNotAvailable(response, driverId);
        }
    }

    private void doGetConfigField(ToJson json, String driverId, String configField, HttpServletResponse response)
            throws IOException {
        DriverConfig driverConfig = rootConfig.getDriver(driverId);

        if (driverConfig != null) {
            JsonObject jsoConfigAll = ToJson.getDriverConfigAsJsonObject(driverConfig);
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
            driverNotAvailable(response, driverId);
        }
    }

    private void doGetInfo(HttpServletRequest request, HttpServletResponse response, String[] pathInfoArray,
            ToJson json) throws IOException {

        String driverId = pathInfoArray[0].replace("/", "");

        try {
            DriverInfo driverInfo = configService.getDriverInfo(driverId);
            if (pathInfoArray.length == 2) {
                json.addDriverSyntax(driverInfo);
            }
            if (pathInfoArray.length > 2 && pathInfoArray[2].equalsIgnoreCase(Const.OPTIONS)) {
                String filter = request.getParameter("filter");
                if (filter != null && !filter.isEmpty()) {
                    switch(filter) {
                    case Const.DRIVER:
                        json.addDriverInfo(driverInfo);
                        break;
                    case Const.DEVICE:
                        json.addDeviceInfo(driverInfo);
                        break;
                    case Const.CHANNEL:
                        json.addChannelInfo(driverInfo);
                        break;
                    default:
                        ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                            "Unknown driver info filter: ", filter, ",", REST_ID, driverId);
                        
                        break;
                    }
                }
                else {
                    json.addDriverOptions(driverInfo);
                }
            }
        } catch (DriverNotAvailableException | ParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        java.util.Date time = new java.util.Date(request.getSession().getLastAccessedTime());

        if (pathAndQueryString == null) {
            return;
        }

        setConfigAccess();

        String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];

        String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
        String driverId = pathInfoArray[0].replace("/", "");

        String json = ServletLib.getJsonText(request);

        if (pathInfoArray.length < 1) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                    REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
        }
        else {
            DriverConfig driverConfig = rootConfig.getDriver(driverId);

            if (driverConfig != null && pathInfoArray.length == 2 && pathInfoArray[1].equalsIgnoreCase(Const.CONFIGS)) {
                doSetConfigs(driverId, response, json);
            }
            else if (driverConfig != null && pathInfoArray.length == 2
                    && pathInfoArray[1].equalsIgnoreCase(Const.SCAN_INTERRUPT)) {
                interruptScanProcess(driverId, response, json);
            }
            else {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        java.util.Date time = new java.util.Date(request.getSession().getLastAccessedTime());

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];

            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
            String driverId = pathInfoArray[0].replace("/", "");

            String json = ServletLib.getJsonText(request);

            if (pathInfoArray.length != 1) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }
            else {
                try {
                    rootConfig.addDriver(driverId);
                    configService.setConfig(rootConfig);
                    configService.writeConfigToFile();

                    doSetConfigs(driverId, response, json);

                } catch (IdCollisionException e) {
                    ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_CONFLICT, logger,
                            "Driver \"" + driverId + "\" already exist");
                } catch (ConfigWriteException e) {
                    ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                            "Could not write driver \"", driverId, "\".");
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized boolean doSetConfigs(String driverId, HttpServletResponse response, String json) {
        boolean ok = false;

        try {
            DriverConfig driverConfig = rootConfig.getDriver(driverId);
            if (driverConfig != null) {
                try {
                    FromJson fromJson = new FromJson(json);
                    fromJson.setDriverConfig(driverConfig, driverId);
                } catch (IdCollisionException e) {

                }
                configService.setConfig(rootConfig);
                configService.writeConfigToFile();
                response.setStatus(HttpServletResponse.SC_OK);
                ok = true;
            }
            else {
                ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                        "Not able to access to driver ", driverId);
            }
        } catch (JsonSyntaxException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_CONFLICT, logger,
                    "JSON syntax is wrong.");
        } catch (MissingJsonObjectException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger, e.getMessage());
        } catch (ConfigWriteException e) {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                    "Could not write driver \"", driverId, "\".");
            logger.debug(e.getMessage());
        } catch (RestConfigIsNotCorrectException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    "Not correct formed driver config json.", " JSON = ", json);
        } catch (IllegalStateException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_CONFLICT, logger, e.getMessage());
        }
        return ok;
    }

    private void interruptScanProcess(String driverId, HttpServletResponse response, String json) {
        try {
            configService.interruptDeviceScan(driverId);
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (UnsupportedOperationException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Driver does not support scan interrupting.", REST_ID, driverId);
        } catch (DriverNotAvailableException e) {
            driverNotAvailable(response, driverId);
        }
    }

    private DeviceScanListenerImplementation scanForAllDevicesAsync(String driverId, String settings, HttpServletResponse response) {
        try {
            scanListener = new DeviceScanListenerImplementation();
            configService.scanForDevices(driverId, settings, scanListener);
            
        } catch (UnsupportedOperationException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Driver does not support scanning.", REST_ID, driverId);
        } catch (DriverNotAvailableException e) {
            driverNotAvailable(response, driverId);
        }
        return scanListener;
    }

    private List<DeviceScanInfo> scanForAllDevices(String driverId, String settings, HttpServletResponse response) {
        return scanForAllDevicesAsync(driverId, settings, response).getScannedDevicesResult();
    }

    @SuppressWarnings("unused")
    private List<DeviceScanInfo> scanForAllDrivers(String driverId, String settings, HttpServletResponse response) {
        List<DeviceScanInfo> scannedDevicesList = new ArrayList<>();
        
        try {
            scannedDevicesList = configService.scanForDevices(driverId, settings);

        } catch (UnsupportedOperationException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Driver does not support scanning.", REST_ID, driverId);
        } catch (DriverNotAvailableException e) {
            driverNotAvailable(response, driverId);
        } catch (ArgumentSyntaxException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    "Argument syntax was wrong.", REST_ID, driverId, " Settings = ", settings);
        } catch (ScanException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Error while scan driver devices", REST_ID, driverId, " Settings = ", settings);
        } catch (ScanInterruptedException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                    "Scan interrupt occured", REST_ID, driverId, " Settings = ", settings);
        }

        return scannedDevicesList;
    }

    @Override
    public synchronized void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        java.util.Date time = new java.util.Date(request.getSession().getLastAccessedTime());

        if (pathAndQueryString != null) {

            setConfigAccess();

            String pathInfo = pathAndQueryString[0];
            String driverId = null;

            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
            driverId = pathInfoArray[0].replace("/", "");

            DriverConfig driverConfig = rootConfig.getDriver(driverId);

            if (pathInfoArray.length != 1) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        REQUESTED_REST_PATH_IS_NOT_AVAILABLE, REST_PATH, request.getPathInfo());
            }
            else if (driverConfig == null) {
                ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
                        "Driver \"" + driverId + "\" does not exist.");
            }
            else {
                try {
                    driverConfig.delete();
                    configService.setConfig(rootConfig);
                    configService.writeConfigToFile();

                    if (rootConfig.getDriver(driverId) == null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    else {
                        ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                logger, "Not able to delete driver ", driverId);
                    }
                } catch (ConfigWriteException e) {
                    ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, logger,
                            "Not able to write into config.");
                    e.printStackTrace();
                }
            }
        }
    }

    private List<String> getDriverIdList() {
        List<String> driverList = new ArrayList<>();
        Collection<DriverConfig> driverConfigs = rootConfig.getDrivers();

        for (DriverConfig driverConfig : driverConfigs) {
            driverList.add(driverConfig.getId());
        }
        return driverList;
    }

    private List<String> getDeviceIdList(String driverId) {
        List<String> deviceList = new ArrayList<>();

        DriverConfig driverConfig = rootConfig.getDriver(driverId);
        Collection<DeviceConfig> deviceConfigs = driverConfig.getDevices();
        for (DeviceConfig deviceConfig : deviceConfigs) {
            deviceList.add(deviceConfig.getId());
        }
        return deviceList;
    }

    private List<Channel> getChannelList(String driverId) {
        List<Channel> driverChannels = new ArrayList<>();

        DriverConfig driverConfig = rootConfig.getDriver(driverId);
        Collection<DeviceConfig> deviceConfigs = driverConfig.getDevices();
        for (DeviceConfig deviceConfig : deviceConfigs) {
            Collection<ChannelConfig> channelConfigs = deviceConfig.getChannels();
            for (ChannelConfig channelConfig : channelConfigs) {
                driverChannels.add(dataAccess.getChannel(channelConfig.getId()));
            }
        }
        return driverChannels;
    }

    private boolean isRunning(String driverId) {
        return configService.getIdsOfRunningDrivers().contains(driverId);
    }

    private static void driverNotAvailable(HttpServletResponse response, String driverId) {
        ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_FOUND, logger,
        		REQUESTED_ID_IS_NOT_AVAILABLE, REST_ID, driverId);
    }

    private void setConfigAccess() {
        this.dataAccess = handleDataAccessService(null);
        this.configService = handleConfigService(null);
        this.rootConfig = handleRootConfig(null);
    }

}
