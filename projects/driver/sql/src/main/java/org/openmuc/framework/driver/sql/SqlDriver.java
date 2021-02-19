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
package org.openmuc.framework.driver.sql;

import org.openmuc.framework.driver.DeviceFactory.Factory;
import org.openmuc.framework.driver.Driver;
import org.openmuc.framework.driver.DriverContext;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;

@Component(service = DriverService.class)
@Factory(device = SqlClient.class)
public class SqlDriver extends Driver {

    private static final String ID = "sql";
    private static final String NAME = "SQL";
    private static final String DESCRIPTION = "SQL \"sequel\" (Structured Query Language) is a domain-specific language " +
                                              "designed for managing data held in databases. The SQL driver connects to " +
                                              "single database instances and provides several possibilities to read data " +
                                              "from columns.";

    private static final String PKG = SqlDriver.class.getPackage().getName().toLowerCase().replace(".driver", "");

    static final String DB_TYPE = System.getProperty(PKG + ".type", "jdbc:mysql");
    static final String DB_DRIVER = System.getProperty(PKG + ".driver", "com.mysql.cj.jdbc.Driver");

    static final String DB_USER = System.getProperty(PKG + ".user", "root");
    static final String DB_PWD = System.getProperty(PKG + ".password", "");

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
