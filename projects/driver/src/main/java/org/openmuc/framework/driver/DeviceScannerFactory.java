package org.openmuc.framework.driver;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Settings;

public interface DeviceScannerFactory {

    DeviceScanner newScanner(Settings settings) throws ArgumentSyntaxException;

}
