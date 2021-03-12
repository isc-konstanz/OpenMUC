/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.driver.csv;

import java.io.File;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.annotation.Setting;
import org.openmuc.framework.driver.DeviceScanner;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scanner to look for CSV files.
 */
public class CsvScanner extends DeviceScanner {
    private static final Logger logger = LoggerFactory.getLogger(CsvScanner.class);

    private static final String DEFAULT_DEVICE_SETTINGS = CsvFile.SAMPLING_MODE + "="
            + SamplingMode.LINE.toString();

    @Setting(id = "path",
             name = "CSV files directory path",
             description = "The systems path to the folder, containing the CSV files.<br><br>" + 
                           "<b>Example:</b> /home/usr/bin/openmuc/csv/"
    )
    private String path;

    private File[] files;

    private volatile boolean interrupt = false;

    public CsvScanner() {
    }

    public CsvScanner(String settings) throws ArgumentSyntaxException {
        super.configureSettings(settings);
        this.onConfigure();
    }

    @Override
    protected void onConfigure() throws ArgumentSyntaxException {
        files = listFiles();
    }

    @Override
    protected void onScan(DriverDeviceScanListener listener) 
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException {
        logger.info("Scan for CSV files in directory: {}", path);
        
        interrupt = false;
        
        if (files != null) {
            final double numberOfFiles = files.length;
            double fileCounter = 0;
            int idCounter = 0;

            for (File file : files) {

                // check if device scan was interrupted
                if (interrupt) {
                    break;
                }
                if (file.isFile()) {
                    if (file.getName().endsWith("csv")) {

                        String deviceId = "csv_device_" + idCounter;

                        listener.deviceFound(new DeviceScanInfo(deviceId, file.getAbsolutePath(),
                                DEFAULT_DEVICE_SETTINGS.toLowerCase(), file.getName()));
                    } // else: do nothing, non csv files are ignored
                } // else: do nothing, folders are ignored

                fileCounter++;
                listener.scanProgressUpdate((int) (fileCounter / numberOfFiles * 100.0));
                idCounter++;
            }
        }
    }

    @Override
    protected void onScanInterrupt() throws UnsupportedOperationException {
        interrupt = true;
    }

    private File[] listFiles() throws ArgumentSyntaxException {
        File dir = new File(path);
        if (!dir.isDirectory()) {
            throw new ArgumentSyntaxException("<path> argument must point to a directory.");
        }
        return dir.listFiles();
    }

}
