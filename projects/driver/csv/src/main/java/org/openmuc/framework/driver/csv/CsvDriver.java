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
package org.openmuc.framework.driver.csv;

import org.openmuc.framework.driver.DriverActivator;
import org.openmuc.framework.driver.annotation.Driver;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;

/**
 * Driver to read data from CSV file.
 * <p>
 * Three sampling modes are available:
 * <ul>
 * <li>LINE: starts from begin of file. With every sampling it reads the next line. Timestamps ignored</li>
 * <li>UNIXTIMESTAMP: With every sampling it reads the line with the closest unix timestamp regarding to sampling
 * timestamp</li>
 * <li>HHMMSS: With every sampling it reads the line with the closest time HHMMSS regarding to sampling timestamp</li>
 * </ul>
 */
@Component
@Driver(id = CsvDriver.ID, 
        name = CsvDriver.NAME, description = CsvDriver.DESCRIPTION,
        device = CsvFile.class, scanner = CsvScanner.class)
public class CsvDriver extends DriverActivator implements DriverService {

    public static final String ID = "csv";
    public static final String NAME = "CSV";
    public static final String DESCRIPTION = "The CSV Driver reads out values from configured files. "
            + "Each device represents a specific file which may contain several columns, addressed by their headers. "
            + "Rows in those columns are read either line by line or by a defined index.";

}
