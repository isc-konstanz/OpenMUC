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

/**
 * Class holding the information of a scanned device.
 * 
 */
public class DeviceScanInfo {

    private final String id;
    private final String address;
    private final String settings;
    private final String description;

    public DeviceScanInfo(String address, String settings, String description) {
        this("", address, settings, description);
    }

    public DeviceScanInfo(String id, String address, String settings, String description) {
        if (address == null) {
            throw new IllegalArgumentException("Device address must not be null.");
        }
        if (id == null || id.isEmpty()) {
            this.id = address.replaceAll("[^a-zA-Z0-9]+", "");
        }
        else {
            this.id = id;
        }

        this.address = address;

        if (settings == null) {
            this.settings = "";
        }
        else {
            this.settings = settings;
        }

        if (description == null) {
            this.description = "";
        }
        else {
            this.description = description;
        }

    }

    /**
     * Gets the ID. The ID is generated out of interface + device address. Special chars are omitted.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the device address
     * 
     * @return the device address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the settings
     * 
     * @return the settings
     */
    public String getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nDeviceScanInfo:\n  ID: ")
                .append(id)
                .append("\n  Address: ")
                .append(address)
                .append("\n  Settings: ")
                .append(settings)
                .append("\n  Description: ")
                .append(description);

        return sb.toString();
    }
}
