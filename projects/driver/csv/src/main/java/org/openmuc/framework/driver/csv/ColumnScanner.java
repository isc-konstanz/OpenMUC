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
package org.openmuc.framework.driver.csv;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.ChannelContext;
import org.openmuc.framework.driver.ChannelScanner;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnScanner extends ChannelScanner {

    private static final Logger logger = LoggerFactory.getLogger(ColumnScanner.class);

    private CsvFile file;

    protected void onCreate(ChannelContext device) throws ArgumentSyntaxException, ConnectionException {
        file = (CsvFile) device;
    }

    @Override
    protected void onScan(List<ChannelScanInfo> channels) throws ArgumentSyntaxException, ScanException, ConnectionException {
        logger.info("Scan for columns in CSV file");
        
        for (String channelId : file.getColumns()) {
            channels.add(new ChannelScanInfo(channelId, channelId, ValueType.DOUBLE, null));
        }
    }

}
