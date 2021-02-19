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

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SamplingTask extends DeviceTask {

    private static final Logger logger = LoggerFactory.getLogger(SamplingTask.class);

    List<ReadRecordContainerImpl> channelRecordContainers;
    private boolean unsupportedOperationExceptionThrown = false;
    private boolean unknownDriverExceptionThrown = false;
    private volatile boolean disabled = false;

    boolean running = false;
    boolean startedLate = false;
    String samplingGroup;

    public SamplingTask(DataManager dataManager, Device device, List<ReadRecordContainerImpl> selectedChannels,
            String samplingGroup) {
        this.dataManager = dataManager;
        this.device = device;
        this.channelRecordContainers = selectedChannels;
        this.samplingGroup = samplingGroup;
    }

    // called by main thread
    public void storeValues() {
        if (disabled) {
            return;
        }
        disabled = true;
        if (unsupportedOperationExceptionThrown) {
            for (ReadRecordContainerImpl channelRecordContainer : channelRecordContainers) {
                channelRecordContainer.getChannel().setFlag(Flag.ACCESS_METHOD_NOT_SUPPORTED);
            }
        }
        else if (unknownDriverExceptionThrown) {
            for (ReadRecordContainerImpl channelRecordContainer : channelRecordContainers) {
                channelRecordContainer.getChannel().setFlag(Flag.DRIVER_THREW_UNKNOWN_EXCEPTION);
            }
        }
        else {
            for (ReadRecordContainerImpl channelRecordContainer : channelRecordContainers) {
                channelRecordContainer.getChannel().setNewRecord(channelRecordContainer.getRecord());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void executeRead() throws UnsupportedOperationException, ConnectionException {
        // TODO must pass containerListHandle
        device.connection.read((List<ChannelRecordContainer>) ((List<?>) channelRecordContainers), null, samplingGroup);
    }

    protected void taskAborted() {
    }

    @Override
    public final void run() {

        try {
            executeRead();
        } catch (UnsupportedOperationException e) {
            unsupportedOperationExceptionThrown = true;
        } catch (ConnectionException e) {
            // Connection to device lost. Signal to device instance and end task without notifying DataManager
            logger.warn("Connection to device {} lost because {}. Trying to reconnect...", device.deviceConfig.getId(),
                    e.getMessage());

            synchronized (dataManager.disconnectedDevices) {
                dataManager.disconnectedDevices.add(device);
            }
            dataManager.interrupt();
            return;
        } catch (Exception e) {
            logger.warn("Unexpected exception thrown by read funtion of driver ", e);
            unknownDriverExceptionThrown = true;
        }

        for (ReadRecordContainerImpl channelRecordContainer : channelRecordContainers) {
            channelRecordContainer.getChannel().handle = channelRecordContainer.getChannelHandle();
        }

        synchronized (dataManager.samplingTaskFinished) {
            dataManager.samplingTaskFinished.add(this);
        }
        dataManager.interrupt();
    }

    // called by main thread
    public final void timeout() {
        if (disabled) {
            return;
        }

        disabled = true;
        if (startedLate) {
            for (ReadRecordContainerImpl driverChannel : channelRecordContainers) {
                driverChannel.getChannel().setFlag(Flag.STARTED_LATE_AND_TIMED_OUT);
            }
        }
        else if (running) {
            for (ReadRecordContainerImpl driverChannel : channelRecordContainers) {
                driverChannel.getChannel().setFlag(Flag.TIMEOUT);
            }
        }
        else {
            for (ReadRecordContainerImpl driverChannel : channelRecordContainers) {
                driverChannel.getChannel().setFlag(Flag.DEVICE_OR_INTERFACE_BUSY);
            }
            device.removeTask(this);
        }

    }

    @Override
    public final DeviceTaskType getType() {
        return DeviceTaskType.SAMPLE;
    }

    public final void deviceNotConnected() {
        for (ChannelRecordContainer recordContainer : channelRecordContainers) {
            recordContainer.setRecord(new Record(Flag.COMM_DEVICE_NOT_CONNECTED));
        }
        taskAborted();
    }

}
