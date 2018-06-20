package org.openmuc.framework.driver.csv.settings;

import java.io.File;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.options.Preferences;

public class DeviceScanSettings {

	public final static String PATH_KEY = "path";

    private File file;

    public DeviceScanSettings(Preferences settings) throws ArgumentSyntaxException {
        String path = settings.getString(PATH_KEY);
        
        file = new File(path);
        if (!file.isDirectory()) {
            throw new ArgumentSyntaxException("<path> argument must point to a directory.");
        }
    }

    public File path() {
        return file;
    }

}
