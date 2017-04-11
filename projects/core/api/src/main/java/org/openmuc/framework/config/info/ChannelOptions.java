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
package org.openmuc.framework.config.info;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelInfo;

public abstract class ChannelOptions extends ChannelInfo {
    
    private final OptionCollection address;
    private final OptionCollection scanSettings;

    public ChannelOptions() {
        
        this.address = configureAddressOptions();
        this.scanSettings = configureScanSettingsOptions();
    }

    public abstract String getDescription();

    private OptionCollection configureAddressOptions() {

        OptionCollection address = new OptionCollection();
        try {
            configureAddress(address);
            return OptionCollection.unmodifiableOptions(address);
        }
        catch (UnsupportedOperationException e) {
            return null;
        }
    }

    protected abstract void configureAddress(OptionCollection address) throws UnsupportedOperationException;

    public Settings parseAddress(String address) throws ArgumentSyntaxException {
        return this.address.parse(address);
    }

    public OptionCollection getAddress() {
        return this.address;
    }

    @Override
    public String getAddressSyntax() {
        return this.address.syntax();
    }

    private OptionCollection configureScanSettingsOptions() {

        OptionCollection scanSettings = new OptionCollection();
        try {
            configureScanSettings(scanSettings);
            return OptionCollection.unmodifiableOptions(scanSettings);
        }
        catch (UnsupportedOperationException e) {
            return null;
        }
    }

    protected abstract void configureScanSettings(OptionCollection scanSettings) throws UnsupportedOperationException;

    public Settings parseScanSettings(String scanSettings) throws ArgumentSyntaxException {
        return this.scanSettings.parse(scanSettings);
    }
    
    public OptionCollection getScanSettings() {
        return this.scanSettings;
    }

    @Override
    public String getScanSettingsSyntax() {
        return this.scanSettings.syntax();
    }

}
