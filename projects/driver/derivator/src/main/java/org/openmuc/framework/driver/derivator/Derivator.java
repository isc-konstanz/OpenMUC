/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.driver.derivator;

import static org.openmuc.framework.driver.derivator.DerivatorConstants.ADDRESS_CHANNEL_ID_INDEX;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.ADDRESS_DERIVATION_TIME_INDEX;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.ADDRESS_PARTS_LENGTH_MAX;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.ADDRESS_PARTS_LENGTH_MIN;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.ADDRESS_SEPARATOR;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.DEFAULT_DERIVATION_TIME;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.DERIVATION_TIME_HOURS;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.DERIVATION_TIME_MINUTES;
import static org.openmuc.framework.driver.derivator.DerivatorConstants.DERIVATION_TIME_SECONDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DriverService.class)
public class Derivator implements DriverService, Connection {

    private static final Logger logger = LoggerFactory.getLogger(Derivator.class);

    private DataAccessService dataAccessService;

    private Map<String, DerivatorChannel> derivatorChannels = new HashMap<String, DerivatorChannel>();

    @Override
    public DriverInfo getInfo() {

        String driverId = "derivator";
        String description = "Is able to calculate the time derivative of a channel value and writes the derived value into a new channel. Different time units supported.";
        String deviceAddressSyntax = "not needed";
        String parametersSyntax = "not needed";
        String channelAddressSyntax = "<id of channel which should be derived>:[<unit>]";
        String deviceScanParametersSyntax = "not supported";

        return new DriverInfo(driverId, description, deviceAddressSyntax, parametersSyntax, channelAddressSyntax,
                deviceScanParametersSyntax);
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {

        for (ChannelRecordContainer container : containers) {
            container.setRecord(deriveRecord(container));
        }
        return null;
    }

    private Record deriveRecord(ChannelRecordContainer container) {
        try {
            DerivatorChannel derivatorChannel = getChannel(container);
            return derivatorChannel.derive();
            
        } catch (ArgumentSyntaxException e) {
            logger.info("Unable to perform derivation for channel {}. {}", container.getChannel().getId(), e.getMessage());
            return new Record(Flag.DRIVER_ERROR_CHANNEL_ADDRESS_SYNTAX_INVALID);
            
        } catch (Exception e) {
            logger.warn("Unable to perform derivation for channel {}. {}", container.getChannel().getId(), e.getMessage());
            return new Record(Flag.DRIVER_ERROR_READ_FAILURE);
        }
    }

    public DerivatorChannel getChannel(ChannelRecordContainer container) throws ArgumentSyntaxException, DerivationException {
        DerivatorChannel derivatorChannel;
        
        String address = container.getChannelAddress();
        String[] addressParts = address.split(ADDRESS_SEPARATOR);
        int addressPartsLength = addressParts.length;
        
        if (addressPartsLength > ADDRESS_PARTS_LENGTH_MAX || addressPartsLength < ADDRESS_PARTS_LENGTH_MIN) {
            throw new ArgumentSyntaxException("Invalid number of channel address parameters.");
        }
        
        int derivativeTime = extractDerivativeTime(addressPartsLength, addressParts);
        String sourceChannelId = addressParts[ADDRESS_CHANNEL_ID_INDEX];
        if (derivatorChannels.containsKey(sourceChannelId)) {
        	derivatorChannel = derivatorChannels.get(sourceChannelId);
        	derivatorChannel.setDerivativeTime(derivativeTime);
        }
        else {
            Channel sourceChannel = dataAccessService.getChannel(sourceChannelId);
            derivatorChannel = new DerivatorChannel(sourceChannel, derivativeTime);
            derivatorChannels.put(sourceChannelId, derivatorChannel);
        }
        return derivatorChannel;
    }

    private int extractDerivativeTime(int addressPartsLength, String[] addressParts) throws ArgumentSyntaxException {
        int derivativeTime = DEFAULT_DERIVATION_TIME;
        
        if (addressPartsLength == ADDRESS_PARTS_LENGTH_MAX) {
            try {
                derivativeTime = Integer.valueOf(addressParts[ADDRESS_DERIVATION_TIME_INDEX]);
                
            } catch (NumberFormatException e) {
                switch (addressParts[ADDRESS_DERIVATION_TIME_INDEX].toLowerCase()) {
                case DERIVATION_TIME_SECONDS:
                    derivativeTime = 1000;
                    break;
                case DERIVATION_TIME_MINUTES:
                    derivativeTime = 60000;
                    break;
                case DERIVATION_TIME_HOURS:
                    derivativeTime = 3600000;
                    break;
                default:
                    throw new ArgumentSyntaxException("Invalid derivative time: " + addressParts[ADDRESS_DERIVATION_TIME_INDEX]);
                }
            }
        }
        return derivativeTime;
    }

    @Override
    public Connection connect(String deviceAddress, String settings)
            throws ArgumentSyntaxException, ConnectionException {

        // No connection needed so far
        return this;
    }

    @Override
    public void disconnect() {

        // No disconnect needed so far
    }

    @Reference
    protected void setDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = null;
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
    public void scanForDevices(String settings, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        throw new UnsupportedOperationException();
    }

}
