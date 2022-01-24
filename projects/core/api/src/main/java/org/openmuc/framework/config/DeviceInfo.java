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

public interface DeviceInfo {

    public static class StaticInfo implements DeviceInfo {

        private final String addressSyntax;
        private final String settingsSyntax;

        private final String scanSettingsSyntax;

        private final ChannelInfo channel;

        public StaticInfo(String addressSyntax, String settingsSyntax, String scanSettingsSyntax,
                ChannelInfo channel) {
            this.channel = channel;
            this.addressSyntax = addressSyntax;
            this.settingsSyntax = settingsSyntax;
            this.scanSettingsSyntax = scanSettingsSyntax;
        }

        @Override
        public String getAddressSyntax() {
            return addressSyntax;
        }

        @Override
        public String getSettingsSyntax() {
            return settingsSyntax;
        }

        @Override
        public String getScanSettingsSyntax() {
            return scanSettingsSyntax;
        }

        @Override
        public ChannelInfo getChannel() {
            return channel;
        }

    }

    public String getAddressSyntax();

    public String getSettingsSyntax();

    public String getScanSettingsSyntax();

    public ChannelInfo getChannel();

}
