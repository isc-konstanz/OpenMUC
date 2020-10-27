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
package org.openmuc.framework.datalogger.sql.time;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmuc.framework.datalogger.sql.Index;


public class TimestampUnix extends Index {

    protected final int resolution;

    public TimestampUnix(String column, int resolution) {
        super(column);
        this.resolution = resolution;
    }

    @Override
    public long decode(ResultSet result) throws SQLException {
        long timestamp = result.getLong(column);
        return timestamp*resolution;
    }

    @Override
    public String encode(long timestamp) {
        return String.valueOf(Math.round((double) timestamp/resolution));
    }

}
