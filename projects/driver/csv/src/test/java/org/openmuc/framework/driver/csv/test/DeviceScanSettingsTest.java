/*
 * Copyright 2011-18 Fraunhofer ISE
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

import org.junit.Test;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.csv.CsvDriver;
import org.openmuc.framework.driver.csv.settings.DeviceScanSettings;

public class DeviceScanSettingsTest {

    private final DriverInfo info = DriverInfoFactory.getInfo(CsvDriver.class);

    // Tests expected to be OK

    String dir = System.getProperty("user.dir");

    @Test
    public void testArgumentCorrectEndingWithSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources";
        info.parse(settings, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test
    public void testArgumentCorrectendingWithoutSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources/";
        info.parse(settings, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    // Tests expected to FAIL

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsNull() throws ArgumentSyntaxException {
        String arguments = null;
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsEmptyString() throws ArgumentSyntaxException {
        String arguments = "";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgument() throws ArgumentSyntaxException {
        String arguments = "paaaaath";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete1() throws ArgumentSyntaxException {
        String arguments = "path";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete2() throws ArgumentSyntaxException {
        String arguments = "path=";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgumentPathDoesNotExist() throws ArgumentSyntaxException, UnsupportedOperationException, ScanException, ScanInterruptedException {
        String arguments = "path=/home/does_not_exist";
        info.parse(arguments, DeviceScanSettings.class).listFiles();
    }

}
