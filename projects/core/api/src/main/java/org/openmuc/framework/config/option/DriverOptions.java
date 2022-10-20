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
package org.openmuc.framework.config.option;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DriverOptions extends DriverInfo {
    private static final Logger logger = LoggerFactory.getLogger(DriverOptions.class);

    protected DriverOptions(DeviceOptions device) {
        super(device);
    }

    protected DriverOptions(DeviceOptions device, String id, String name, String description) {
        super(device, id, name, description);
    }

    @Override
    public DriverOptions setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public DriverOptions setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    @Override
    public DeviceOptions getDevice() {
        return (DeviceOptions) super.getDevice();
    }

    @Override
    public ChannelOptions getChannel() {
        return (ChannelOptions) super.getChannel();
    }

    public Options getDriverConfigs() throws ParseException, IOException {
        return readDriverConfigs();
    }

    public static Options readDriverConfigs() throws ParseException, IOException {
        return readConfigs("driver");
    }

    static Options readConfigs(String type) throws ParseException, IOException {

        //Get file from resources folder
        InputStream is = DriverOptions.class.getResourceAsStream(type+".xml");
        if (is == null) {
            throw new IOException("Driver info options resource not found");
        }
        
        DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();
        docBFac.setIgnoringComments(true);
        
        Document doc;
        try {
            doc = docBFac.newDocumentBuilder().parse(is);
        } catch (Exception e) {
            throw new ParseException(e);
        }
        
        Node node = doc.getDocumentElement();
        if (!node.getNodeName().equals("options")) {
            throw new ParseException("Root node in default options is not of type \"options\"");
        }
        return Options.fromDomNode(node);
    }

    public static class DriverConfigs extends DriverOptions {

        /**
         * Constructor to set driver info
         * 
         * @param id
         *            driver ID
         */
        DriverConfigs(String id) {
            super(new DeviceOptions.DeviceConfigs(
                  new ChannelOptions.ChannelConfigs()));
            
            this.id = id;
        }

        /**
         * Constructor to set driver info
         * 
         * @param is
         *            resource stream, containing all option info as XML nodes
         */
        DriverConfigs(InputStream is) {
            super(new DeviceOptions.DeviceConfigs(
                  new ChannelOptions.ChannelConfigs()));
            
            if (is != null) {
                try {
                    DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();
                    docBFac.setIgnoringComments(true);
                    
                    Document doc = docBFac.newDocumentBuilder().parse(is);
                    Node node = doc.getDocumentElement();
                    if (!node.getNodeName().equals("options") && !node.getNodeName().equals("configuration") ) {
                        logger.warn("Root node in driver \"{}\" options is not of type \"configuration\"", id);
                        return;
                    }
                    
                    NodeList childNodes = node.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node childNode = childNodes.item(i);
                        String childNodeName = childNode.getNodeName();
                        if (childNodeName.equals("#text")) {
                            continue;
                        }
                        else if(childNodeName.equals("driver")) {
                            readFromDomNode(childNode);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error while reading driver \"{}\" options: {}", id, e.getMessage());
                }
            }
            else {
                logger.warn("Driver info options resource not found");
            }
        }

        private void readFromDomNode(Node node) throws ParseException {
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttribute = attributes.getNamedItem("id");
            if (nameAttribute == null) {
                throw new ParseException("Driver info has no id attribute");
            }
            this.id = nameAttribute.getTextContent();
            
            Map<String, OptionValue> optionsById = new HashMap<String, OptionValue>();
            
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                String childNodeName = childNode.getNodeName();
                if (childNodeName.equals("#text")) {
                    continue;
                }
                else if (childNodeName.equals("name")) {
                    this.name = childNode.getTextContent();
                }
                else if (childNodeName.equals("description")) {
                    this.description = Options.trimDomNodeText(childNode);
                }
                else if (childNodeName.equals("deviceAddress")) {
                    this.getDevice().setAddressOptions(Options.fromDomNode(childNode, optionsById));
                }
                else if (childNodeName.equals("deviceSettings")) {
                    this.getDevice().setSettingsOptions(Options.fromDomNode(childNode, optionsById));
                }
                else if (childNodeName.equals("deviceScanSettings")) {
                    this.getDevice().setScanSettingsOptions(Options.fromDomNode(childNode, optionsById));
                }
                else if (childNodeName.equals("channelAddress")) {
                    this.getChannel().setAddressOptions(Options.fromDomNode(childNode, optionsById));
                }
                else if (childNodeName.equals("channelSettings")) {
                    this.getChannel().setSettingsOptions(Options.fromDomNode(childNode, optionsById));
                }
                else if (childNodeName.equals("channelScanSettings")) {
                    this.getChannel().setScanSettingsOptions(Options.fromDomNode(childNode, optionsById));
                }
                else {
                    throw new ParseException("Unknown tag found:" + childNodeName);
                }
            }
        }

        @Override
        public DriverConfigs setName(String name) {
            super.setName(name);
            return this;
        }

        @Override
        public DriverConfigs setDescription(String description) {
            super.setDescription(description);
            return this;
        }

        @Override
        public DeviceOptions.DeviceConfigs getDevice() {
            return (DeviceOptions.DeviceConfigs) super.getDevice();
        }

        public DriverConfigs setDevice(Class<? extends Configurable> device) {
            getDevice().setAddress(device);
            getDevice().setSettings(device);
            return this;
        }

        public DriverConfigs setDeviceAddress(Class<? extends Configurable> configurable) {
            getDevice().setAddress(configurable);
            return this;
        }

        public DriverConfigs setDeviceSettings(Class<? extends Configurable> configurable) {
            getDevice().setSettings(configurable);
            return this;
        }

        public DriverConfigs setScanDeviceSettings(Class<? extends Configurable> configurable) {
            getDevice().setScanSettings(configurable);
            return this;
        }

        @Override
        public ChannelOptions.ChannelConfigs getChannel() {
            return (ChannelOptions.ChannelConfigs) super.getChannel();
        }

        public DriverConfigs setChannel(Class<? extends Configurable> channel) {
            getChannel().setAddress(channel);
            getChannel().setSettings(channel);
            return this;
        }

        public DriverConfigs setChannelAddress(Class<? extends Configurable> configurable) {
            getChannel().setAddress(configurable);
            return this;
        }

        public DriverConfigs setChannelSettings(Class<? extends Configurable> configurable) {
            getChannel().setSettings(configurable);
            return this;
        }

        public DriverConfigs setScanChannelSettings(Class<? extends Configurable> configurable) {
            getChannel().setScanSettings(configurable);
            return this;
        }

    }

}
