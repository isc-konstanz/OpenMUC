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
package org.openmuc.framework.driver.csv.test;

import org.junit.jupiter.api.Test;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.driver.csv.CsvDriver;

public class DriverInfoTest {

    @Test
    public void printDriverInfo() {
        CsvDriver driver = new CsvDriver();
        DriverInfo info = driver.getInfo();

        System.out.println("Id: " + info.getId());
        System.out.println("Description: " + info.getDescription());
        System.out.println("DeviceAddressSyntax: " + info.getDevice().getAddressSyntax());
        System.out.println("DeviceSettingsSyntax: " + info.getDevice().getSettingsSyntax());
        System.out.println("DeviceScanSettingsSyntax: " + info.getDevice().getScanSettingsSyntax());
        System.out.println("ChannelAddressSyntax: " + info.getChannel().getAddressSyntax());
    }

}
