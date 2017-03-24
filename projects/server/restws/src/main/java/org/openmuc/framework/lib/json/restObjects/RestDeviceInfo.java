/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.lib.json.restObjects;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.openmuc.framework.config.DriverInfo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class RestDeviceInfo {
	
	private static final Charset CHARSET = Charset.forName("UTF-8");

	String description = null;
	LinkedList<RestParameterInfo> address = null;
	RestParameterSyntax addressSyntax = null;
	LinkedList<RestParameterInfo> settings = null;
	RestParameterSyntax settingsSyntax = null;
	LinkedList<RestParameterInfo> scanSettings = null;
	RestParameterSyntax scanSettingsSyntax = null;
	LinkedList<RestParameterInfo> config = null;

	public String getDescription() {
		return description;
	}

	public LinkedList<RestParameterInfo> getAddress() {
		return address;
	}

	public RestParameterSyntax getAddressSyntax() {
		return addressSyntax;
	}

	public LinkedList<RestParameterInfo> getSettings() {
		return settings;
	}

	public RestParameterSyntax getSettingsSyntax() {
		return settingsSyntax;
	}

	public LinkedList<RestParameterInfo> getScan() {
		return scanSettings;
	}

	public RestParameterSyntax getScanSettingsSyntax() {
		return scanSettingsSyntax;
	}

	public LinkedList<RestParameterInfo> getConfig() {
		return config;
	}

	private void addDefaultFromResource(Path infoPath) throws IOException {

		if (Files.exists(infoPath)) {
			byte[] encoded = Files.readAllBytes(infoPath);
			String infoString = new String(encoded, CHARSET);
			
			if (config == null) {
				config = new LinkedList<RestParameterInfo>();
			}
			
			Gson gson = new Gson();
			JsonArray jsa = gson.fromJson(infoString.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", ""), JsonArray.class);
			for (JsonElement objectJson : jsa) {
				RestParameterInfo parameter = gson.fromJson(objectJson, RestParameterInfo.class);
				config.add(parameter);
			}
		}
	}

	private static LinkedList<RestParameterInfo> getSyntaxParameter(String name, String syntax) {

		LinkedList<RestParameterInfo> list = new LinkedList<RestParameterInfo>();

		RestParameterInfo parameter = new RestParameterInfo();
		parameter.id = name.toLowerCase();
		parameter.name = name;
		parameter.type = RestParameterType.TEXT;
		
		if (syntax != null && !syntax.toLowerCase().replace(".", "").equals("na") && !syntax.equals("?")) {
			parameter.description = "Syntax: " + syntax;
			parameter.required = true;
		}
		else {
			parameter.description = null;
			parameter.required = false;
		}
		list.add(parameter);
		
		return list;
	}

	public static RestDeviceInfo getInfoFromResource(DriverInfo info) throws IOException {

		String infoDirName = System.getProperty("org.openmuc.framework.driverinfo");
		if (infoDirName == null) {
			infoDirName = "lib/info/";
		}
		if (!infoDirName.endsWith("/")) {
			infoDirName += "/";
		}
		Path infoPath = Paths.get(infoDirName + info.getId() + "/device.json");
		
		RestDeviceInfo restInfo;
		if (!Files.exists(infoPath)) {
			
			restInfo = new RestDeviceInfo();
			restInfo.description = info.getDescription();
			restInfo.address = getSyntaxParameter("address", info.getDeviceAddressSyntax());
			restInfo.addressSyntax = new RestParameterSyntax(";");
			restInfo.settings = getSyntaxParameter("settings", info.getSettingsSyntax());
			restInfo.settingsSyntax = new RestParameterSyntax(";");
			restInfo.scanSettings = getSyntaxParameter("settings", info.getDeviceScanSettingsSyntax());
			restInfo.scanSettingsSyntax = new RestParameterSyntax(";");
		}
		else {
			byte[] encoded = Files.readAllBytes(infoPath);
			String infoString = new String(encoded, CHARSET);
			
			restInfo = new Gson().fromJson(infoString.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", ""), RestDeviceInfo.class);
			if (restInfo.address != null && restInfo.addressSyntax == null) restInfo.addressSyntax = new RestParameterSyntax(":", ",");
			if (restInfo.settings != null && restInfo.settingsSyntax == null) restInfo.settingsSyntax = new RestParameterSyntax(":", ",");
			if (restInfo.scanSettings != null && restInfo.scanSettingsSyntax == null) restInfo.scanSettingsSyntax = new RestParameterSyntax(":", ",");
		}
		
		// Get default info json from resources
		restInfo.addDefaultFromResource(Paths.get(infoDirName + "default/device.json"));
		
		return restInfo;
	}
}
