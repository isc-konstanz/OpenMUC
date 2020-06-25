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

    private static final MqttLogger logger = new MqttLogger();

    @BeforeAll
    static void connect() {
        String packageName = MqttLogger.class.getPackage().getName().toLowerCase();
        System.setProperty(packageName + ".host", "localhost");
        System.setProperty(packageName + ".port", "1883");
        System.setProperty(packageName + ".username", "guest");
        System.setProperty(packageName + ".password", "guest");
        System.setProperty(packageName + ".topic", "device/data");

        logger.connect();
    }

    @Test
    void publish() {
        logger.publish("hello there".getBytes());
    }

    @Test
    void buffering() throws InterruptedException {
        logger.publish("1st message".getBytes());
        logger.publish("2nd message".getBytes());
        logger.publish("3rd message".getBytes());
        logger.publish("4th message".getBytes());
        logger.publish("5th message".getBytes());
        logger.publish("6th message".getBytes());
    }
}
