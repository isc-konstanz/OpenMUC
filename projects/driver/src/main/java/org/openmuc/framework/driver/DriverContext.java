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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.option.DriverOptions;

public class DriverContext extends DriverOptions {

    <D extends Device<?>> DriverContext(Driver<D> driver) {
        super(driver, 
              driver.getId(),
              driver.getName(),
              driver.getDescription());
    }

    @Override
    public DriverContext setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public DriverContext setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    public DeviceContext getDevice() {
        return (DeviceContext) super.getDevice();
    }

    public ChannelContext getChannel() {
        return (ChannelContext) super.getChannel();
    }

    static <C extends Configurable> C newInstance(Class<C> configurable) throws IllegalArgumentException {
        try {
            return (C) configurable.getDeclaredConstructor().newInstance();
            
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(MessageFormat.format("Unable to instance {0}: {1}", 
                    configurable.getSimpleName(), e.getMessage()));
        }
    }

}
