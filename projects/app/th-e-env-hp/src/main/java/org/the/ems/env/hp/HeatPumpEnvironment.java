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

import org.openmuc.framework.dataaccess.DataAccessService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.the.ems.env.hp.htr.HeatingRodController;

@Component(immediate = true, service = HeatPumpEnvironmentService.class)
public final class HeatPumpEnvironment implements HeatPumpEnvironmentService {
    private static final Logger logger = LoggerFactory.getLogger(HeatPumpEnvironment.class);

    HeatingRodController heatingRod;

    @Reference
    private DataAccessService dataAccessService;

    @Activate
    private void activate() {
        logger.info("Activating TH-E Environment: Heat Pump");
        heatingRod = new HeatingRodController(dataAccessService);
    }

    @Deactivate
    private void deactivate() {
        logger.info("Deactivating TH-E Environment: Heat Pump");
    }

}
