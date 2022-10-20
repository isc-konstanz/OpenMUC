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
package org.openmuc.framework.driver;

import org.openmuc.framework.config.option.DriverOptions;
import org.openmuc.framework.driver.annotation.Driver;

public class DriverContext extends DriverOptions {

    <D extends DriverDevice> DriverContext(DriverDeviceContext device, Driver driver) {
        super(device, 
              driver.id(),
              driver.name(),
              driver.description());
    }

    @Override
    public DriverContext setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public DriverContext setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    public DriverDeviceContext getDevice() {
        return (DriverDeviceContext) super.getDevice();
    }

    public DriverChannelContext getChannel() {
        return (DriverChannelContext) super.getChannel();
    }

}
