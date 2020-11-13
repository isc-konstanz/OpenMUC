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
package org.openmuc.framework.driver.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.address.Address;
import org.openmuc.framework.config.address.AddressSyntax;
import org.openmuc.framework.config.settings.Setting;
import org.openmuc.framework.config.settings.SettingsSyntax;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AddressSyntax(separator = ";")
@SettingsSyntax(separator = ";", assignmentOperator = "=")
public class UaConnection extends Device<UaChannel>{
    private static final Logger logger = LoggerFactory.getLogger(UaConnection.class);

    private OpcUaClient client;

    @Address(id = "address",
             name = "Server address",
             description = "The address to the OPC server, e.g. 192.168.178.48:4840/opc.")
    protected String address;

    @Setting(id = "ns", 
             name="Namespace default", 
             description="The default namespace string to address on the server",
             mandatory = false)
    private String namespaceUri = null;

    private int namespaceIndex = 0;

    @Override
    protected void onConnect() throws ConnectionException {
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
            
            if (namespaceUri != null) {
                String[] namespaceArray = serverNode.getNamespaceArray().get();
                this.namespaceIndex = Arrays.asList(namespaceArray).indexOf(namespaceUri);
            }
        } catch (Exception e) {
            logger.error("OPC connection to server failed {}", e);
        }
    }

    @Override
    protected void onDisconnect() {
        client.disconnect();
    }

    @Override
    protected UaChannel onCreateChannel() throws ArgumentSyntaxException {
        return new UaChannel(client.getAddressSpace(), namespaceIndex);
    }

}
