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
package org.openmuc.framework.options;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

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

    protected Options deviceAddress;
    protected Options deviceSettings;
    protected Options deviceScanSettings;
    protected Options channelAddress;
    protected Options channelSettings;
    protected Options channelScanSettings;

    protected DriverOptions(String id) {
        this.id = id;
    }

    /**
     * Constructor to set driver info
     * 
     * @param is
     *            resource stream, containing all option info as XML nodes
     */
    protected DriverOptions(InputStream is) {
        if (is != null) {
            try {
                DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();
                docBFac.setIgnoringComments(true);
                
                Document doc = docBFac.newDocumentBuilder().parse(is);
                Node node = doc.getDocumentElement();
                if (!node.getNodeName().equals("configuration")) {
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

    /**
     * Returns the ID of the driver. The ID may only contain ASCII letters, digits, hyphens and underscores. By
     * convention the ID should be meaningful and all lower case letters (e.g. "mbus", "modbus").
     * 
     * @return the unique ID of the driver.
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DriverOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DriverOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    public Options getDriverConfigs() throws ParseException, IOException {
        return readDriverConfigs();
    }

    public Options getDeviceAddress() {
    	return deviceAddress;
    }

    public DriverOptions setDeviceAddress(Class<? extends Configurable> deviceAddress) {
    	setDeviceAddress(Options.parseAddress(deviceAddress));
        return this;
    }

    public DriverOptions setDeviceAddress(Options deviceAddress) {
    	this.deviceAddress = deviceAddress;
    	if (deviceAddress != null) {
        	deviceAddressSyntax = deviceAddress.getSyntax();
    	}
    	else {
    		deviceAddressSyntax = null;
    	}
    	return this;
    }

    public Options getDeviceSettings() {
    	return deviceSettings;
    }

    public DriverOptions setDeviceSettings(Class<? extends Configurable> deviceSettings) {
    	setDeviceSettings(Options.parseSettings(deviceSettings));
        return this;
    }

    public DriverOptions setDeviceSettings(Options deviceSettings) {
    	this.deviceSettings = deviceSettings;
    	if (deviceSettings != null) {
    		deviceSettingsSyntax = deviceSettings.getSyntax();
    	}
    	else {
    		deviceSettingsSyntax = null;
    	}
        return this;
    }

    public Options getDeviceScanSettings() {
    	return deviceScanSettings;
    }

    public DriverOptions setDeviceScanSettings(Class<? extends Configurable> deviceScanSettings) {
    	setDeviceScanSettings(Options.parseSettings(deviceScanSettings));
        return this;
    }

    public DriverOptions setDeviceScanSettings(Options deviceScanSettings) {
    	this.deviceScanSettings = deviceScanSettings;
    	if (deviceScanSettings != null) {
    		deviceScanSettingsSyntax = deviceScanSettings.getSyntax();
    	}
    	else {
    		deviceScanSettingsSyntax = null;
    	}
        return this;
    }

    public Options getDeviceConfigs() throws ParseException, IOException {
        return readDeviceConfigs();
    }

    public Options getChannelAddress() {
    	return channelAddress;
    }

    public DriverOptions setChannelAddress(Class<? extends Configurable> channelAddress) {
    	setChannelAddress(Options.parseAddress(channelAddress));
        return this;
    }

    public DriverOptions setChannelAddress(Options channelAddress) {
    	this.channelAddress = channelAddress;
    	if (channelAddress != null) {
        	channelAddressSyntax = channelAddress.getSyntax();
    	}
    	else {
    		channelAddressSyntax = null;
    	}
        return this;
    }

    public Options getChannelSettings() {
    	return channelSettings;
    }

    public DriverOptions setChannelSettings(Class<? extends Configurable> channelSettings) {
    	setChannelSettings(Options.parseSettings(channelSettings));
        return this;
    }

    public DriverOptions setChannelSettings(Options channelSettings) {
    	this.channelSettings = channelSettings;
    	if (channelSettings != null) {
    		channelSettingsSyntax = channelSettings.getSyntax();
    	}
    	else {
    		channelSettingsSyntax = null;
    	}
        return this;
    }

    public Options getChannelScanSettings() {
    	return channelScanSettings;
    }

    public DriverOptions setChannelScanSettings(Class<? extends Configurable> channelScanSettings) {
    	setChannelScanSettings(Options.parseSettings(channelScanSettings));
        return this;
    }

    public DriverOptions setChannelScanSettings(Options channelScanSettings) {
    	this.channelScanSettings = channelScanSettings;
    	if (channelScanSettings != null) {
    		channelScanSettingsSyntax = channelScanSettings.getSyntax();
    	}
    	else {
    		channelScanSettingsSyntax = null;
    	}
        return this;
    }

    public Options getChannelConfigs() throws ParseException, IOException {
        return readChannelConfigs();
    }

    public static Options readDriverConfigs() throws ParseException, IOException {
        return readConfigs("driver");
    }

    public static Options readDeviceConfigs() throws ParseException, IOException {
        return readConfigs("device");
    }

    public static Options readChannelConfigs() throws ParseException, IOException {
        return readConfigs("channel");
    }

    private static Options readConfigs(String type) throws ParseException, IOException {

        //Get file from resources folder
        InputStream is = DriverOptions.class.getResourceAsStream("options/"+type+".xml");
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
        if (!node.getNodeName().equals("configuration")) {
            throw new ParseException("Root node in default options is not of type \"configuration\"");
        }
        return Options.getFromDomNode(node);
    }

    private void readFromDomNode(Node node) throws ParseException {
        NamedNodeMap attributes = node.getAttributes();
        Node nameAttribute = attributes.getNamedItem("id");
        if (nameAttribute == null) {
            throw new ParseException("Driver info has no id attribute");
        }
        this.id = nameAttribute.getTextContent();
        
        Map<String, Option> optionsById = new HashMap<String, Option>();
        
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
                this.description = DriverOptions.trimTextFromDomNode(childNode);
            }
            else if (childNodeName.equals("deviceAddress")) {
                setDeviceAddress(Options.getFromDomNode(childNode, optionsById));
            }
            else if (childNodeName.equals("deviceSettings")) {
                setDeviceSettings(Options.getFromDomNode(childNode, optionsById));
            }
            else if (childNodeName.equals("deviceScanSettings")) {
                setDeviceScanSettings(Options.getFromDomNode(childNode, optionsById));
            }
            else if (childNodeName.equals("channelAddress")) {
                setChannelAddress(Options.getFromDomNode(childNode, optionsById));
            }
            else if (childNodeName.equals("channelSettings")) {
                setChannelSettings(Options.getFromDomNode(childNode, optionsById));
            }
            else if (childNodeName.equals("channelScanSettings")) {
                setChannelScanSettings(Options.getFromDomNode(childNode, optionsById));
            }
            else {
                throw new ParseException("Unknown tag found:" + childNodeName);
            }
        }
    }

    static String trimTextFromDomNode(Node node) {
        BufferedReader reader = new BufferedReader(new StringReader(node.getTextContent()));
        StringBuffer result = new StringBuffer();
        try {
            String line;
            while ( (line = reader.readLine() ) != null)
                result.append(line.replaceAll("^\\s+", "").replace("\n", "").replace("\r", ""));
            
            return result.toString();
        } catch (IOException e) {
            logger.info("Error while trimming text: {}", e.getMessage());
        }
        return null;
    }

}
