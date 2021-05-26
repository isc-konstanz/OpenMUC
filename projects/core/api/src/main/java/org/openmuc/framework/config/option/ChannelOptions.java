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

import org.openmuc.framework.config.ChannelInfo;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.ParseException;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

public interface ChannelOptions extends ChannelInfo {

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

    public static class ChannelConfigs implements ChannelOptions {

        private Options address;
        private Options settings;

        private Options scanSettings;

        protected ChannelConfigs() {
        }

        @Override
        public Options getAddressOptions() {
            return address;
        }

        ChannelOptions setAddress(Class<? extends Configurable> configurable) {
            setAddressOptions(Options.parse(ADDRESS, configurable));
            return this;
        }

        ChannelOptions setAddressOptions(Options address) {
            this.address = address;
            return this;
        }

        @Override
        public Options getSettingsOptions() {
            return settings;
        }

        ChannelOptions setSettings(Class<? extends Configurable> configurable) {
            setSettingsOptions(Options.parse(SETTING, configurable));
            return this;
        }

        ChannelOptions setSettingsOptions(Options settings) {
            this.settings = settings;
            return this;
        }

        @Override
        public Options getScanSettingsOptions() {
            return scanSettings;
        }

        ChannelOptions setScanSettings(Class<? extends Configurable> configurable) {
            setSettingsOptions(Options.parse(SETTING, configurable));
            return this;
        }

        ChannelOptions setScanSettingsOptions(Options scanSettings) {
            this.scanSettings = scanSettings;
            return this;
        }

    }

}
