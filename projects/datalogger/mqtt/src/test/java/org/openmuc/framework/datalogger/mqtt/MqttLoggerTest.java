/*
 * Copyright 2011-2020 Fraunhofer ISE
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

package org.openmuc.framework.datalogger.mqtt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class MqttLoggerTest {

    private static final MqttLogger mqttLogger = new MqttLogger();

    @BeforeAll
    static void connect() {
        String packageName = MqttLogger.class.getPackage().getName().toLowerCase();
        System.setProperty(packageName + ".host", "localhost");
        System.setProperty(packageName + ".port", "1883");
        System.setProperty(packageName + ".username", "guest");
        System.setProperty(packageName + ".password", "guest");
        System.setProperty(packageName + ".topic", "device/data");

        System.setProperty(packageName + ".maxFileCount", "2");
        System.setProperty(packageName + ".maxFileSize", "1");
        System.setProperty(packageName + ".maxBufferSize", "1");

        mqttLogger.connect();
    }

    /**
     * Complete test of file buffering from logger's point of view.
     * <p>
     * Scenario: Logger connects to a broker, after some while connection to broker is interrupted. Now, logger should
     * log into a file. After some time the connection to the broker is reestablished. Now logger should transfer all
     * buffered messages to the broker and clear the file (buffer) afterwards. At the same time new live logs should be
     * send to the broker as well (in parallel)
     */
    @Test
    void testFileBuffering() {

        // Involves: mqtt logger, lib-mqtt, lib-FilePersistence
        // Note: lib-FilePersistence has it own tests for correct parameter handling

        // 1. start logger and connect to a BrokerMock (just print messages to terminal)
        // (executor which calls log every second)

        // 2. log a few messages to terminal

        // 3. interrupt/close connection of BrokerMock

        // 4. logger should log into file. check if it does.

        // 5. reconnect to the BrokerMock

        // 6. empty file buffer and send (historical) messages to broker AND send live log messages to broker as well
    }
}
