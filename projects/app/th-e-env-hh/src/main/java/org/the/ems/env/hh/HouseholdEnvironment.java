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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hh.hs.CirculationPump;
import org.the.ems.env.hh.hs.HeatExchangeValve;
import org.the.ems.env.hh.hs.HeatSink;
import org.the.ems.env.hh.hs.HeatExchangePulse;

@Component(immediate = true, service = {})
public final class HouseholdEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(HouseholdEnvironment.class);

    private static final String ID_TH_POWER_SETPOINT = "hh_th_power";
    private static final String ID_TH_POWER = "hh_flow_power";

    private static final int SECONDS_PER_INTERVAL = 60*3;
    private final int interval = SECONDS_PER_INTERVAL*1000;

    private Channel powerSetpoint;
    private Channel power;

    private List<HeatSink> heatSinks = new ArrayList<HeatSink>();

    private Timer updateTimer;

    private Controller powerSetpointController = new Controller(0.8, 0.2, 0.125, 4000, 0);

    private RecordAverageListener powerSetpointListener;
    private RecordAverageListener powerListener;

    @Reference
    private DataAccessService dataAccessService;

    @Activate
    private void activate() {
        logger.info("Activating TH-E Environment: Household");
        heatSinks.add(new CirculationPump(dataAccessService));
        heatSinks.add(new HeatExchangeValve(dataAccessService));
        heatSinks.add(new HeatExchangePulse(dataAccessService));

        powerSetpointListener = new RecordAverageListener(interval);
        powerSetpoint = dataAccessService.getChannel(ID_TH_POWER_SETPOINT);
        powerSetpoint.addListener(powerSetpointListener);

        powerListener = new RecordAverageListener(interval);
        power = dataAccessService.getChannel(ID_TH_POWER);
        power.addListener(powerListener);
        
        initializeUpdateTimer();
    }

    @Deactivate
    private void deactivate() {
        logger.info("Deactivating TH-E Environment: Heat Pump");
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge();
            updateTimer = null;
        }
        powerSetpoint.removeListener(powerSetpointListener);
        power.removeListener(powerListener);
    }

    private void initializeUpdateTimer() {
        powerSetpointController.reset();
        updateTimer = new Timer("TH-E Environment: Household control timer");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                setThermalSetpoint();
            }
        };
        updateTimer.scheduleAtFixedRate(task, 0, interval);
    }

    public void setThermalSetpoint() {
        double setpoint  = powerSetpointController.process(interval/(1000*60*3), 
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
}
