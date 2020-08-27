/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openmuc.framework.server.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class ChannelNamespace extends ManagedNamespace {

    static final String NAMESPACE_URI = "urn:openmuc";

    private final SubscriptionModel subscriptionModel;

//    private final Map<String, UaFolderNode> folders = new HashMap<String, UaFolderNode>();

    ChannelNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);
        subscriptionModel = new SubscriptionModel(server, this);
    }

    public void addChannelNode(UaChannel channel) throws UaException {
    	UaFolderNode folderNode;
    	String folderName = channel.getFolder();

    	//Adding folder to address Space
     	if(folderName != null) {
			folderNode = new UaFolderNode(
	                getNodeContext(),
	                newNodeId(folderName),
	                newQualifiedName(folderName),
	                LocalizedText.english(folderName));
			
	        folderNode.addReference(new Reference(
	                folderNode.getNodeId(),
	                Identifiers.Organizes,
	                Identifiers.ObjectsFolder.expanded(),
	                false));

     	}else {
			folderNode = new UaFolderNode(
	                getNodeContext(),
	                Identifiers.ObjectsFolder,
	                newQualifiedName("ObjectsFolder"),
	                LocalizedText.english("ObjectsFolder"));
			
	        folderNode.addReference(new Reference(
	                folderNode.getNodeId(),
	                Identifiers.Organizes,
	                Identifiers.RootFolder.expanded(),
	                false));
    	}
		getNodeManager().addNode(folderNode);
		
        //Adding variable to address space
     	AttributeContext context = new AttributeContext(getServer());

     	String name = channel.getDescription();
        if (name.isEmpty()) {
            name = channel.getId();
        }

        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId(channel.getId()))
                .setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE)))
                .setBrowseName(newQualifiedName(channel.getId()))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(channel.getNodeType())
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        node.setValue(channel.getValue(context, node));
        node.setAttributeDelegate(channel);
        
        getNodeManager().addNode(node);
        folderNode.addOrganizes(node);
    }

    @Override
    protected void onStartup() {
        super.onStartup();

    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

}
