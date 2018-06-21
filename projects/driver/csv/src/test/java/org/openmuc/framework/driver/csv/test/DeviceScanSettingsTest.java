package org.openmuc.framework.driver.csv.test;

import org.junit.Test;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.csv.CsvDriver;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceScanSettingsTest {

    private static final Logger logger = LoggerFactory.getLogger(DeviceScanSettingsTest.class);

    private final DriverInfo info = DriverInfoFactory.getInfo(CsvDriver.class);

    // Tests expected to be OK

    String dir = System.getProperty("user.dir");

    @Test
    public void testArgumentCorrectEndingWithSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources";
        info.parse(settings, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test
    public void testArgumentCorrectendingWithoutSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources/";
        info.parse(settings, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    // Tests expected to FAIL

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsNull() throws ArgumentSyntaxException {
        String arguments = null;
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsEmptyString() throws ArgumentSyntaxException {
        String arguments = "";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgument() throws ArgumentSyntaxException {
        String arguments = "paaaaath";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete1() throws ArgumentSyntaxException {
        String arguments = "path";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete2() throws ArgumentSyntaxException {
        String arguments = "path=";
        info.parse(arguments, PreferenceType.SETTINGS_SCAN_DEVICE);
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
