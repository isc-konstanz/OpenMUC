/*
 * Copyright 2011-2022 Fraunhofer ISE
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

public interface ChannelInfo {

    public static class StaticInfo implements ChannelInfo {
    
        private String addressSyntax;
        private String settingsSyntax;

        private String scanSettingsSyntax;

        public StaticInfo(String addressSyntax) {
            this.addressSyntax = addressSyntax;
        }

        public StaticInfo(String addressSyntax, String settingsSyntax, String scanSettingsSyntax) {
            this.addressSyntax = addressSyntax;
            this.settingsSyntax = settingsSyntax;
            this.scanSettingsSyntax = scanSettingsSyntax;
        }

        public String getAddressSyntax() {
            return addressSyntax;
        }

        public String getSettingsSyntax() {
            return settingsSyntax;
        }

        public String getScanSettingsSyntax() {
            return scanSettingsSyntax;
        }

    }

    public String getAddressSyntax();

    public String getSettingsSyntax();

    public String getScanSettingsSyntax();

}
