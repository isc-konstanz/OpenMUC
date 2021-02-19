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
package org.openmuc.framework.driver;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.ChannelOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.driver.spi.ChannelTaskContainer;

public abstract class ChannelContext extends Configurable implements ChannelOptions, ChannelFactory, ChannelScannerFactory {

    Class<? extends ChannelContainer> channelClass;

    Class<? extends ChannelScanner> scannerClass;

    DeviceContext context;

    ChannelContext() {
        bindContext(getClass());
    }

    ChannelContext(Class<? extends ChannelContext> context) {
        bindContext(context);
    }

    ChannelContext bindContext(Class<? extends ChannelContext> context) {
        ChannelFactory.Factory factory = context.getAnnotation(ChannelFactory.Factory.class);
        if (factory != null) {
            scannerClass = factory.scanner();
            channelClass = factory.channel();
        }
        return this;
    }

    public final DeviceContext getContext() {
        return context;
    }

    @Override
    public final Options getAddressOptions() {
        Options channelAddress = null;
        if (channelClass != null) {
            channelAddress = Options.parseAddress(channelClass);
        }
        return channelAddress;
    }

    @Override
    public final Options getSettingsOptions() {
        Options channelSettings = null;
        if (channelClass != null) {
            channelSettings = Options.parseSettings(channelClass);
        }
        return channelSettings;
    }

    @Override
    public final Options getScanSettingsOptions() {
        Options scanSettings = null;
        if (scannerClass != null && !scannerClass.equals(ChannelScanner.class)) {
            scanSettings = Options.parseSettings(scannerClass);
        }
        return scanSettings;
    }

    final void bindChannel(Class<? extends ChannelContainer> channelClass) {
        this.channelClass = channelClass;
    }

    final ChannelContainer newChannel(ChannelTaskContainer container) throws ArgumentSyntaxException {
        return this.newChannel(container.getChannel().getAddress(), 
                               container.getChannel().getSettings());
    }

    public ChannelContainer newChannel(String address, String settings) throws ArgumentSyntaxException {
        return this.newChannel(Configurations.parseAddress(address, channelClass),
                              Configurations.parseSettings(settings, channelClass));
    }

    @Override
    public ChannelContainer newChannel(Address address, Settings settings) throws ArgumentSyntaxException {
        return this.newChannel();
    }

    protected ChannelContainer newChannel() {
        return DriverContext.newInstance(channelClass);
    }

    final void bindScanner(Class<? extends ChannelScanner> scannerClass) {
        this.scannerClass = scannerClass;
    }

    public ChannelScanner newScanner(String settings) throws ArgumentSyntaxException {
        return this.newScanner(Configurations.parseSettings(settings, scannerClass));
    }

    @Override
    public ChannelScanner newScanner(Settings settings) throws ArgumentSyntaxException {
        return this.newScanner();
    }

    protected ChannelScanner newScanner() {
        return DriverContext.newInstance(scannerClass);
    }

}
