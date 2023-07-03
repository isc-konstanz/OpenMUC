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

package org.openmuc.framework.driver.mqtt;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.lib.mqtt.MqttSettings;

public class MqttDriverSettings extends Configurable {

    public static class MqttChannelAddress extends Configurable {
        @Option(type = ADDRESS,
                name = "Topic",
                description = "Name of the mqtt topic",
                mandatory = true
        )
        private String topic;

        public String getTopic() {
            return topic;
        }

        public MqttChannelAddress(String address) throws ArgumentSyntaxException {
            this.configure(ADDRESS, address);
        }
    }

    @Option(type = ADDRESS,
            name = "Host",
            description = "URL of the mqtt broker, e.g. localhost, 192.168.8.4, ...",
            mandatory = true
    )
    private String host;

    @Option(type = SETTING,
            name = "Port",
            description = "Port for MQTT communication",
            mandatory = true
    )
    private int port;

    @Option(type = SETTING,
            name = "Parser",
            description = "Identifier of needed parser implementation e.g. <em>openmuc</em>",
            mandatory = true
    )
    private String parser;

    @Option(type = SETTING,
            name = "Username",
            description = "Name of your MQTT account",
            mandatory = false
    )
    private String username;

    @Option(type = SETTING,
            name = "Password",
            description = "Password of your MQTT account",
            mandatory = false
    )
    private String password;

    @Option(type = SETTING,
            name = "Record collection size",
            description = "This parameter makes it possible to optimize the performance of " +
                   "listening and logging huge amounts of records. The driver waits until " +
                   "the configured number of records is collected, before returning the " +
                   "list to the data manager. This decreases the number of needed tasks " +
                   "e.g. for writing to a database.",
            valueDefault = "1",
            mandatory = false
    )
    private int recordCollectionSize = 1;

    @Option(type = SETTING,
            name = "SSL",
            description = "<em>true</em> enable ssl, <em>false</em> disable ssl",
            valueDefault = "false",
            mandatory = false
    )
    private boolean ssl = false;

    @Option(type = SETTING,
            name = "Maximum buffer size",
            description = "Maximum buffer size in kB. If limit is reached than buffer will be written to file.",
            valueDefault = "0",
            mandatory = false
    )
    private long maxBufferSize = 0;

    @Option(type = SETTING,
            name = "Maximum file size",
            description = "Maximum file size in kB",
            valueDefault = "0",
            mandatory = false
    )
    private long maxFileSize = 0;

    @Option(type = SETTING,
            name = "Maximum files",
            description = "Number of files to be created for buffering",
            valueDefault = "1",
            mandatory = false
    )
    private int maxFileCount = 1;

    @Option(type = SETTING,
            name = "Connection retry interval",
            description = "Connection retry interval in s – reconnect after given seconds when connection fails",
            valueDefault = "10",
            mandatory = false
    )
    private int connectionRetryInterval = 10;

    @Option(type = SETTING,
            name = "Connection alive interval",
            description = "Connection alive interval in s – periodically send PING message to broker to detect broken connections",
            valueDefault = "10",
            mandatory = false
    )
    private int connectionAliveInterval = 10;

    @Option(type = SETTING,
            name = "First will topic",
            description = "Topic on which firstWillPayload will be published on successful connections",
            mandatory = false
    )
    private String firstWillTopic = "";

    @Option(type = SETTING,
            name = "First will payload",
            description = "Payload of the first will message",
            mandatory = false
    )
    private byte[] firstWillPayload = "".getBytes();

    @Option(type = SETTING,
            name = "Last will topic",
            description = "Topic on which lastWillPayload will be published",
            mandatory = false
    )
    private String lastWillTopic = "";

    @Option(type = SETTING,
            name = "Last will payload",
            description = "Payload of the last will message",
            mandatory = false
    )
    private byte[] lastWillPayload = "".getBytes();

    @Option(type = SETTING,
            name = "Last will (always)",
            description = "<em>true</em>: publish last will payload on every disconnection, " +
                   "including intended disconnects by the client. " +
                   "<em>false</em> publish only on errors/connection interrupts",
            valueDefault = "false",
            mandatory = false
    )
    private boolean lastWillAlways = false;

    @Option(type = SETTING,
            name = "Persistence directory",
            description = "directory to store data for file buffering e.g. <em>data/driver/mqtt</em>",
            valueDefault = "data/driver/mqtt",
            mandatory = false
    )
    private String persistenceDirectory = "data/driver/mqtt";

    @Option(type = SETTING,
            name = "WebSocket",
            description = "Usage of a WebSocket",
            valueDefault = "false",
            mandatory = false
    )
    private boolean webSocket = false;

    public MqttDriverSettings(String address, String settings) throws ArgumentSyntaxException {
        this.configure(SETTING, settings);
        this.host = address;
    }

    public String getParser() {
        return parser;
    }

    public int getRecordCollectionSize() {
        return recordCollectionSize;
    }

    public boolean isSsl() {
        return ssl;
    }

    public MqttSettings getSettings() {
        return new MqttSettings(host, port, username, password, ssl, maxBufferSize, maxFileSize, maxFileCount,
                                connectionRetryInterval, connectionAliveInterval, persistenceDirectory, 
                                lastWillTopic, lastWillPayload, lastWillAlways, firstWillTopic, firstWillPayload, 
                                webSocket);
    }

}
