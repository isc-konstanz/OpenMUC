package org.openmuc.framework.driver.csv;

import java.io.File;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.options.ChannelOptions;
import org.openmuc.framework.config.options.DeviceOptions;
import org.openmuc.framework.config.options.Parameters;
import org.openmuc.framework.driver.csv.options.CsvChannelOptions;
import org.openmuc.framework.driver.csv.options.CsvDeviceOptions;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO wo legen wir die csv dateien ab damit sie im release zugänglich sind?
//a) in treiber integrieren (die wären dann fix) (settings leer lassen?)
//b) nutzer könnten über settings anderes verzeichnis angeben

// datei mit chronologisch aufsteigenden zeitstempeln oder ohne zeitstempel
@Component
public class CsvDriver implements DriverService {

    private final static Logger logger = LoggerFactory.getLogger(CsvDriver.class);

    // Settings mode realtime, nextline-rewind, nextline
    // Settings seperator = ;
    // Settings comment = #

    private final static String DEFAULT_SETTINGS = CsvDeviceOptions.SAMPLING_MODE + "="
            + CsvDeviceOptions.SAMPLING_MODE_DEFAULT.toString();

    private final static String ID = "csv";
    private final static String NAME = "CSV";
    private final static String DESCRIPTION = "The CSV Driver reads out values from configured files. "
            + "Several columns from different CSV files may be read line by line or by index.";
    private final static DeviceOptions DEVICE_OPTIONS = new CsvDeviceOptions();
    private final static ChannelOptions CHANNEL_OPTIONS = new CsvChannelOptions();
    private final static DriverInfo DRIVER_INFO = new DriverInfo(ID, NAME, DESCRIPTION, DEVICE_OPTIONS, CHANNEL_OPTIONS);

    private boolean isDeviceScanInterrupted = false;

    @Override
    public DriverInfo getInfo() {
        return DRIVER_INFO;
    }

    @Override
    public void scanForDevices(String settings, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {

        logger.info("Scan for CSV files. Settings: " + settings);

        // reset interrupted flag on start of scan
        isDeviceScanInterrupted = false;

        Parameters deviceScanSettings = DEVICE_OPTIONS.parseScanSettings(settings);
        String path = deviceScanSettings.getString(CsvDeviceOptions.PATH_DIR);
        File[] listOfFiles;
        if (!path.isEmpty()) {
            File file = new File(path);
            if (!file.isDirectory()) {
                throw new ArgumentSyntaxException("<path> argument must point to a directory.");
            }
            listOfFiles = file.listFiles();
        }
        else {
            throw new ArgumentSyntaxException("<path> argument must point to a directory.");
        }

        if (listOfFiles != null) {

            double numberOfFiles = listOfFiles.length;
            double fileCounter = 0;

            int idCounter = 0;

            for (File file : listOfFiles) {
                if (isDeviceScanInterrupted) {
                    break;
                }

                if (file.isFile()) {
                    if (file.getName().endsWith("csv")) {

                        String deviceId = "csv_device_" + idCounter;

                        listener.deviceFound(new DeviceScanInfo(deviceId, file.getAbsolutePath(),
                                DEFAULT_SETTINGS.toLowerCase(), file.getName()));
                    } // else: do nothing, non csv files are ignored
                } // else: do nothing, folders are ignored

                fileCounter++;
                listener.scanProgressUpdate((int) (fileCounter / numberOfFiles * 100.0));
                idCounter++;
            }
        }
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        isDeviceScanInterrupted = true;
    }

    @Override
    public Connection connect(String deviceAddress, String settings)
            throws ArgumentSyntaxException, ConnectionException {

        CsvDeviceConnection csvConnection = new CsvDeviceConnection(deviceAddress, settings);
        logger.debug("csv driver connected");
        return csvConnection;
    }

}
