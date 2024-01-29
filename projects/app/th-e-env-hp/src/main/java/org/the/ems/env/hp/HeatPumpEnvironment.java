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
package org.the.ems.env.hp;

import java.util.Dictionary;

import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.lib.osgi.config.DictionaryPreprocessor;
import org.openmuc.framework.lib.osgi.config.ServicePropertyException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hp.hr.HeatingRodController;

public final class HeatPumpEnvironment implements HeatPumpEnvironmentService, ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(HeatPumpEnvironment.class);

    private final HeatPumpEnvironmentSettings settings;
    private final HeatPumpEnvironmentProperties properties;

    private HeatingRodController heatingRod;

    HeatPumpEnvironment(DataAccessService dataAccessService) {
        settings = new HeatPumpEnvironmentSettings();
        properties = new HeatPumpEnvironmentProperties(settings, dataAccessService);
	}

    protected void activate() {
    	heatingRod = new HeatingRodController(properties);
    }

    protected void deactivate() {
    	if (heatingRod != null) {
    		heatingRod.shutdown();
        	heatingRod = null;
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
        logger.info("Heat pump environment configuration updated: {}", properties.toString());
        if (heatingRod != null) {
        	deactivate();
        }
        activate();
    }

}
