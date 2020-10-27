/*
 * Copyright 2011-2020 Fraunhofer ISE
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
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.csv.CsvScanner;

public class DeviceScanSettingsTest {

    // Tests expected to be OK

    String dir = System.getProperty("user.dir");

    @Test
    public void testArgumentCorrectEndingWithSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources";
        new CsvScanner(settings);
    }

    @Test
    public void testArgumentCorrectendingWithoutSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources/";
        new CsvScanner(settings);
    }

    // Tests expected to FAIL

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsNull() throws ArgumentSyntaxException {
        String settings = null;
        new CsvScanner(settings);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsEmptyString() throws ArgumentSyntaxException {
        String settings = "";
        new CsvScanner(settings);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgument() throws ArgumentSyntaxException {
        String settings = "paaaaath";
        new CsvScanner(settings);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete1() throws ArgumentSyntaxException {
        String settings = "path";
        new CsvScanner(settings);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete2() throws ArgumentSyntaxException {
        String settings = "path=";
        new CsvScanner(settings);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgumentPathDoesNotExist() throws ArgumentSyntaxException, UnsupportedOperationException, ScanException, ScanInterruptedException {
        String settings = "path=/home/does_not_exist";
        new CsvScanner(settings);
    }

}
