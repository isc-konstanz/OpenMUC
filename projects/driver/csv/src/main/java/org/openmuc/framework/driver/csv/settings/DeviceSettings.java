package org.openmuc.framework.driver.csv.settings;

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;
import org.openmuc.framework.driver.csv.ESamplingMode;

public class DeviceSettings extends Preferences {

	public static final PreferenceType TYPE = PreferenceType.SETTINGS_DEVICE;

	public static final String SAMPLING_MODE = "samplingmode";

	@Option(SAMPLING_MODE)
    private ESamplingMode samplingMode;

	@Option
    private boolean rewind;

	@Override
	public PreferenceType getPreferenceType() {
		return TYPE;
	}

    public ESamplingMode samplingMode() {
        return samplingMode;
    }

    public boolean rewind() {
        return rewind;
    }

}
