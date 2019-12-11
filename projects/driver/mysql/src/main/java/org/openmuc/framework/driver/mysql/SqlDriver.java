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
package org.openmuc.framework.driver.mysql;

import org.openmuc.framework.driver.spi.Driver;
import org.openmuc.framework.driver.spi.DriverContext;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;

@Component
public class SqlDriver extends Driver<SqlClient> implements DriverService {

    private static final String ID = "mysql";
    private static final String NAME = "MySQL";
    private static final String DESCRIPTION = "Placeholder for a driver description.";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void onCreate(DriverContext context) {
        context.setName(NAME)
               .setDescription(DESCRIPTION);
    }

}
