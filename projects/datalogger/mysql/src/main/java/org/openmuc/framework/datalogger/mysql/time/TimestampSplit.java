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
package org.openmuc.framework.datalogger.mysql.time;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.openmuc.framework.datalogger.mysql.Index;


public class TimestampSplit extends Index {

    protected final SimpleDateFormat format;
    protected final SimpleDateFormat formatDate;
    protected final SimpleDateFormat formatTime;

    public TimestampSplit(String column, String format) {
        super(column);
        String[] formats = format.split(" ");
        
        this.formatDate = new SimpleDateFormat(formats[0]);
        this.formatTime = new SimpleDateFormat(formats[1]);
        this.format = new SimpleDateFormat(format);
    }

    public String queryWhere(long startTime, long endTime) {
        String[] columns = column.split(",");
        
        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);
        
        return MessageFormat.format("WHERE {0} >= ''{1}'' AND {0} <= ''{2}'' AND {3} >= ''{4}'' AND {3} <= ''{5}'' ORDER BY {0},{3} ASC", 
                columns[0], formatDate.format(startDate), formatDate.format(endDate),
                columns[1], formatTime.format(startDate), formatDate.format(formatTime));
    }

    @Override
    public long decode(ResultSet result) throws SQLException, ParseException {
        String[] columns = column.split(",");
        
//        Date date = result.getDate(columns[0]);
//        Time time = result.getTime(columns[1]);
//        
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(date.getYear(), date.getMonth(), date.getDate(), 
//                time.getHours(), time.getMinutes(), time.getSeconds());
        
        return format.parse(result.getString(columns[0]) + " " + result.getString(columns[1])).getTime();
    }

    @Override
    public String encode(long timestamp) {
        Date date = new Date(timestamp);
        return formatDate.format(date) + "','" + formatTime.format(date);
    }

}
