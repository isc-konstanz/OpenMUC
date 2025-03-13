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
package org.openmuc.framework.driver.opcua;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.DriverChannel;
import org.openmuc.framework.driver.annotation.Configure;

public class OpcChannel extends DriverChannel {

    @Option(id = "id",
            type = ADDRESS,
            name = "Identifier",
            description = "The identifier string for a node in the address space of an OPC UA server.")
    private String identifier;

    private NodeId nodeId;

    public NodeId getNodeId() {
        return nodeId;
    }

    @Configure
    public void setNamespace(OpcConnection connection) {
    	for (NodeId nodeId: connection.nodes) {
    		if (nodeId.getIdentifier().equals(identifier)) {
    	        this.nodeId = nodeId;
    	        return;
    		}
    	}
        nodeId = new NodeId(connection.getNamespaceIndex(), identifier);
    }

    public Record decode(DataValue data) {
        long timestamp = data.getServerTime().getJavaTime();
        Object value = data.getValue().getValue();
        switch (getValueType()) {
        case BOOLEAN:
            return new Record(new BooleanValue((Boolean) value), timestamp);
        case BYTE:
            return new Record(new ByteValue((Byte) value), timestamp);
        case SHORT:
            return new Record(new ShortValue((Short) value), timestamp);
        case INTEGER:
            return new Record(new IntValue((Integer) value), timestamp);
        case LONG:
            return new Record(new LongValue((Long) value), timestamp);
        case FLOAT:
            return new Record(new FloatValue((Float) value), timestamp);
        case DOUBLE:
            return new Record(new DoubleValue((Double) value), timestamp);    
        default:
            return new Record(new StringValue((String) value), timestamp);
        }
    }

    public DataValue encode() {
        Value value = getRecord().getValue();
        Variant variant;
        switch (getValueType()) {
        case BOOLEAN:
            variant = new Variant(value.asBoolean());
            break;
        case BYTE:
            variant = new Variant(value.asByte());
            break;
        case SHORT:
            variant = new Variant(value.asShort());
            break;
        case INTEGER:
            variant = new Variant(value.asInt());
            break;
        case LONG:
            variant = new Variant(value.asLong());
            break;
        case FLOAT:
            variant = new Variant(value.asFloat());
            break;
        case DOUBLE:
            variant = new Variant(value.asDouble());
            break;
        default:
            variant = new Variant(value.asString());
            break;
        }
        // FIXME: verify necessity of status or timestamp
        return new DataValue(variant, null, null);
    }

}
