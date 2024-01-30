/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.driver.clipper;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.driver.DriverActivator;
import org.openmuc.framework.driver.DriverChannelFactory;
import org.openmuc.framework.driver.DriverDevice;
import org.openmuc.framework.driver.DriverDeviceFactory;
import org.openmuc.framework.driver.annotation.Device;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Driver;
import org.openmuc.framework.driver.clipper.Clipper.ClipperChannelFactory;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = DriverService.class)
@Driver(id = Clipper.ID,
        name = Clipper.NAME,
        description = Clipper.DESCRIPTION,
        device = ClipperChannelFactory.class)
public class Clipper extends DriverActivator implements DriverService, DriverDeviceFactory {

    public static final String ID = "clipper";
    public static final String NAME = "Clipper";
    public static final String DESCRIPTION = "Driver to listen to values of a channel and write the clipped value into a new channel. Different clipping types supported.";

    private ClipperChannelFactory channelFactory;

    @Reference
    protected void bindDataAccessService(DataAccessService dataAccessService) {
    	this.channelFactory = new ClipperChannelFactory(dataAccessService);
    }

    protected void unbindDataAccessService(DataAccessService dataAccessService) {
    	this.channelFactory = null;
    }

	@Override
	public ClipperChannelFactory newDevice(Address address, Settings settings) throws ConnectionException {
		if (channelFactory == null) {
			throw new ConnectionException("Clipper not yet ready");
		}
		return channelFactory;
	}

	@Device(channel = ClipperChannel.class)
	public class ClipperChannelFactory extends DriverDevice implements DriverChannelFactory {

	    private final DataAccessService dataAccessService;

	    protected ClipperChannelFactory(DataAccessService dataAccessService) {
	    	this.dataAccessService = dataAccessService;
	    }

	    @Disconnect
	    public void disconnect(Clipper clipper) throws ArgumentSyntaxException, ConnectionException {
	    	getChannels().stream().forEach(c -> ((ClipperChannel) c).stopListening());
	    }

		@Override
		public ClipperChannel newChannel(Address address, Settings settings) throws ArgumentSyntaxException {
			String channelId = address.getString(ClipperChannel.ID);
			Channel channel = dataAccessService.getChannel(channelId);
	        if (channel == null) {
	            throw new ArgumentSyntaxException("Source channel is null");
	        }
			return new ClipperChannel(channel);
		}
	}

}
