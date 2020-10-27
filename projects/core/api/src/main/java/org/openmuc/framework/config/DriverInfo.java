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
package org.openmuc.framework.config;

public class DriverInfo {

    protected String id;
    protected String name;
    protected String description;
    protected String deviceAddressSyntax;
    protected String deviceSettingsSyntax;
    protected String deviceScanSettingsSyntax;
    protected String channelAddressSyntax;
    protected String channelSettingsSyntax;
    protected String channelScanSettingsSyntax;

    protected DriverInfo() {
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
        this.description = description;
        this.deviceAddressSyntax = deviceAddressSyntax;
        this.deviceSettingsSyntax = deviceSettingsSyntax;
        this.deviceScanSettingsSyntax = deviceScanSettingsSyntax;
        this.channelAddressSyntax = channelAddressSyntax;
        this.channelSettingsSyntax = channelSettingsSyntax;
        this.channelScanSettingsSyntax = channelScanSettingsSyntax;
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
        this.description = description;
        this.deviceAddressSyntax = deviceAddressSyntax;
        this.deviceSettingsSyntax = deviceSettingsSyntax;
        this.deviceScanSettingsSyntax = deviceScanSettingsSyntax;
        this.channelAddressSyntax = channelAddressSyntax;
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

    public String getDescription() {
        return description;
    }

    public String getDeviceAddressSyntax() {
        return deviceAddressSyntax;
    }

    public String getDeviceSettingsSyntax() {
        return deviceSettingsSyntax;
    }

    public String getDeviceScanSettingsSyntax() {
        return deviceScanSettingsSyntax;
    }

    public String getChannelAddressSyntax() {
        return channelAddressSyntax;
    }

    public String getChannelSettingsSyntax() {
        return channelSettingsSyntax;
    }

    public String getChannelScanSettingsSyntax() {
        return channelScanSettingsSyntax;
    }

}
