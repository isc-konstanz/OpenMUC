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
import org.openmuc.framework.driver.csv.settings.DeviceScanSettings;

public class DeviceScanSettingsTest {

    // Tests expected to be OK

    String dir = System.getProperty("user.dir");

    @Test
    public void testArgumentCorrectEndingWithSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources/";
        new DeviceScanSettings(settings);
    }

    @Test
    public void testArgumentCorrectEndingWithoutSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources";
        new DeviceScanSettings(settings);
    }

    // Tests expected to FAIL

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsNull() throws ArgumentSyntaxException {
        String arguments = null;
        new DeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsEmptyString() throws ArgumentSyntaxException {
        String arguments = "";
        new DeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgument() throws ArgumentSyntaxException {
        String arguments = "paaaaath";
        new DeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete1() throws ArgumentSyntaxException {
        String arguments = "path";
        new DeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete2() throws ArgumentSyntaxException {
        String arguments = "path=";
        new DeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgumentPathDoesNotExist() throws ArgumentSyntaxException {
        String arguments = "path=/home/does_not_exist";
        new DeviceScanSettings(arguments);
    }

}
