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
package org.openmuc.framework.datalogger.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;


public abstract class Index {

    protected final String column;

    public Index(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public String queryWhere(long startTime, long endTime) {
        return MessageFormat.format("WHERE {0} >= ''{1}'' AND {0} <= ''{2}'' ORDER BY {0} ASC", column,
                encode(startTime),
                encode(endTime));
    }

    public abstract long decode(ResultSet result) throws SQLException, ParseException;

    public abstract String encode(long timestamp);

}
