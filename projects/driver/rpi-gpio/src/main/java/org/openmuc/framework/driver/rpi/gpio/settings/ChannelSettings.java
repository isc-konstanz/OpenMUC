/*
 * Copyright 2011-18 Fraunhofer ISE
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
package org.openmuc.framework.driver.rpi.gpio.settings;

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class ChannelSettings extends Preferences {

    public static final PreferenceType TYPE = PreferenceType.SETTINGS_CHANNEL;

    @Option
    private boolean inverted = false;

    @Option
    private int impulses = 1;

    @Option
    private boolean intervalCount = false;

    @Option
    private Integer derivativeTime = null;

    @Override
    public PreferenceType getPreferenceType() {
        return TYPE;
    }

    public boolean isInverted() {
        return inverted;
    }

    public double getImpulses() {
        return (double) impulses;
    }

    public boolean isIntervalCount() {
        return intervalCount;
    }

    public boolean isDerivative() {
    	if (derivativeTime != null) {
    		return true;
    	}
        return false;
    }

    public double getDerivariveTime() {
        return derivativeTime.doubleValue();
    }

}
