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
package org.openmuc.framework.server.opcua;

import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegate;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.openmuc.framework.options.Setting;
import org.openmuc.framework.server.spi.Channel;

public class UaChannel extends Channel implements AttributeDelegate {

	@Setting(id="ns")
	private String namespace;

    @Override
    public DataValue getValue(AttributeContext context, VariableNode node) throws UaException {
    	DataValue value = null;
    	switch (getValueType()) {
		case BOOLEAN:
			break;
		case BYTE:
			break;
		case BYTE_ARRAY:
			break;
		case SHORT:
			break;
		case INTEGER:
			break;
		case LONG:
			break;
		case FLOAT:
			break;
		case DOUBLE:
			break;
		case STRING:
			break;
		default:
			break;
    	}
    	return value;
    }

    @Override
    public void setValue(AttributeContext context, VariableNode node, DataValue value) throws UaException {
    	switch (getValueType()) {
		case BOOLEAN:
			break;
		case BYTE:
			break;
		case BYTE_ARRAY:
			break;
		case SHORT:
			break;
		case INTEGER:
			break;
		case LONG:
			break;
		case FLOAT:
			break;
		case DOUBLE:
			break;
		case STRING:
			break;
		default:
			break;
    	}
    }

}
