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
import org.openmuc.framework.driver.DeviceContext;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;
import org.openmuc.framework.options.SettingsSyntax;

@SettingsSyntax(separator = ";", assignmentOperator = "=")
public class SqlChannel extends Channel {

    @Address(id = "dataColumn",
            name = "Data column",
            description = "The column name of the table containing the value data.",
            mandatory = false)
    protected String dataColumn = "data";

    @Setting(id = "keyColumn",
            name = "Key column",
            description = "The column name of the table containing the unique key identifying the series.<br>" +
                          "<i>Only necessary for unnormalized tables</i>.",
            mandatory = false)
    protected String keyColumn = "key";

    @Setting(id = "key",
            name = "Key",
            description = "The unique key identifying the series.<br>" +
                    "<i>Only necessary for unnormalized tables.</i>",
            mandatory = false)
    protected String key = null;

    @Setting(id = "table",
            name = "Table name",
            description = "Tablename to read columns from.<br>" +
            		      "Will override the configured table name of the connection.",
            mandatory = false)
    protected String table = null;

    @Override
    protected void onCreate(DeviceContext context) {
        if (table == null) {
            table = ((SqlClient) context).getTable();
        }
    }

    @Override
    protected void onConfigure() {
        if (table == null) {
            table = getId().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        }
    }

    public String getDataColumn() {
        return dataColumn;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public String getKey() {
        return key;
    }

    public String getTable() {
        return table;
    }

}
