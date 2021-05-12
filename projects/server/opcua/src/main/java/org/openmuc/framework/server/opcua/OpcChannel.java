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
package org.openmuc.framework.server.opcua;

import static org.openmuc.framework.config.option.annotation.OptionType.ADDRESS;
import static org.openmuc.framework.config.option.annotation.OptionType.SETTING;

import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegate;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.config.option.annotation.OptionSyntax;
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
import org.openmuc.framework.data.Value;
import org.openmuc.framework.server.ServerChannel;

@OptionSyntax(separator = ";", assignment = "=", keyValuePairs = {ADDRESS, SETTING})
public class OpcChannel extends ServerChannel implements AttributeDelegate {

    @Option(mandatory = false)
    private String folder = null;

    @Option(id="ns",
            mandatory = false)
    private int namespaceIndex = 0;

    public String getFolder() {
        return folder;
    }

    public int getNamespaceIndex() {
        return namespaceIndex;
    }

    public NodeId getNodeType() {
        switch (getValueType()) {
        case BOOLEAN:
            return Identifiers.Boolean;
        case BYTE:
            return Identifiers.Byte;
        case BYTE_ARRAY:
            return Identifiers.ByteString;
        case SHORT:
            return Identifiers.UInt16;
        case INTEGER:
            return Identifiers.Integer;
        case LONG:
            return Identifiers.UInt64;
        case FLOAT:
            return Identifiers.Float;
        case DOUBLE:
            return Identifiers.Double;
        default:
            return Identifiers.String;
        }
    }

    @Override
    public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
        Record record = getRecord();
        if (record.getFlag() != Flag.VALID) {
            return new DataValue(StatusCode.BAD);
        }
        Value value = record.getValue();
        DateTime sourceTime = new DateTime(record.getTimestamp());
        DateTime serverTime = new DateTime(System.currentTimeMillis());
        
        switch (getValueType()) {
        case BOOLEAN:
            return new DataValue(new Variant(value.asBoolean()), StatusCode.GOOD, sourceTime, serverTime);
        case BYTE:
            return new DataValue(new Variant(value.asByte()), StatusCode.GOOD, sourceTime, serverTime);
        case SHORT:
            return new DataValue(new Variant(value.asShort()), StatusCode.GOOD, sourceTime, serverTime);
        case INTEGER:
            return new DataValue(new Variant(value.asInt()), StatusCode.GOOD, sourceTime, serverTime);
        case LONG:
            return new DataValue(new Variant(value.asLong()), StatusCode.GOOD, sourceTime, serverTime);
        case FLOAT:
            return new DataValue(new Variant(value.asFloat()), StatusCode.GOOD, sourceTime, serverTime);
        case DOUBLE:
            return new DataValue(new Variant(value.asDouble()), StatusCode.GOOD, sourceTime, serverTime);
        default:
            return new DataValue(new Variant(value.asString()), StatusCode.GOOD, sourceTime, serverTime);
        }
    }

    @Override
    public void setValue(AttributeContext context, VariableNode node, DataValue value) throws UaException {
        Variant variant = value.getValue();
        Record record = null;
        switch (getValueType()) {
        case BOOLEAN:
            record = new Record(new BooleanValue((Boolean) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        case BYTE:
            record = new Record(new ByteValue((Byte) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        case SHORT:
            record = new Record(new ShortValue((Short) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        case INTEGER:
            record = new Record(new IntValue((Integer) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        case LONG:
            record = new Record(new LongValue((Long) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        case FLOAT:
            record = new Record(new FloatValue((Float) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        case DOUBLE:
            record = new Record(new DoubleValue((Double) variant.getValue()), value.getServerTime().getUtcTime());    
            break;
        default:
            record = new Record(new StringValue((String) variant.getValue()), value.getServerTime().getUtcTime());
            break;
        }
        setRecord(record);
    }

}
