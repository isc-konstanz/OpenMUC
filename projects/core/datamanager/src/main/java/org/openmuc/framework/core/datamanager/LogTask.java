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

package org.openmuc.framework.core.datamanager;

import java.util.List;

import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LogTask extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(LogTask.class);

    private final DataLoggerService dataLogger;
    private final List<LogRecordContainer> containers;
    private final long timestamp;

    public LogTask(DataLoggerService dataLogger, List<LogRecordContainer> containers, long timestamp) {
        this.dataLogger = dataLogger;
        this.containers = containers;
        this.timestamp = timestamp;
    }

    @Override
    public void run() {
        try {
            dataLogger.log(containers, timestamp);
            
        } catch(Exception e) {
            logger.warn("Unexpected exception thrown by log funtion of data logger {}: {}", 
                    dataLogger.getId(), e.getMessage());
        }
    }

}
