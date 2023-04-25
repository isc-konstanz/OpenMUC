/*
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * You are free to use code of this sample file in any
 * way you like and without any restrictions.
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
import org.the.ems.env.hp.vlv.Valve;

@Component(immediate = true, service = HeatPumpEnvironmentService.class)
public final class HeatPumpEnvironment implements HeatPumpEnvironmentService {

    private static final Logger logger = LoggerFactory.getLogger(HeatPumpEnvironment.class);

    private Valve valve; 

    @Reference
    private DataAccessService dataAccessService;

    public HeatPumpEnvironment() {
    }

	@Override
	public Valve getValve() {
		return valve;
	}

    @Activate
    private void activate() {
        logger.info("Activating TH-E Environment: Heat Pump");

//    	valve = new Valve(...);
    }

    @Deactivate
    private void deactivate() {
        logger.info("Deactivating TH-E Environment: Heat Pump");
    }

}
