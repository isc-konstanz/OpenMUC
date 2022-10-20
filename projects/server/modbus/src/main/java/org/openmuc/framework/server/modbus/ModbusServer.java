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
package org.openmuc.framework.server.modbus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.lib.osgi.config.DictionaryPreprocessor;
import org.openmuc.framework.lib.osgi.config.PropertyHandler;
import org.openmuc.framework.lib.osgi.config.ServicePropertyException;
import org.openmuc.framework.server.modbus.register.ChannelHoldingRegister;
import org.openmuc.framework.server.modbus.register.ChannelInputRegister;
import org.openmuc.framework.server.spi.ServerMappingContainer;
import org.openmuc.framework.server.spi.ServerService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

public class ModbusServer implements ServerService, ManagedService {
    private static Logger logger = LoggerFactory.getLogger(ModbusServer.class);
    private final SimpleProcessImage spi = new SimpleProcessImage();

    private ModbusSlave slave;
    private final PropertyHandler property;
    private final Settings settings;

    public ModbusServer() {
        String pid = ModbusServer.class.getName();
        settings = new Settings();
        property = new PropertyHandler(settings, pid);
    }

    private void startServer(SimpleProcessImage spi) throws IOException {
        String address = property.getString(Settings.ADDRESS);
        int port = property.getInt(Settings.PORT);
        String type = property.getString(Settings.TYPE).toLowerCase();
        boolean isRtuTcp = false;

        logServerSettings();

        try {
            switch (type) {
            case "udp":
                slave = ModbusSlaveFactory.createUDPSlave(InetAddress.getByName(address), port);
                break;
            case "serial":
                logger.error("Serial connection is not supported, yet. Using RTU over TCP with default values.");
            case "rtutcp":
                isRtuTcp = true;
            case "tcp":
            default:
                slave = ModbusSlaveFactory.createTCPSlave(InetAddress.getByName(address), port,
                        property.getInt(Settings.POOLSIZE), isRtuTcp);
                break;
            }
            slave.setThreadName("modbusServerListener");
            slave.addProcessImage(property.getInt(Settings.UNITID), spi);
            slave.open();
        } catch (ModbusException e) {
            throw new IOException(e.getMessage());
        } catch (UnknownHostException e) {
            logger.error("Unknown host: {}", address);
            throw new IOException(e.getMessage());
        }
    }

    private void logServerSettings() {
        if (logger.isDebugEnabled()) {
            logger.debug("Address:  {}", property.getString(Settings.ADDRESS));
            logger.debug("Port:     {}", property.getString(Settings.PORT));
            logger.debug("UnitId:   {}", property.getString(Settings.UNITID));
            logger.debug("Type:     {}", property.getString(Settings.TYPE));
            logger.debug("Poolsize: {}", property.getString(Settings.POOLSIZE));
        }
    }

    void shutdown() {
        if (slave != null) {
            slave.close();
        }
    }

    @Override
    public String getId() {
        return "modbus";
    }

    @Override
    public void updatedConfiguration(List<ServerMappingContainer> mappings) {
        bindMappings(mappings);
        try {
            startServer(spi);
        } catch (IOException e) {
            logger.error("Error starting server.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serverMappings(List<ServerMappingContainer> mappings) {
        logger.debug("serverMappings");
        bindMappings(mappings);
    }

    private void bindMappings(List<ServerMappingContainer> mappings) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bind mappings of {} channel.", mappings.size());
        }

        for (final ServerMappingContainer container : mappings) {

            String serverAddress = container.getServerMapping().getServerAddress();

            PrimaryTable primaryTable = PrimaryTable
                    .getEnumfromString(serverAddress.substring(0, serverAddress.indexOf(':')));
            int modbusAddress = Integer
                    .parseInt(serverAddress.substring(serverAddress.indexOf(':') + 1, serverAddress.lastIndexOf(':')));
            String dataTypeStr = serverAddress.substring(serverAddress.lastIndexOf(':') + 1);

            DataType dataType = DataType.valueOf(dataTypeStr);

            logMapping(primaryTable, modbusAddress, dataType, container.getChannel());

            switch (primaryTable) {
            case INPUT_REGISTERS:
                addInputRegisters(spi, modbusAddress, dataType, container.getChannel());
                break;
            case HOLDING_REGISTERS:
                addHoldingRegisters(spi, modbusAddress, dataType, container.getChannel());
                break;
            case COILS:
                // TODO: create for coils
                break;
            case DISCRETE_INPUTS:
                // TODO: create for discrete inputs
                break;
            default:
            }
        }
    }

    private void logMapping(PrimaryTable primaryTable, int modbusAddress, DataType dataType, Channel channel) {
        if (logger.isDebugEnabled()) {
            logger.debug("ChannelId: {}, Register: {}, Address: {}, DataType: {}, ValueType: {}",
                    channel.getId(), primaryTable, modbusAddress, dataType, channel.getValueType());
        }
    }

    private void addHoldingRegisters(SimpleProcessImage spi, int modbusAddress, DataType dataType, Channel channel) {
        while (spi.getRegisterCount() <= modbusAddress + 4) {
            spi.addRegister(new SimpleRegister());
        }
        List<ChannelHoldingRegister> registers = new ArrayList<ChannelHoldingRegister>();
        ChannelHoldingRegister nextRegister = null;
        for (int i=0; i<dataType.getRegisterSize(); i++) {
        	int index = dataType.getRegisterSize()-1-i;
        	
        	ChannelHoldingRegister register = new ChannelHoldingRegister(channel, dataType, 2*index, 2*index+1, nextRegister);
        	registers.add(register);
        	nextRegister = register;
        }
        if (registers.size() < 1) {
        	return;
        }
        for (int i=0; i<registers.size(); i++) {
        	int index = registers.size()-1-i;
        	
        	ChannelHoldingRegister register = registers.get(index);
            logger.debug("Set {} holding register {}: {} to {}", dataType, modbusAddress+i, register.getHighByte(), register.getLowByte());
            
            spi.setRegister(modbusAddress+i, register);
        }
    }

    private void addInputRegisters(SimpleProcessImage spi, int modbusAddress, DataType dataType, Channel channel) {
        while (spi.getInputRegisterCount() <= modbusAddress + 4) {
            spi.addInputRegister(new SimpleInputRegister());
        }
        for (int i=0; i<dataType.getRegisterSize(); i++) {
        	ChannelInputRegister register = new ChannelInputRegister(channel, dataType, 2*i, 2*i+1);
            logger.debug("Set {} holding register {}: {} to {}", dataType, modbusAddress+i, register.getHighByte(), register.getLowByte());
            
            spi.setInputRegister(modbusAddress+i, register);
        }
    }

    @Override
    public void updated(Dictionary<String, ?> propertiesDict) throws ConfigurationException {
        DictionaryPreprocessor dict = new DictionaryPreprocessor(propertiesDict);
        if (!dict.wasIntermediateOsgiInitCall()) {
            tryProcessConfig(dict);
        }
    }

    private void tryProcessConfig(DictionaryPreprocessor newConfig) {
        try {
            property.processConfig(newConfig);
            if (property.configChanged()) {
                shutdown();
                startServer(spi);
            }
        } catch (ServicePropertyException | IOException e) {
            logger.error("Update properties failed", e);
            shutdown();
        }
    }

}
