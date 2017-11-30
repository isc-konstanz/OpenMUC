/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.driver.iec62056p21;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.iec62056p21.options.Iec62056DevicePreferences;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec62056Device implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(Iec62056Device.class);

    private final Iec62056DevicePreferences settings;
    private final Iec62056Connection connection;

    public Iec62056Device(Iec62056Connection connection, Iec62056DevicePreferences settings) {
        this.connection = connection;
        this.settings = settings;
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ScanException, ConnectionException {

        List<Iec62056DataSet> dataSets;
        try {
            synchronized(connection) {
                dataSets = connection.read(this.settings);
            }

            if (dataSets == null) {
                throw new TimeoutException("No data sets received.");
            }
        } catch (IOException e) {
            logger.debug("Scanning channels for device failed: " + e);
            throw new ScanException(e);
        } catch (TimeoutException e) {
            logger.debug("Timeout while scanning channels for device: " + e);
            throw new ScanException(e);
        }

        List<ChannelScanInfo> scanInfos = new ArrayList<>(dataSets.size());

        for (Iec62056DataSet dataSet : dataSets) {
            try {
                Double.parseDouble(dataSet.getValue());
                scanInfos.add(new ChannelScanInfo(dataSet.getId(), "", ValueType.DOUBLE, null));
            } catch (NumberFormatException e) {
                scanInfos.add(new ChannelScanInfo(dataSet.getId(), "", ValueType.STRING, dataSet.getValue().length()));
            }

        }
        return scanInfos;
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        Map<String, ChannelRecordContainer> dataSetsById = new HashMap<String, ChannelRecordContainer>();
        for (ChannelRecordContainer container : containers) {
            dataSetsById.put(container.getChannelAddress(), container);
        }
        
        List<Iec62056DataSet> dataSets = null;
        try {
            synchronized(connection) {
                dataSets = connection.read(settings, dataSetsById.keySet());
            }

            if (dataSets == null) {
                throw new TimeoutException("No data sets received.");
            }
        } catch (IOException e) {
            logger.debug("Reading from device failed: " + e);
            
            for (ChannelRecordContainer container : containers) {
                container.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
            }
            return null;
        } catch (TimeoutException e) {
            logger.debug("Timeout while reading from device: " + e);
            for (ChannelRecordContainer container : containers) {
                container.setRecord(new Record(Flag.TIMEOUT));
            }
            throw new ConnectionException("Read timed out: " + e.getMessage());
        }

        long time = System.currentTimeMillis();
        for (Iec62056DataSet dataSet : dataSets) {
            if (dataSetsById.containsKey(dataSet.getId())) {
                String value = dataSet.getValue();
                if (value != null) {
                    ChannelRecordContainer container = dataSetsById.get(dataSet.getId());
                    try {
                        container.setRecord(new Record(new DoubleValue(Double.parseDouble(dataSet.getValue())), time));
                    } catch (NumberFormatException e) {
                        container.setRecord(new Record(new StringValue(dataSet.getValue()), time));
                    }
                }
                break;
            }
        }
        return null;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect() {
        connection.close();
    }
}
