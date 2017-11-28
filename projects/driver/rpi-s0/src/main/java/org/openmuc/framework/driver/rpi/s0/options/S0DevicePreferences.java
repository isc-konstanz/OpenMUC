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
package org.openmuc.framework.driver.rpi.s0.options;

import org.openmuc.framework.config.options.Preferences;

import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;

public class S0DevicePreferences {

    private static final String PIN_KEY = "pin";

    private static final String BROADCOM_SCHEME_KEY = "broadcomScheme";
    private static final boolean BROADCOM_SCHEME_DEFAULT = false;

    private static final String PULL_RESISTANCE_KEY = "pullResistance";
    private static final PinPullResistance PULL_RESISTANCE_DEFAULT = PinPullResistance.PULL_DOWN;

    private static final String SHUTDOWN_PULL_RESISTANCE_KEY = "shutdownPullResistance";
    private static final PinPullResistance SHUTDOWN_PULL_RESISTANCE_DEFAULT = PinPullResistance.PULL_DOWN;

    private static final String SHUTDOWN_STATE_KEY = "shutdownState";
    private static final PinState SHUTDOWN_STATE_DEFAULT = PinState.LOW;

    private static final String BOUNCE_TIME_KEY = "bounceTime";
    private static final int BOUNCE_TIME_DEFAULT = 60;

    private final Preferences address;
    private final Preferences settings;

    public S0DevicePreferences(Preferences address, Preferences settings) {
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

    public PinState getShutdownState() {

        if (settings.contains(SHUTDOWN_STATE_KEY)) {
            return PinState.getState(settings.getBoolean(SHUTDOWN_STATE_KEY));
        }
        return SHUTDOWN_STATE_DEFAULT;
    }

    public int getBounceTime() {
        
        if (settings.contains(BOUNCE_TIME_KEY)) {
            return settings.getInteger(BOUNCE_TIME_KEY);
        }
        return BOUNCE_TIME_DEFAULT;
    }

}
