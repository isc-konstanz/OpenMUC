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
package org.openmuc.framework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.config.options.OptionInfo;
import org.openmuc.framework.config.options.OptionSyntax;
import org.openmuc.framework.config.options.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DriverInfo {
    private final static Logger logger = LoggerFactory.getLogger(DriverInfo.class);

    public final static String VIRTUAL = "virtual";

    private String id;
    private String name = null;
    private String description = null;
    private OptionInfo deviceAddress = null;
    private OptionInfo deviceSettings = null;
    private OptionInfo deviceScanSettings = null;
    private OptionInfo channelAddress = null;
    private OptionInfo channelSettings = null;
    private OptionInfo channelScanSettings = null;
    
    /**
     * Constructor to set driver info
     * 
     * @param is
     *            resource stream, containing all driver info as XML nodes
     */
    public DriverInfo(InputStream is) {
        if (is != null) {
            DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();
            docBFac.setIgnoringComments(true);
            try {
                Document doc = docBFac.newDocumentBuilder().parse(is);
                Node node = doc.getDocumentElement();
                if (!node.getNodeName().equals("configuration")) {
                    logger.warn("Root node in driver info options is not of type \"configuration\"");
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
                logger.warn("Error while reading driver info options: {}", e.getMessage());
            }
        }
        else {
            logger.warn("Driver info options resource not found");
        }
    }
    
    /**
     * Constructor to set driver info
     * 
     * @param id
     *            driver ID
     * @param description
     *            driver description
     * @param deviceAddressSyntax
     *            device address syntax
     * @param deviceSettingsSyntax
     *            device settings syntax
     * @param channelAddressSyntax
     *            channel address syntax
     * @param deviceScanSettingsSyntax
     *            device scan settings syntax
     */
    public DriverInfo(String id, String description, 
            String deviceAddressSyntax, String deviceSettingsSyntax,
            String channelAddressSyntax, String deviceScanSettingsSyntax) {
        this.id = id;
        this.description = description;
        this.deviceAddress = new OptionSyntax(deviceAddressSyntax);
        this.deviceSettings = new OptionSyntax(deviceSettingsSyntax);
        this.deviceScanSettings = new OptionSyntax(deviceScanSettingsSyntax);
        this.channelAddress = new OptionSyntax(channelAddressSyntax);
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

    public String getDescription() {
        return description;
    }

    public Preferences parseDeviceAddress(String addressStr) throws ArgumentSyntaxException {
        return deviceAddress.parse(addressStr);
    }

    public Preferences parseDeviceSettings(String settingsStr) throws ArgumentSyntaxException {
        return deviceSettings.parse(settingsStr);
    }

    public Preferences parseDeviceScanSettings(String scanSettingsStr) throws ArgumentSyntaxException {
        return deviceScanSettings.parse(scanSettingsStr);
    }

    public Preferences parseChannelAddress(String addressStr) throws ArgumentSyntaxException {
        return channelAddress.parse(addressStr);
    }

    public Preferences parseChannelSettings(String settingsStr) throws ArgumentSyntaxException {
        return channelSettings.parse(settingsStr);
    }

    public Preferences parseChannelScanSettings(String scanSettingsStr) throws ArgumentSyntaxException {
        return channelScanSettings.parse(scanSettingsStr);
    }

    public OptionInfo getDriverConfig() throws ParseException, IOException {
        return readConfigs("driver");
    }

    public OptionInfo getDeviceAddress() {
        return deviceAddress;
    }

    public OptionInfo getDeviceSettings() {
        return deviceSettings;
    }

    public OptionInfo getDeviceScanSettings() {
        return deviceScanSettings;
    }

    public OptionInfo getDeviceConfig() throws ParseException, IOException {
        return readConfigs("device");
    }

    public OptionInfo getChannelAddress() {
        return channelAddress;
    }

    public OptionInfo getChannelSettings() {
        return channelSettings;
    }

    public OptionInfo getChannelScanSettings() {
        return channelScanSettings;
    }

    public OptionInfo getChannelConfig() throws ParseException, IOException {
        return readConfigs("channel");
    }

    private OptionCollection readConfigs(String type) throws ParseException, IOException {

        //Get file from resources folder
        InputStream is = DriverInfo.class.getResourceAsStream("options/"+type+".xml");
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
        return OptionCollection.getFromDomNode(node);
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
                this.description = DriverInfo.trimTextFromDomNode(childNode);
            }
            else if (childNodeName.equals("deviceAddress")) {
                this.deviceAddress = OptionCollection.getFromDomNode(childNode, optionsById);
            }
            else if (childNodeName.equals("deviceSettings")) {
                this.deviceSettings = OptionCollection.getFromDomNode(childNode, optionsById);
            }
            else if (childNodeName.equals("deviceScanSettings")) {
                this.deviceScanSettings = OptionCollection.getFromDomNode(childNode, optionsById);
            }
            else if (childNodeName.equals("channelAddress")) {
                this.channelAddress = OptionCollection.getFromDomNode(childNode, optionsById);
            }
            else if (childNodeName.equals("channelSettings")) {
                this.channelSettings = OptionCollection.getFromDomNode(childNode, optionsById);
            }
            else if (childNodeName.equals("channelScanSettings")) {
                this.channelScanSettings = OptionCollection.getFromDomNode(childNode, optionsById);
            }
            else {
                throw new ParseException("Unknown tag found:" + childNodeName);
            }
        }
    }
    
    public static String trimTextFromDomNode(Node node) {
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

    public static DriverInfo getVirtualDriverInfo() {
    	return new DriverInfo(DriverInfo.class.getResourceAsStream("virtual.xml"));
    }

}
