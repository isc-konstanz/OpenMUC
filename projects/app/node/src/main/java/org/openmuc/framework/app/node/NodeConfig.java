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
package org.openmuc.framework.app.node;

import java.util.Arrays;
import java.util.List;

public class NodeConfig {

    private static final String NODE_ID_KEY = "org.openmuc.framework.app.node.id";
    private static final String NODE_ID_DEFAULT = "node";

    private static final String PLUS_ID_KEY = "org.openmuc.framework.app.node.plus";
    private static final String MINUS_ID_KEY = "org.openmuc.framework.app.node.minus";

    private static final String SEP_KEY = "org.openmuc.framework.app.node.separator";
    private static final String SEP_DEFAULT = ",";

    public String getNodeChannel() {
        return System.getProperty(NODE_ID_KEY, NODE_ID_DEFAULT);
    }

    public List<String> getPlusChannels() throws IllegalArgumentException {
        String channels = System.getProperty(PLUS_ID_KEY, null);
        if (channels == null || channels.isEmpty()) {
        	throw new IllegalArgumentException("Missing or invalid property: "+PLUS_ID_KEY);
        }
        return Arrays.asList(channels.split(getSeparator()));
    }

    public List<String> getMinusChannels() throws IllegalArgumentException {
        String channels = System.getProperty(MINUS_ID_KEY, null);
        if (channels == null || channels.isEmpty()) {
        	throw new IllegalArgumentException("Missing or invalid property: "+MINUS_ID_KEY);
        }
        return Arrays.asList(channels.split(getSeparator()));
    }

    public String getSeparator() {
        return System.getProperty(SEP_KEY, SEP_DEFAULT);
    }

}
