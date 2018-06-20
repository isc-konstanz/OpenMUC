package org.openmuc.framework.driver.csv.settings;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.options.Preferences;
import org.openmuc.framework.driver.csv.ESamplingMode;

public class DeviceSettings {

	public final static String SAMPLINGMODE_KEY = "samplingmode";
	public final static String REWIND_KEY = "rewind";

    private final ESamplingMode samplingMode;
    private final boolean rewind;

    public DeviceSettings(Preferences settings) throws ArgumentSyntaxException {
    	if (settings.contains(SAMPLINGMODE_KEY)) {
        	samplingMode = ESamplingMode.valueOf(settings.getString(SAMPLINGMODE_KEY).toUpperCase());
    	}
    	else {
    		samplingMode = ESamplingMode.LINE;
    	}

    	if (settings.contains(REWIND_KEY)) {
        	rewind = settings.getBoolean(REWIND_KEY);
    	}
    	else {
    		rewind = false;
    	}
    }

    public ESamplingMode samplingMode() {
        return samplingMode;
    }

    public boolean rewind() {
        return rewind;
    }

}
