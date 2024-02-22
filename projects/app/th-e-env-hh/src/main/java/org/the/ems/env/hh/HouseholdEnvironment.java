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
package org.the.ems.env.hh;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.lib.osgi.config.DictionaryPreprocessor;
import org.openmuc.framework.lib.osgi.config.ServicePropertyException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.Controller;
import org.the.ems.env.RecordAverageListener;
import org.the.ems.env.hh.hs.FlowPump;
import org.the.ems.env.hh.hs.HeatExchangePulse;
import org.the.ems.env.hh.hs.HeatExchangeValve;
import org.the.ems.env.hh.hs.HeatSink;


@Component(immediate = true, service = {})
public final class HouseholdEnvironment implements ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(HouseholdEnvironment.class);

    private final HouseholdEnvironmentSettings settings;
    private final HouseholdEnvironmentProperties properties;

    private int interval = HouseholdEnvironmentSettings.INTERVAL_DEFAULT*1000;

    private Channel powerSetpoint;
    private Channel power;

    private List<HeatSink> heatSinks = new ArrayList<HeatSink>();

    private Controller powerSetpointController;

    private RecordAverageListener powerSetpointListener;
    private RecordAverageListener powerListener;

    private Timer updateTimer;

    HouseholdEnvironment(DataAccessService dataAccessService) {
        settings = new HouseholdEnvironmentSettings();
        properties = new HouseholdEnvironmentProperties(settings, dataAccessService);
	}

    private void activate() {
    	interval = properties.getInterval();

        heatSinks.add(new FlowPump(properties));
        heatSinks.add(new HeatExchangeValve(properties));
        heatSinks.add(new HeatExchangePulse(properties));

    	powerSetpointController = new Controller(0.8, 0.2, 0.125, 4000, 0);
    	powerSetpointController.enableErrorDragging(200);

        powerSetpointListener = new RecordAverageListener(interval);
        powerSetpoint = properties.getThermalPowerChannel();
        powerSetpoint.addListener(powerSetpointListener);

        powerListener = new RecordAverageListener(interval);
        power = properties.getThermalPowerSetpointChannel();
        power.addListener(powerListener);

    	if (properties.isEnabled()) {
            activateControl();
    	}
    }

    private void activateControl() {
        updateTimer = new Timer("TH-E Environment: Household control timer");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                setThermalSetpoint();
            }
        };
        updateTimer.scheduleAtFixedRate(task, 0, interval);
    }

    private void deactivate() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge();
            updateTimer = null;
        }
        for (HeatSink heatSink : heatSinks) {
        	heatSink.set(0);
        }
        heatSinks.clear();

        powerSetpointController.reset();
        powerSetpoint.removeListener(powerSetpointListener);
        power.removeListener(powerListener);
    }

    public void setThermalSetpoint() {
        double setpoint  = powerSetpointController.process(interval, 
        		powerSetpointListener.getMean(),
        		powerListener.getMean());
        
        for (HeatSink heatSink : heatSinks) {
        	heatSink.set(setpoint);
        	if (setpoint > 0) {
            	setpoint -= heatSink.getPower();
            	if (setpoint < 0) {
            		setpoint = 0;
            	}
        	}
        }
    }

    @Override
    public void updated(Dictionary<String, ?> propertyDict) throws ConfigurationException {
        DictionaryPreprocessor dictionary = new DictionaryPreprocessor(propertyDict);
        if (!dictionary.wasIntermediateOsgiInitCall()) {
            updateConfiguration(dictionary);
        }
    }

    private void updateConfiguration(DictionaryPreprocessor dictionary) {
        try {
        	properties.processConfig(dictionary);
            if (properties.configChanged() || properties.isDefaultConfig()) {
            	applyConfiguration();
            }
        } catch (ServicePropertyException e) {
            logger.error("Update properties failed", e);
            deactivate();
        }
    }

    private void applyConfiguration() {
        logger.info("Household environment configuration updated: {}", properties.toString());
        if (heatSinks.size() > 0) {
        	deactivate();
        }
        activate();
    }
}
