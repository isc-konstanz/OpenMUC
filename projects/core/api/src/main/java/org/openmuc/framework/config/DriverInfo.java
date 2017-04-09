/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.config;

import org.openmuc.framework.config.info.ChannelSyntax;
import org.openmuc.framework.config.info.DeviceSyntax;
import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.config.info.OptionSelection;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.ValueType;

public class DriverInfo {

    private final String id;
    private final String name;
    private final String description;
    private final DeviceInfo deviceInfo;
    private final ChannelInfo channelInfo;
    
    /**
     * Constructor to set driver info
     * 
     * @param id
     *            driver ID
     * @param name
     *            driver name
     * @param description
     *            driver description
     * @param deviceInfo
     *            device description, address, settings and scan settings syntax
     * @param channelInfo
     *            channel description, address and scan settings syntax
     */
    public DriverInfo(String id, String name, String description,
            DeviceInfo deviceInfo, ChannelInfo channelInfo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deviceInfo = deviceInfo;
        this.channelInfo = channelInfo;
    }
    
    /**
     * Constructor to set driver info
     * 
     * @param id
     *            driver ID
     * @param description
     *            driver description
     * @param deviceAddressSyntax
     *            device address syntax
     * @param deviceSettingsSyntax
     *            device settings syntax
     * @param channelAddressSyntax
     *            channel address syntax
     * @param deviceScanSettingsSyntax
     *            device scan settings syntax
     */
    public DriverInfo(String id, String description, 
            String deviceAddressSyntax, String deviceSettingsSyntax,
            String channelAddressSyntax, String deviceScanSettingsSyntax) {
        this(id, null, description, 
                new DeviceSyntax(deviceAddressSyntax, deviceSettingsSyntax, deviceScanSettingsSyntax), 
                new ChannelSyntax(channelAddressSyntax, null));
    }

    /**
     * Returns the ID of the driver. The ID may only contain ASCII letters, digits, hyphens and underscores. By
     * convention the ID should be meaningful and all lower case letters (e.g. "mbus", "modbus").
     * 
     * @return the unique ID of the driver.
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public String getDeviceAddressSyntax() {
        return deviceInfo.getAddressSyntax();
    }

    public String getDeviceSettingsSyntax() {
        return deviceInfo.getSettingsSyntax();
    }

    public String getDeviceScanSettingsSyntax() {
        return deviceInfo.getScanSettingsSyntax();
    }
    
    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public String getChannelAddressSyntax() {
        return channelInfo.getAddressSyntax();
    }

    public String getChannelScanSettingsSyntax() {
        return channelInfo.getScanSettingsSyntax();
    }

    public static OptionCollection config() {
        
        OptionCollection config = new OptionCollection();
        config.add(samplingTimeout());
        config.add(connectRetryInterval());

        return OptionCollection.unmodifiableOptions(config);
    }

    private static Option samplingTimeout() {
        
        Option samplingTimeout = new Option("samplingTimeout", "Sampling timeout", ValueType.INTEGER);
        samplingTimeout.setDescription("Default time waited for a read operation of any Device to complete, "
                + "if the Device doesn’t set a sampling timeout on its own.");
        samplingTimeout.setMandatory(false);
        samplingTimeout.setDefaultValue(new IntValue(0));
        samplingTimeout.setValueSelection(OptionSelection.timeSelection());
        
        return samplingTimeout;
    }

    private static Option connectRetryInterval() {
        
        Option connectRetryInterval = new Option("connectRetryInterval", "Connect retry interval", ValueType.INTEGER);
        connectRetryInterval.setDescription("Default time waited until a failed connection attempt of any Device is repeated, "
                + "if the Device doesn’t set a connect retry interval on its own.");
        connectRetryInterval.setMandatory(false);
        connectRetryInterval.setDefaultValue(new IntValue(60000));
        connectRetryInterval.setValueSelection(OptionSelection.timeSelection());
        
        return connectRetryInterval;
    }

}
