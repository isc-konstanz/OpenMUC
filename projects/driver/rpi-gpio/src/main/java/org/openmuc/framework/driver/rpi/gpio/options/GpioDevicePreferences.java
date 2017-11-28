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
package org.openmuc.framework.driver.rpi.gpio.options;

import org.openmuc.framework.config.options.Preferences;

import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;

public class GpioDevicePreferences {

    private static final String PIN_KEY = "pin";

    private static final String BROADCOM_SCHEME_KEY = "broadcomScheme";
    private static final boolean BROADCOM_SCHEME_DEFAULT = false;

    private static final String TYPE_KEY = "type";

    private static final String DEFAULT_STATE_KEY = "defaultState";
    private static final PinState DEFAULT_STATE_DEFAULT = PinState.LOW;

    private static final String SHUTDOWN_STATE_KEY = "shutdownState";
    private static final PinState SHUTDOWN_STATE_DEFAULT = PinState.LOW;

    private static final String PULL_RESISTANCE_KEY = "pullResistance";
    private static final PinPullResistance PULL_RESISTANCE_DEFAULT = PinPullResistance.PULL_DOWN;

    private static final String SHUTDOWN_PULL_RESISTANCE_KEY = "shutdownPullResistance";
    private static final PinPullResistance SHUTDOWN_PULL_RESISTANCE_DEFAULT = PinPullResistance.PULL_DOWN;

    private final Preferences address;
    private final Preferences settings;

    public GpioDevicePreferences(Preferences address, Preferences settings) {
        this.address = address;
        this.settings = settings;
    }

    public Integer getPin() {
        
        if (address.contains(PIN_KEY)) {
            return address.getInteger(PIN_KEY);
        }
        return null;
    }

    public boolean useBroadcomScheme() {
        
        if (address.contains(BROADCOM_SCHEME_KEY)) {
            return address.getBoolean(BROADCOM_SCHEME_KEY);
        }
        return BROADCOM_SCHEME_DEFAULT;
    }

    public GpioType getType() {

        if (settings.contains(TYPE_KEY)) {
            return GpioType.newType(settings.getString(TYPE_KEY));
        }
        return null;
    }

    public PinState getDefaultState() {

        if (settings.contains(DEFAULT_STATE_KEY)) {
            return PinState.getState(settings.getBoolean(DEFAULT_STATE_KEY));
        }
        return DEFAULT_STATE_DEFAULT;
    }

    public PinState getShutdownState() {

        if (settings.contains(SHUTDOWN_STATE_KEY)) {
            return PinState.getState(settings.getBoolean(SHUTDOWN_STATE_KEY));
        }
        return SHUTDOWN_STATE_DEFAULT;
    }

    public PinPullResistance getPullResistance() {
        
        if (settings.contains(PULL_RESISTANCE_KEY)) {
            return PinPullResistance.valueOf(settings.getString(PULL_RESISTANCE_KEY));
        }
        return PULL_RESISTANCE_DEFAULT;
    }

    public PinPullResistance getShutdownPullResistance() {
        
        if (settings.contains(SHUTDOWN_PULL_RESISTANCE_KEY)) {
            return PinPullResistance.valueOf(settings.getString(SHUTDOWN_PULL_RESISTANCE_KEY));
        }
        return SHUTDOWN_PULL_RESISTANCE_DEFAULT;
    }

}
