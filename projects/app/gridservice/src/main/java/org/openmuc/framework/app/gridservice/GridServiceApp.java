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
package org.openmuc.framework.app.gridservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.app.gridservice.power.PowerListener;
import org.openmuc.framework.app.gridservice.power.PowerListener.PowerCallbacks;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.RecordListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {})
public final class GridServiceApp implements PowerCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(GridServiceApp.class);

    private static final int INTERVAL = 900000;

    private final Map<String, Record> powerRecords = new HashMap<String, Record>();
    private final List<String> powerChannels = new ArrayList<String>();

    @Reference
    private DataAccessService dataAccessService;

    private Channel service;

    private int serviceSetpoint = 0;
    private int serviceMax;
    private int powerMax;
    private int powerMin;
    private double freqMax;
    private double freqMin;

    private boolean error = false;
    private long errorTime = System.currentTimeMillis() - INTERVAL;

    @Activate
    private void activate() {
        logger.info("Activating Grid Service App");
        try {
            GridServiceConfig config = new GridServiceConfig();
            
            powerChannels.addAll(Arrays.asList(config.getPowerChannels().split(",")));
            for (String id : powerChannels) {
                registerPowerListener(id);
            }
            service = initializeChannel(config.getServiceChannel());
            service.setLatestRecord(new Record(new DoubleValue(0), System.currentTimeMillis()));
            serviceMax = config.getServiceMax();
            
            powerMax = config.getPowerMax();
            powerMin = config.getPowerMin();
            
            freqMax = config.getFrequencyMax();
            freqMin = config.getFrequencyMin();
            
            registerFrequencyListener(config.getFrequencyChannel());
            
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.error("Error while applying configuration: {}", e.getMessage());
        }
    }

    protected Channel initializeChannel(String id) throws IllegalArgumentException {
        Channel channel = dataAccessService.getChannel(id);
        if (channel == null) {
            throw new IllegalArgumentException("Unable to find Channel for id: " + id);
        }
        return channel;
    }

    protected void registerPowerListener(String id) throws IllegalArgumentException {
        Channel channel = initializeChannel(id);
        channel.addListener(new PowerListener(this, channel));
    }

    protected void registerFrequencyListener(String id) throws IllegalArgumentException {
        Channel channel = initializeChannel(id);
        channel.addListener(new RecordListener() {
        	
			@Override
			public void newRecord(Record record) {
		        if (record.getFlag() == Flag.VALID) {
		        	double frequency = record.getValue().asDouble();
		        	if (frequency >= freqMax) {
		        		serviceSetpoint = serviceMax;
		        		errorTime = System.currentTimeMillis();
		        		error = true;
		        	}
		        	else if (frequency <= freqMin) {
		        		serviceSetpoint = -serviceMax;
		        		errorTime = System.currentTimeMillis();
		        		error = true;
		        	}
		        	else if (error && System.currentTimeMillis() - errorTime >= INTERVAL) {
		        		serviceSetpoint = 0;
		        		error = false;

		        		service.setLatestRecord(new Record(new DoubleValue(serviceSetpoint), record.getTimestamp()));
		        	}
		        	if (error) {
		        		service.setLatestRecord(new Record(new DoubleValue(serviceSetpoint), record.getTimestamp()));
		        	}
		        }
			}
        });
    }

    @Deactivate
    private void deactivate() {
        logger.info("Deactivating Grid Service App");
    }

	@Override
	public synchronized void onPowerReceived(String id, Record record) {
		if (powerRecords.containsKey(id) && record.getTimestamp() - powerRecords.get(id).getTimestamp() <= 0) {
			return;
		}
		powerRecords.put(id, record);
		
		Record serviceLast = service.getLatestRecord();
		if (error || powerRecords.size() != powerChannels.size() || record.getTimestamp() - serviceLast.getTimestamp() < 1000) {
			return;
		}
		
		double power = 0;
		for (Record r : powerRecords.values()) {
			power += r.getValue().asDouble();
		}
		
		double service = 0;
		if (serviceSetpoint != 0) {
			service = serviceLast.getValue().asDouble() + (serviceSetpoint - power);
			
			if (serviceLast.getValue().asDouble() > 0) {
				if (service > serviceMax) {
					service = serviceMax;
				}
				else if (service < 0) {
					service = 0;
					serviceSetpoint = 0;
				}
			}
			else if (serviceLast.getValue().asDouble() < 0) {
				if (service < -serviceMax) {
					service = -serviceMax;
				}
				else if (service > 0) {
					service = 0;
					serviceSetpoint = 0;
				}
			}
		}
		else if (power > powerMax) {
			serviceSetpoint = powerMax;
			service = powerMax - power;
		}
		else if (power < powerMin) {
			serviceSetpoint = powerMin;
			if (powerMin < 0) {
				service = powerMin + power;
			}
			else {
				service = powerMin - power;
			}
		}
		this.service.setLatestRecord(new Record(new DoubleValue(service), record.getTimestamp()));
    }

}
