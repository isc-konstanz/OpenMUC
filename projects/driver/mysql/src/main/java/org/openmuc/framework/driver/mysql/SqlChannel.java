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

import org.openmuc.framework.driver.Channel;
import org.openmuc.framework.options.Address;

public abstract class SqlChannel extends Channel {

    @Address(id = "table", mandatory = false)
    protected String table = getId().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");

    @Address(id = "data",
             mandatory = false,
             description = "Varies due to the table construction. " +
                           "Either column name or cell content, " +
                           "if more rows have the same timestamp.")
    protected String data = null;

    @Address(id = "column", mandatory = false)
    protected String column = "data";

    @Address(id = "index", mandatory = false)
    protected String index = "time";

    public String getTable() {
        return table;
    }

    public String getIndexColumn() {
        return index;
    }

    public String getDataColumn() {
        return data;
    }

    public String getColumn() {
        return column;
    }

    public abstract String getReadQuery() throws UnsupportedOperationException;

}
