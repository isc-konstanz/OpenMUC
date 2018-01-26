package org.openmuc.framework.core.datamanager;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DeviceScanListener;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;

public class ScanForDevicesAsyncTask implements Runnable {

    private final DriverService driver;
    private final String settings;
    private final DeviceScanListener listener;

    public ScanForDevicesAsyncTask(DriverService driver, String settings, DeviceScanListener listener) {
        this.driver = driver;
        this.settings = settings;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            driver.scanForDevices(settings, new NonBlockingScanListener(listener));
        } catch (UnsupportedOperationException e) {
            listener.scanError("Device scan not supported by driver");
            return;
        } catch (ArgumentSyntaxException e) {
            listener.scanError("Scan settings syntax invalid: " + e.getMessage());
            return;
        } catch (ScanException e) {
            listener.scanError("IOException while scanning: " + e.getMessage());
            return;
        } catch (ScanInterruptedException e) {
            listener.scanInterrupted();
            return;
        }
    }

    class NonBlockingScanListener implements DriverDeviceScanListener {
        List<DeviceScanInfo> scanInfos = new ArrayList<>();
        DeviceScanListener listener;

        public NonBlockingScanListener(DeviceScanListener listener) {
            this.listener = listener;
        }

        @Override
        public void scanProgressUpdate(int progress) {
            listener.scanProgress(progress);
            if (progress >= 100) {
            	listener.scanFinished();
            }
        }

        @Override
        public void deviceFound(DeviceScanInfo scanInfo) {
            if (!scanInfos.contains(scanInfo)) {
                scanInfos.add(scanInfo);
                listener.deviceFound(scanInfo);
            }
        }
    }

}
