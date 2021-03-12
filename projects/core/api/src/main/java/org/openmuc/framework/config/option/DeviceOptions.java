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
package org.openmuc.framework.config.option;

import java.io.IOException;

import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.DeviceInfo;
import org.openmuc.framework.config.ParseException;

public interface DeviceOptions extends DeviceInfo {

    @Override
    public default String getAddressSyntax() {
        Options address = getAddressOptions();
        if (address == null) {
            return null;
        }
        return address.getSyntax();
    }

    @Override
    public default String getSettingsSyntax() {
        Options settings = getSettingsOptions();
        if (settings == null) {
            return null;
        }
        return settings.getSyntax();
    }

    @Override
    public default String getScanSettingsSyntax() {
        Options settings = getScanSettingsOptions();
        if (settings == null) {
            return null;
        }
        return settings.getSyntax();
    }

    public Options getAddressOptions();

    public Options getSettingsOptions();

    public Options getScanSettingsOptions();

    public default Options getConfigOptions() throws ParseException, IOException {
        return readConfigOptions();
    }

    public static Options readConfigOptions() throws ParseException, IOException {
        return DriverOptions.readConfigs("device");
    }

    public static class DeviceConfigs implements DeviceOptions {

        private Options address;
        private Options settings;

        private Options scanSettings;

        private ChannelOptions channel;

        protected DeviceConfigs(ChannelOptions channel) {
            this.channel = channel;
        }

        @Override
        public Options getAddressOptions() {
            return address;
        }

        DeviceOptions setAddress(Class<? extends Configurable> address) {
            setAddressOptions(Options.parseAddress(address));
            return this;
        }

        DeviceOptions setAddressOptions(Options address) {
            this.address = address;
            return this;
        }

        @Override
        public Options getSettingsOptions() {
            return settings;
        }

        DeviceOptions setSettings(Class<? extends Configurable> settings) {
            setSettingsOptions(Options.parseSettings(settings));
            return this;
        }

        DeviceOptions setSettingsOptions(Options settings) {
            this.settings = settings;
            return this;
        }

        @Override
        public Options getScanSettingsOptions() {
            return scanSettings;
        }

        DeviceOptions setScanSettings(Class<? extends Configurable> scanSettings) {
            setScanSettingsOptions(Options.parseSettings(scanSettings));
            return this;
        }

        DeviceOptions setScanSettingsOptions(Options scanSettings) {
            this.scanSettings = scanSettings;
            return this;
        }

        @Override
        public ChannelOptions getChannel() {
            return channel;
        }

    }

}
