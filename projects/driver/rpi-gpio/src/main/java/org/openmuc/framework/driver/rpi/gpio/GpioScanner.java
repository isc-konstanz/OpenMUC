/*
 * Copyright 2011-2022 Fraunhofer ISE
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
package org.openmuc.framework.driver.rpi.gpio;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.Syntax;
import org.openmuc.framework.driver.DriverDeviceScanner;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo.BoardType;

@Syntax(separator = ",", assignment = ":", keyValuePairs = { ADDRESS, SETTING })
public class GpioScanner extends DriverDeviceScanner {
    private static final Logger logger = LoggerFactory.getLogger(GpioScanner.class);

    @Option(id = GpioConfigs.MODE,
            type = SETTING,
            name = "I/O mode",
            valueSelection = "DIGITAL_INPUT:Input,DIGITAL_OUTPUT:Output"
    )
    private PinMode mode;

    private BoardType board = BoardType.RaspberryPi_Unknown;

    private volatile boolean interrupt = false;

    @Override
    public void scan(DriverDeviceScanListener listener) 
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException {
        
        logger.info("Scan for {}s of the Raspberry Pi platform: {}", 
                mode.name().toLowerCase().replace('_', ' '), board.name().replace('_', ' '));
        
        interrupt = false;
        
        int counter = 1;
        Pin[] pins = RaspiPin.allPins(board);
        for (Pin pin : pins) {
            if (interrupt) {
                break;
            }
            if (pin.getSupportedPinModes().contains(mode)) {
                String scanAddress = GpioConfigs.PIN + ":" + pin.getAddress();
                String scanSettings = GpioConfigs.MODE + ":" + mode.name();
                
                listener.deviceFound(new DeviceScanInfo("Pin"+pin.getAddress(), 
                        scanAddress, scanSettings, pin.getName()));
                
                listener.scanProgressUpdate((int) Math.round(counter/(double) pins.length*100));
                counter++;
            }
        }
    }

    @Override
    public void interrupt() throws UnsupportedOperationException {
        interrupt = true;
    }

}
