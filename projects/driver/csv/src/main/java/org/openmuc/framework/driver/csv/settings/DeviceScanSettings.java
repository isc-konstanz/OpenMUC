package org.openmuc.framework.driver.csv.settings;

import java.io.File;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class DeviceScanSettings extends Preferences {

	public static final PreferenceType TYPE = PreferenceType.SETTINGS_SCAN_DEVICE;

	@Option
	private String path;

	@Override
	public PreferenceType getPreferenceType() {
		return TYPE;
	}

    public File[] listFiles() throws ArgumentSyntaxException {
    	File dir = new File(path);
        if (!dir.isDirectory()) {
            throw new ArgumentSyntaxException("<path> argument must point to a directory.");
        }
        return dir.listFiles();
    }

}
