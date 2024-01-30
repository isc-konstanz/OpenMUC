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

import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.lib.osgi.deployment.RegistrationHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public final class HouseholdEnvironmentComponent {
    private static final Logger logger = LoggerFactory.getLogger(HouseholdEnvironment.class);

    private RegistrationHandler registrationHandler;

    private HouseholdEnvironment householdEnv;

    @Reference
    private DataAccessService dataAccessService;

    @Activate
    protected void activate(BundleContext context) {
        logger.info("Activating TH-E Environment: Household");
        householdEnv = new HouseholdEnvironment(dataAccessService);

        registrationHandler = new RegistrationHandler(context);
        registrationHandler.provideInFrameworkAsManagedService(householdEnv, HouseholdEnvironment.class.getName());
    }

    @Deactivate
    private void deactivate() {
        logger.info("Deactivating TH-E Environment: Household");
    }

}
