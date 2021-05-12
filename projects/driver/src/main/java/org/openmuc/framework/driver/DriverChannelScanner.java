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
package org.openmuc.framework.driver;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.Reflectable;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.spi.ConnectionException;

public abstract class DriverChannelScanner extends Reflectable {

	DriverChannelContext context;

    protected DriverChannelScanner() {
    }

    void invokeConfigure(DriverChannelContext context, Settings settings) throws ArgumentSyntaxException {
        this.configure(settings);
        this.context = context;
        
        invokeMethod(Configure.class, this, context, settings);
        invokeMethod(Configure.class, this, context);
        invokeMethod(Configure.class, this);
    }

    public DriverChannelContext getContext() {
    	return context;
    }

    public List<ChannelScanInfo> scan() 
            throws ArgumentSyntaxException, ScanException, ConnectionException {
        
        List<ChannelScanInfo> channels = new ArrayList<>();
        this.scan(channels);
        return channels;
    }

    protected abstract void scan(List<ChannelScanInfo> channelScanInfos) 
            throws ArgumentSyntaxException, ScanException, ConnectionException;

}
