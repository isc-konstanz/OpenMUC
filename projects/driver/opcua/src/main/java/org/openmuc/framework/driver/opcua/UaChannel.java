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

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.api.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.api.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.driver.Channel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.AddressSyntax;
import org.openmuc.framework.options.Setting;
import org.openmuc.framework.options.SettingsSyntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AddressSyntax(separator = ";")
@SettingsSyntax(separator = ";", assignmentOperator = "=")
public class UaChannel extends Channel {
    private static final Logger logger = LoggerFactory.getLogger(UaChannel.class);

    @Address(id = "id",
             name = "Identifier",
             description = "The identifier string for a node in the address space of an OPC UA server.")
    private String identifier;

    @Setting(id="ns",
    		name = "Namespace index",
    		description = "The namespace index formatted as a base 10 number. The index an OPC UA server uses "
    				    + "for a namespace URI. The namespace URI identifies the naming authority defining the "
    				    + "identifiers of NodeIds, e.g. the OPC Foundation, other standard bodies and consortia, "
    				    + "the underlying system, the local server.")
    private int namespaceIndex = 0;

    private VariableNode node;

    private final AddressSpace addressSpace;

    public UaChannel(AddressSpace addressSpace, int namespaceDefault) {
    	this.addressSpace = addressSpace;
    	this.namespaceIndex = namespaceDefault;
    }

    @Override
    protected void onConfigure() {
    	NodeId nodeId = new NodeId(namespaceIndex, identifier);
    	this.node = addressSpace.createVariableNode(nodeId);
    }

    @Override
    protected Record onRead() throws UnsupportedOperationException, ConnectionException {
        try {
            DataValue value = node.readValue().get();
            Variant variant = value.getValue();
            switch (getValueType()) {
            case BOOLEAN:
                return new Record(new BooleanValue((Boolean) variant.getValue()), value.getServerTime().getUtcTime());
            case BYTE:
                return new Record(new ByteValue((Byte) variant.getValue()), value.getServerTime().getUtcTime());
            case SHORT:
                return new Record(new ShortValue((Short) variant.getValue()), value.getServerTime().getUtcTime());
            case INTEGER:
                return new Record(new IntValue((Integer) variant.getValue()), value.getServerTime().getUtcTime());
            case LONG:
                return new Record(new LongValue((Long) variant.getValue()), value.getServerTime().getUtcTime());
            case FLOAT:
                return new Record(new FloatValue((Float) variant.getValue()), value.getServerTime().getUtcTime());
            case DOUBLE:
                return new Record(new DoubleValue((Double) variant.getValue()), value.getServerTime().getUtcTime());    
            default:
                return new Record(new StringValue((String) variant.getValue()), value.getServerTime().getUtcTime());
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Reading data from OPC server failed. {}", e);
        }
        return new Record(new DoubleValue(Double.NaN), System.currentTimeMillis(), 
                Flag.DRIVER_ERROR_READ_FAILURE);
    }

}
