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

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.openmuc.framework.driver.Channel;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.Setting;

public class OpcChannel extends Channel {

    @Address(id = "nodeId",
             name = "Node ID",
             description = "Use an inverted pin state logic."
             )
    private String address;

    @Setting(id="ns",
    		name = "missing name",
    		description = "missing description",
    		mandatory = false
    		)
    private int namespace = 1;
    
    private NodeId nodeId;
    
    public OpcChannel(int namespace) {
    	this.namespace = namespace;
    }

    @Override
    protected void onConfigure() {
    	nodeId = new NodeId(namespace, address);
    }
    
    public NodeId getNodeId() {
    	return nodeId;
    }

    
}
