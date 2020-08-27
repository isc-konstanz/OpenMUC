/*
 * Copyright 2011-18 Fraunhofer ISE
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.AddressSyntax;
import org.openmuc.framework.options.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AddressSyntax(separator = ";")
public class OpcConnection extends Device<OpcChannel>{
	
	private static final Logger logger = LoggerFactory.getLogger(OpcConnection.class);

	private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();
	private OpcUaClient client;

	@Address(id = "host",
			name = "Host name",
			description = "URL to the OPC server, e.g. 127.0.0.1:4840/opc. "
					+ "The prefix 'opc.tcp://' is added automatically if not entered.")
	protected String host;
	
	@Setting(id = "namespace", 
			name="channel namespace", 
			description="String of the namespace in the server", 
			valueDefault = "urn:openmuc", 
			mandatory = false)
	private String namespaceUri = "urn:openmuc";

	private int namespace;
	
	public String getHost() {
		return host;
	}

	@Override
	protected void onConfigure() {
	}

	@Override
	protected void onDisconnect() {
		future.complete(client);
	}

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
			
			if (!host.contains("opc.tcp://")) {
				host = "opc.tcp://" + host;
			}
					
			client = OpcUaClient.create(host,
					endpoints -> endpoints.stream().filter(e -> true).findFirst(),
					configBuilder -> configBuilder
							.setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
							.setApplicationUri("urn:eclipse:milo:examples:client")
							.setCertificate(loader.getClientCertificate()).setKeyPair(loader.getClientKeyPair())
							.setIdentityProvider(new AnonymousProvider()).setRequestTimeout(uint(5000))
							.build());

			client.connect().get();

	        // Get a typed reference to the Server object: ServerNode
			ServerTypeNode serverNode = client.getAddressSpace().getObjectNode(
				    Identifiers.Server,
				    ServerTypeNode.class
				).get();

	        String[] namespaceArray = serverNode.getNamespaceArray().get();
	        namespace = Arrays.asList(namespaceArray).indexOf(namespaceUri);

		} catch (Exception e) {
			logger.error("OPC connection to server failed {}", e);
		}
	}

	@Override
    protected OpcChannel onCreateChannel() throws ArgumentSyntaxException {
		return new OpcChannel(namespace);
    }

	@Override
	public Object onRead(List<OpcChannel> channels, Object containerListHandle, String samplingGroup)
			throws ConnectionException {
		long samplingTime = System.currentTimeMillis();

		for (OpcChannel channel : channels) {
			try {
				NodeId nodeId = channel.getNodeId();
		        
				VariableNode node = client.getAddressSpace().createVariableNode(nodeId);
				DataValue value = node.readValue().get();
				
				channel.setRecord(new Record(new DoubleValue(Double.parseDouble(value.getValue().getValue().toString())), 
						samplingTime, 
						Flag.VALID));
				
			} catch (InterruptedException | ExecutionException e) {
				logger.warn("Reading data from OPC server failed. {}", e);
			}
			
			
		}
		
		return null;

		
	}

}
