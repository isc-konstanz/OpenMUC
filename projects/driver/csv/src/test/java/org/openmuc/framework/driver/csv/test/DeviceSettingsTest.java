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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.csv.test.utils.CsvTestFile;
import org.openmuc.framework.driver.spi.ConnectionException;

public class DeviceSettingsTest {

    private static final String DEVICE_ADDRESS = System.getProperty("user.dir") + "/src/test/resources/test_data.csv";

    @Test
    public void testWrongSamplingMode() throws ConnectionException, ArgumentSyntaxException {

        String deviceSettings = "samplingmode=hhmmss2";

        Assertions.assertThrows(ArgumentSyntaxException.class,
                () -> new CsvTestFile(DEVICE_ADDRESS, deviceSettings));
    }
}
