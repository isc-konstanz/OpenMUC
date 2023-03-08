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
package org.openmuc.framework.driver.revpi.dio;

import java.io.IOException;

import org.clehne.revpi.dataio.DataInOut;
import org.openmuc.framework.driver.DriverActivator;
import org.openmuc.framework.driver.annotation.Driver;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DriverService.class)
@Driver(id = RevPiDriver.ID,
        name = RevPiDriver.NAME, description = RevPiDriver.DESCRIPTION,
        device = RevPiDigitalIoDevice.class)
public class RevPiDriver extends DriverActivator {

    private static final Logger logger = LoggerFactory.getLogger(RevPiDriver.class);

    public static final String ID = "revpi";
    public static final String NAME = "IO (Revolution Pi)";
    public static final String DESCRIPTION = 
            "This driver enables the access to the variety of channels of the Revolution Pi platform. " +
            "Devices represent the Digital Input/Output (IO) Boards of the Revolution Pi.";

	DataInOut data;

    @Activate
    public void activate() {
        data = new DataInOut();
    }

    @Deactivate
    public void deactivate() {
		try {
			data.close();
			
		} catch (IOException e) {
			logger.warn("Exception on closing driver: " + e.getMessage());
		}
		this.data = null;
    }

}
