/*
 * Copyright 2011-2021 Fraunhofer ISE
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

import java.text.MessageFormat;

public class DriverInfo {

    protected String id;
    protected String name;
    protected String description;

    protected final DeviceInfo device;

    protected DriverInfo(DeviceInfo device) {
        this.device = device;
    }

    protected DriverInfo(DeviceInfo device, String id, String name, String description) {
        this(device);
        this.id = id;
        this.setName(name);
        this.setDescription(description);
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
     * @param deviceScanSettingsSyntax
     *            device scan settings syntax
     * @param channelAddressSyntax
     *            channel address syntax
     * @param channelSettingsSyntax
     *            channel settings syntax
     * @param channelScanSettingsSyntax
     *            channel scan settings syntax
     */
    public DriverInfo(String id, String description, 
            String deviceAddressSyntax, String deviceSettingsSyntax, String deviceScanSettingsSyntax, 
            String channelAddressSyntax, String channelSettingsSyntax, String channelScanSettingsSyntax) {
        this.id = id;
        this.name = null;
        this.description = description;
        
        this.device = new DeviceInfo.StaticInfo(deviceAddressSyntax, deviceSettingsSyntax, deviceScanSettingsSyntax, 
                      new ChannelInfo.StaticInfo(channelAddressSyntax, channelSettingsSyntax, channelScanSettingsSyntax));
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
     * @param deviceScanSettingsSyntax
     *            device scan settings syntax
     * @param channelAddressSyntax
     *            channel address syntax
     */
    public DriverInfo(String id, String description, 
            String deviceAddressSyntax, String deviceSettingsSyntax, String channelAddressSyntax, 
            String deviceScanSettingsSyntax) {
        this.id = id;
        this.name = null;
        this.description = description;
        
        this.device = new DeviceInfo.StaticInfo(deviceAddressSyntax, deviceSettingsSyntax, deviceScanSettingsSyntax, 
                      new ChannelInfo.StaticInfo(channelAddressSyntax));
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

    public DriverInfo setName(String name) {
    	if (name == null || name.isEmpty()) {
            String id = getId();
            name = id.substring(0, 1).toUpperCase() + 
                   id.substring(1, id.length());
    	}
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DriverInfo setDescription(String description) {
    	if (description == null || description.isEmpty()) {
    		description = MessageFormat.format("Driver implementation for the {0} protocol", getName());
    	}
        this.description = description;
        return this;
    }

    public DeviceInfo getDevice() {
        return device;
    }

    public ChannelInfo getChannel() {
        return device.getChannel();
    }

}
