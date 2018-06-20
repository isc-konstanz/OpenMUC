package org.openmuc.framework.driver.modbus.test;

import org.junit.Test;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.driver.modbus.ModbusDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverTest {

    private static final Logger logger = LoggerFactory.getLogger(DriverTest.class);

    @Test
    public void printDriverInfo() {
        ModbusDriver driver = new ModbusDriver();
        DriverInfo info = driver.getInfo();

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("Driver Id = " + info.getId() + "\n");
        sb.append("Description = " + info.getDescription() + "\n");
        sb.append("DeviceAddressSyntax = " + info.getDeviceAddress().getSyntax() + "\n");
        sb.append("SettingsSyntax = " + info.getDeviceSettings().getSyntax() + "\n");
        sb.append("ChannelAddressSyntax = " + info.getChannelAddress().getSyntax() + "\n");
        sb.append("DeviceScanSettingsSyntax = " + info.getDeviceScanSettings().getSyntax() + "\n");
        logger.info(sb.toString());

    }

}
