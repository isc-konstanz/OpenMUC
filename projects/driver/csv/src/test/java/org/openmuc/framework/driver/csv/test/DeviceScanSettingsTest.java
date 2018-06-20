package org.openmuc.framework.driver.csv.test;

import org.junit.Test;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.csv.CsvDriver;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceScanSettingsTest {

    private static final Logger logger = LoggerFactory.getLogger(DeviceScanSettingsTest.class);

    final static DriverInfo info = new DriverInfo(CsvDriver.class.getResourceAsStream("options.xml"));
    
    // Tests expected to be OK

    String dir = System.getProperty("user.dir");

    @Test
    public void testArgumentCorrectEndingWithSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources";
        info.parseDeviceScanSettings(settings);
    }

    @Test
    public void testArgumentCorrectendingWithoutSlash() throws ArgumentSyntaxException {
        String settings = "path=" + dir + "/src/test/resources/";
        info.parseDeviceScanSettings(settings);
    }

    // Tests expected to FAIL

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsNull() throws ArgumentSyntaxException {
        String arguments = null;
        info.parseDeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentsEmptyString() throws ArgumentSyntaxException {
        String arguments = "";
        info.parseDeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testWrongArgument() throws ArgumentSyntaxException {
        String arguments = "paaaaath";
        info.parseDeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete1() throws ArgumentSyntaxException {
        String arguments = "path";
        info.parseDeviceScanSettings(arguments);
    }

    @Test(expected = ArgumentSyntaxException.class)
    public void testArgumentIncomplete2() throws ArgumentSyntaxException {
        String arguments = "path=";
        info.parseDeviceScanSettings(arguments);
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
