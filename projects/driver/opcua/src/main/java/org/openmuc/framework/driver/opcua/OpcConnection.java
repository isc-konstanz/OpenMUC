/*
 * Copyright 2011-2021 Fraunhofer ISE
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
package org.openmuc.framework.driver.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.DriverDevice;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Device;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.annotation.Write;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Device(channel = OpcChannel.class)
public class OpcConnection extends DriverDevice {
    private static final Logger logger = LoggerFactory.getLogger(OpcConnection.class);

    private OpcUaClient client;

    @Option(type = ADDRESS,
            name = "Server address",
            description = "The address to the OPC server, e.g. 192.168.178.48:4840/opc.")
    protected String address;

    @Option(type = SETTING, 
    		id = "ns", 
            name="Namespace default", 
            description="The default namespace string to address on the server",
            mandatory = false)
    private String namespaceUri = null;

    private int namespaceIndex = 0;

    public int getNamespaceIndex() {
    	return namespaceIndex;
    }

    @Connect
    public void connect() throws ConnectionException {
        try {
            Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
            Files.createDirectories(securityTempDir);
            if (!Files.exists(securityTempDir)) {
                throw new ConnectionException("Unable to create security dir: " + securityTempDir);
            }
            logger.debug("Security temp dir: {}", securityTempDir.toAbsolutePath());

            KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

            if (!address.contains("opc.tcp://")) {
                address = "opc.tcp://" + address;
            }
            client = OpcUaClient.create(address,
                    endpoints -> endpoints.stream().filter(e -> true).findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english("OpenMUC OPC UA Client"))
                            .setApplicationUri("urn:openmuc:client")
                            .setCertificate(loader.getClientCertificate()).setKeyPair(loader.getClientKeyPair())
                            .setIdentityProvider(new AnonymousProvider()).setRequestTimeout(uint(5000))
                            .build());
            
            client.connect().get();
            
            // Get a typed reference to the Server object: ServerNode
            ServerTypeNode serverNode = client.getAddressSpace().getObjectNode(
                    Identifiers.Server,
                    ServerTypeNode.class
                ).get();
            
            if (namespaceUri != null && 
                    !namespaceUri.isEmpty()) {
                try {
                    namespaceIndex = Integer.parseInt(namespaceUri);
                    
                } catch(NumberFormatException e) {
                    
                    namespaceIndex = Arrays.asList(serverNode.getNamespaceArray().get()).indexOf(namespaceUri);
                }
            }
        } catch (Exception e) {
            logger.error("OPC connection to server failed {}", e);
        }
    }

    @Disconnect
    public void close() {
        client.disconnect();
    }

    @Read
    public void read(List<OpcChannel> channels, String samplingGroup) 
            throws ConnectionException {
        try {
            List<NodeId> nodeIds = channels.stream().map(c -> c.getNodeId()).collect(Collectors.toList());
            List<DataValue> values = client.readValues(0.0, TimestampsToReturn.Both, nodeIds).get();
            
            for (OpcChannel channel : channels) {
                try {
                    int index = nodeIds.indexOf(channel.getNodeId());
                    channel.setRecord(channel.decode(values.get(index)));
                    
                } catch (NullPointerException e) {
                    channel.setRecord(new Record(new DoubleValue(Double.NaN), System.currentTimeMillis(), 
                            Flag.DRIVER_ERROR_READ_FAILURE));
                }
            }
        } catch (InterruptedException e) {
            for (OpcChannel channel : channels) {
                channel.setRecord(new Record(new DoubleValue(Double.NaN), System.currentTimeMillis(), 
                            Flag.DRIVER_ERROR_TIMEOUT));
            }
        } catch (ExecutionException | NullPointerException e) {
            logger.warn("Reading data from OPC server failed. {}", e);
            throw new ConnectionException(e);
        }
    }

    @Write
    public void write(List<OpcChannel> channels)
            throws ConnectionException {
        try {
            for (OpcChannel channel : channels) {
                DataValue value = channel.encode();
                try {
                    StatusCode status = client.writeValue(channel.getNodeId(), value).get();
                    
                    if (status.isGood()) {
                        channel.setFlag(Flag.VALID);
                    }
                    else {
                        logger.warn("Writing data to OPC UA channel {} failed: {}", channel.getId(), status.toString());
                    }
                } catch (InterruptedException e) {
                    channel.setFlag(Flag.DRIVER_ERROR_TIMEOUT);
                }
            }
        } catch (ExecutionException e) {
            logger.warn("Writing data to OPC server failed. {}", e);
            throw new ConnectionException(e);
        }
    }

}
