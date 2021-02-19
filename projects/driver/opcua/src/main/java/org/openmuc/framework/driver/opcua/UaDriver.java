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
package org.openmuc.framework.driver.opcua;

import org.openmuc.framework.driver.DeviceFactory.Factory;
import org.openmuc.framework.driver.Driver;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;

@Component
@Factory(device = UaConnection.class)
public class UaDriver extends Driver implements DriverService {

    private static final String ID = "opcua";
    private static final String NAME = "OPC UA";
    private static final String DESCRIPTION = "OPC Unified Architecture (OPC UA) "
            + "is a machine to machine communication protocol for industrial automation "
            + "developed by the OPC Foundation.";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

}
