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
package org.openmuc.framework.lib.json.restObjects;

public class RestDriverSyntax {

    private String id;
    private String description;
    private String deviceAddressSyntax;
    private String deviceSettingsSyntax;
    private String deviceScanSettingsSyntax;
    private String channelAddressSyntax;
    private String channelScanSettingsSyntax;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceAddressSyntax() {
        return deviceAddressSyntax;
    }

    public void setDeviceAddressSyntax(String deviceAddressSyntax) {
        this.deviceAddressSyntax = deviceAddressSyntax;
    }

    public String getDeviceSettingsSyntax() {
        return deviceSettingsSyntax;
    }

    public void setDeviceSettingsSyntax(String deviceSettingsSyntax) {
        this.deviceSettingsSyntax = deviceSettingsSyntax;
    }

    public String getDeviceScanSettingsSyntax() {
        return deviceScanSettingsSyntax;
    }

    public void setDeviceScanSettingsSyntax(String deviceScanSettingsSyntax) {
        this.deviceScanSettingsSyntax = deviceScanSettingsSyntax;
    }

    public String getChannelAddressSyntax() {
        return channelAddressSyntax;
    }

    public void setChannelAddressSyntax(String channelAddressSyntax) {
        this.channelAddressSyntax = channelAddressSyntax;
    }

    public String getChannelScanSettingsSyntax() {
        return channelScanSettingsSyntax;
    }

    public void setChannelScanSettingsSyntax(String channelScanSettingsSyntax) {
        this.channelScanSettingsSyntax = channelScanSettingsSyntax;
    }

}
