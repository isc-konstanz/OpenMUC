package org.openmuc.framework.driver.csv.test;

import org.junit.Test;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.info.DeviceOptions;
import org.openmuc.framework.config.info.Settings;
import org.openmuc.framework.driver.csv.CsvDriver;
import org.openmuc.framework.driver.csv.settings.CsvDeviceOptions;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceScanSettingsTest {

    private final static Logger logger = LoggerFactory.getLogger(DeviceScanSettingsTest.class);

    private final static DeviceOptions DEVICE_OPTIONS = new CsvDeviceOptions();
    
    // Tests expected to be OK

    String dir = System.getProperty("user.dir");

    @Test
    public void testArgumentCorrectEndingWithSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources";
        DEVICE_OPTIONS.parseScanSettings(settings);
    }

    @Test
    public void testArgumentCorrectendingWithoutSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources/";
        DEVICE_OPTIONS.parseScanSettings(settings);
    }

    // Tests expected to FAIL

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsNull() throws ArgumentSyntaxException {
        String arguments = null;
        DEVICE_OPTIONS.parseScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsEmptyString() throws ArgumentSyntaxException {
        String arguments = "";
        DEVICE_OPTIONS.parseScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgument() throws ArgumentSyntaxException {
        String arguments = "paaaaath";
        DEVICE_OPTIONS.parseScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete1() throws ArgumentSyntaxException {
        String arguments = "path";
        DEVICE_OPTIONS.parseScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete2() throws ArgumentSyntaxException {
        String arguments = "path=";
        DEVICE_OPTIONS.parseScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgumentPathDoesNotExist() throws ArgumentSyntaxException, UnsupportedOperationException, ScanException, ScanInterruptedException {
        String arguments = "path=/home/does_not_exist";
        
        CsvDriver csvDriver = new CsvDriver();
        csvDriver.scanForDevices(arguments, new DriverDeviceScanListener() {

            @Override
            public void scanProgressUpdate(int progress) {
                logger.info("Scan progress: " + progress + " %");
            }

            @Override
            public void deviceFound(DeviceScanInfo scanInfo) {
                logger.info(scanInfo.toString());
            }
        });        
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgumentNoDirctory() throws ArgumentSyntaxException, UnsupportedOperationException, ScanException, ScanInterruptedException {
        String arguments = "path=/home/mmittels/git/openmuc/projects/driver/csv/resources/CsvTestDevice_1.csv";
        
        CsvDriver csvDriver = new CsvDriver();
        csvDriver.scanForDevices(arguments, new DriverDeviceScanListener() {

            @Override
            public void scanProgressUpdate(int progress) {
                logger.info("Scan progress: " + progress + " %");
            }

            @Override
            public void deviceFound(DeviceScanInfo scanInfo) {
                logger.info(scanInfo.toString());
            }
        });  
    }

}
