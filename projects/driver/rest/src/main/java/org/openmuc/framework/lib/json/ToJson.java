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
package org.openmuc.framework.lib.json;

import static org.openmuc.framework.lib.json.Const.VALUE_STRING;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DeviceState;
import org.openmuc.framework.lib.json.rest.objects.RestChannelConfig;
import org.openmuc.framework.lib.json.rest.objects.RestChannelMapper;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceConfig;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceMapper;
import org.openmuc.framework.lib.json.rest.objects.RestDriverConfig;
import org.openmuc.framework.lib.json.rest.objects.RestDriverMapper;
import org.openmuc.framework.lib.json.rest.objects.RestRecord;
import org.openmuc.framework.lib.json.rest.objects.RestScanProgressInfo;
import org.openmuc.framework.lib.json.rest.objects.RestUserConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ToJson {

    private final Gson gson;
    private final JsonObject jsonObject;

    public ToJson() {

        gson = new GsonBuilder().serializeSpecialFloatingPointValues()
                .registerTypeAdapter(byte[].class, new ByteArraySerializer())
                .create();
        jsonObject = new JsonObject();
    }

    public JsonObject getJsonObject() {

        return jsonObject;
    }

    public void addJsonObject(String propertyName, JsonObject jsonObject) {

        this.jsonObject.add(propertyName, jsonObject);
    }

    @Override
    public String toString() {
        return gson.toJson(jsonObject);
    }

    public void addNumber(String propertyName, Number value) {

        jsonObject.addProperty(propertyName, value);
    }

    public void addBoolean(String propertyName, boolean value) {

        jsonObject.addProperty(propertyName, value);
    }

    public void addValue(Value value, ValueType valueType) {
        if (value == null) {
            jsonObject.add(Const.VALUE_STRING, JsonNull.INSTANCE);
            return;
        }

        switch (valueType) {
        case BOOLEAN:
            jsonObject.addProperty(VALUE_STRING, value.asBoolean());
            break;
        case BYTE:
            jsonObject.addProperty(VALUE_STRING, value.asByte());
            break;
        case BYTE_ARRAY:
            jsonObject.addProperty(VALUE_STRING, gson.toJson(value.asByteArray()));
            break;
        case DOUBLE:
            jsonObject.addProperty(VALUE_STRING, value.asDouble());
            break;
        case FLOAT:
            jsonObject.addProperty(VALUE_STRING, value.asFloat());
            break;
        case INTEGER:
            jsonObject.addProperty(VALUE_STRING, value.asInt());
            break;
        case LONG:
            jsonObject.addProperty(VALUE_STRING, value.asLong());
            break;
        case SHORT:
            jsonObject.addProperty(VALUE_STRING, value.asShort());
            break;
        case STRING:
            jsonObject.addProperty(VALUE_STRING, value.asString());
            break;
        default:
            jsonObject.add(VALUE_STRING, JsonNull.INSTANCE);
            break;
        }
    }

    public void addString(String propertyName, String value) {

        jsonObject.addProperty(propertyName, value);
    }

    public void addStringList(String propertyName, List<String> stringList) {

        jsonObject.add(propertyName, gson.toJsonTree(stringList).getAsJsonArray());
    }

    public void addRecordList(List<Record> recordList, ValueType valueType) throws ClassCastException {

        JsonArray jsa = new JsonArray();
        if (recordList != null) {
            for (Record record : recordList) {
                jsa.add(getRecordAsJsonElement(record, valueType));
            }
        }
        jsonObject.add(Const.RECORDS, jsa);
    }

    public void addRecordList(List<Channel> channels) throws ClassCastException {

        JsonArray jsa = new JsonArray();
        for (Channel channel : channels) {
            JsonObject jso = new JsonObject();

            jso.addProperty(Const.ID, channel.getId());
            jso.addProperty(Const.VALUE_TYPE, channel.getValueType().toString());
            jso.add(Const.RECORD, getRecordAsJsonElement(channel.getLatestRecord(), channel.getValueType()));
            jsa.add(jso);
        }
        jsonObject.add(Const.RECORDS, jsa);
    }

    public void addRecord(Record record, ValueType valueType) throws ClassCastException {

        jsonObject.add(Const.RECORD, getRecordAsJsonElement(record, valueType));
    }

    public void addChannelIdList(List<Channel> channelList) {

        JsonArray jsa = new JsonArray();

        for (Channel channelConfig : channelList) {
            jsa.add(gson.toJsonTree(channelConfig.getId()));
        }
        jsonObject.add(Const.CHANNELS, jsa);
    }

    private JsonElement getRecordAsJsonElement(Record record, ValueType valueType) throws ClassCastException {

        return gson.toJsonTree(getRestRecord(record, valueType), RestRecord.class);
    }

    private RestRecord getRestRecord(Record rc, ValueType valueType) throws ClassCastException {

        Value value = rc.getValue();
        Flag flag = rc.getFlag();
        RestRecord rrc = new RestRecord();

        rrc.setTimestamp(rc.getTimestamp());

        try {
            flag = getFlag(value, valueType, flag);

        } catch (TypeConversionException e) {
            flag = Flag.DRIVER_ERROR_CHANNEL_VALUE_TYPE_CONVERSION_EXCEPTION;
        }
        if (flag != Flag.VALID) {
            rrc.setFlag(flag);
            rrc.setValue(null);
            return rrc;
        }
        rrc.setFlag(flag);
        
        if (value == null) {
            rrc.setValue(null);
            return rrc;
        }
        switch (valueType) {
        case FLOAT:
            rrc.setValue(value.asFloat());
            break;
        case DOUBLE:
            rrc.setValue(value.asDouble());
            break;
        case SHORT:
            rrc.setValue(value.asShort());
            break;
        case INTEGER:
            rrc.setValue(value.asInt());
            break;
        case LONG:
            rrc.setValue(value.asLong());
            break;
        case BYTE:
            rrc.setValue(value.asByte());
            break;
        case BOOLEAN:
            rrc.setValue(value.asBoolean());
            break;
        case BYTE_ARRAY:
            rrc.setValue(value.asByteArray());
            break;
        case STRING:
            rrc.setValue(value.asString());
            break;
        default:
            rrc.setValue(null);
            break;
        }
        return rrc;
    }

    private Flag getFlag(Value value, ValueType valueType, Flag flag) {
        if (value == null) {
            return flag;
        }

        switch (valueType) {
        case DOUBLE:
            if (Double.isInfinite(value.asDouble())) {
                return Flag.VALUE_IS_INFINITY;
            }
            else if (Double.isNaN(value.asDouble())) {
                return Flag.VALUE_IS_NAN;
            }
            break;
        case FLOAT:
            if (Float.isInfinite(value.asFloat())) {
                return Flag.VALUE_IS_INFINITY;
            }
            else if (Float.isNaN(value.asFloat())) {
                return Flag.VALUE_IS_NAN;
            }
            break;
        default:
            // is not a floating point number
            return flag;
        }
        return flag;
    }

    public void addChannelState(ChannelState channelState) {
        
        jsonObject.addProperty(Const.STATE, channelState.name());
    }

    public void addChannelStateList(List<Channel> channelList) {

        JsonArray jsa = new JsonArray();
        for (Channel channel : channelList) {
            JsonObject jso = new JsonObject();
            jso.addProperty(Const.ID, channel.getId());
            jso.addProperty(Const.STATE, channel.getChannelState().name());
            jsa.add(jso);
        }
        jsonObject.add(Const.STATES, jsa);
    }

    public void addChannelConfigList(List<ChannelConfig> configList) {

        JsonArray jsa = new JsonArray();
        for (ChannelConfig channelConfig : configList) {
            RestChannelConfig restConfig = RestChannelMapper.getRestChannelConfig(channelConfig);
            jsa.add(gson.toJsonTree(restConfig, RestChannelConfig.class).getAsJsonObject());
        }
        jsonObject.add(Const.CONFIGS, jsa);
    }

    public void addChannelConfig(ChannelConfig config) {

        RestChannelConfig restConfig = RestChannelMapper.getRestChannelConfig(config);
        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restConfig, RestChannelConfig.class).getAsJsonObject());
    }

    public void addChannelScanInfoList(List<ChannelScanInfo> channelScanInfoList) {

        JsonArray jsa = new JsonArray();
        for (ChannelScanInfo channelScanInfo : channelScanInfoList) {
            JsonObject jso = new JsonObject();
            jso.addProperty(Const.ADDRESS, channelScanInfo.getChannelAddress());
            jso.addProperty(Const.SETTINGS, channelScanInfo.getChannelSettings());
            jso.addProperty(Const.VALUE_TYPE, channelScanInfo.getValueType().name());
            jso.addProperty(Const.VALUE_TYPE_LENGTH, channelScanInfo.getValueTypeLength());
            jso.addProperty(Const.DESCRIPTION, channelScanInfo.getDescription());
            jso.addProperty(Const.METADATA, channelScanInfo.getMetaData());
            jso.addProperty(Const.UNIT, channelScanInfo.getUnit());
            jsa.add(jso);
        }
        jsonObject.add(Const.CHANNELS, jsa);
    }

    public void addDeviceConfigList(List<DeviceConfig> configList) {

        JsonArray jsa = new JsonArray();

        for (DeviceConfig deviceConfig : configList) {
            RestDeviceConfig restConfig = RestDeviceMapper.getRestDeviceConfig(deviceConfig);
            jsa.add(gson.toJsonTree(restConfig, RestDeviceConfig.class).getAsJsonObject());
        }
        jsonObject.add(Const.CONFIGS, jsa);
    }

    public void addDeviceConfig(DeviceConfig config) {

        RestDeviceConfig restConfig = RestDeviceMapper.getRestDeviceConfig(config);
        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restConfig, RestDeviceConfig.class).getAsJsonObject());
    }

    public void addDeviceStateList(Map<String, DeviceState> deviceStates) {

        JsonArray jsa = new JsonArray();
        
        for (Map.Entry<String, DeviceState> deviceState : deviceStates.entrySet()) {
            JsonObject jso = new JsonObject();
            jso.addProperty(Const.ID, deviceState.getKey());
            jso.addProperty(Const.STATE, deviceState.getValue().name());
            jsa.add(jso);
        }
        jsonObject.add(Const.STATES, jsa);
    }

    public void addDeviceState(DeviceState deviceState) {

        jsonObject.addProperty(Const.STATE, deviceState.name());
    }

    public void addDeviceScanProgressInfo(RestScanProgressInfo restScanProgressInfo) {

        jsonObject.add(Const.SCAN_PROGRESS_INFO, gson.toJsonTree(restScanProgressInfo));
    }

    public void addDeviceScanInfoList(List<DeviceScanInfo> deviceScanInfoList) {

        JsonArray jsa = new JsonArray();
        for (DeviceScanInfo deviceScanInfo : deviceScanInfoList) {
            JsonObject jso = new JsonObject();
            jso.addProperty(Const.ID, deviceScanInfo.getId());
            jso.addProperty(Const.ADDRESS, deviceScanInfo.getAddress());
            jso.addProperty(Const.SETTINGS, deviceScanInfo.getSettings());
            jso.addProperty(Const.DESCRIPTION, deviceScanInfo.getDescription());
            jsa.add(jso);
        }
        jsonObject.add(Const.DEVICES, jsa);
    }

    public void addDriverConfigList(List<DriverConfig> configList) {

        JsonArray jsa = new JsonArray();

        for (DriverConfig driverConfig : configList) {
            RestDriverConfig restConfig = RestDriverMapper.getRestDriverConfig(driverConfig);
            jsa.add(gson.toJsonTree(restConfig, RestDriverConfig.class).getAsJsonObject());
        }
        jsonObject.add(Const.CONFIGS, jsa);
    }

    public void addDriverConfig(DriverConfig config) {

        RestDriverConfig restConfig = RestDriverMapper.getRestDriverConfig(config);
        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restConfig, RestDriverConfig.class).getAsJsonObject());
    }

    public void addUserConfig(RestUserConfig restUserConfig) {

        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restUserConfig, RestUserConfig.class).getAsJsonObject());
    }

    public static JsonObject getDriverConfigAsJsonObject(DriverConfig config) {
        RestDriverConfig restConfig = RestDriverMapper.getRestDriverConfig(config);
        return new Gson().toJsonTree(restConfig, RestDriverConfig.class).getAsJsonObject();
    }

    public static JsonObject getDeviceConfigAsJsonObject(DeviceConfig config) {
        RestDeviceConfig restConfig = RestDeviceMapper.getRestDeviceConfig(config);
        return new Gson().toJsonTree(restConfig, RestDeviceConfig.class).getAsJsonObject();
    }

    public static JsonObject getChannelConfigAsJsonObject(ChannelConfig config) {
        RestChannelConfig restConfig = RestChannelMapper.getRestChannelConfig(config);
        return new Gson().toJsonTree(restConfig, RestChannelConfig.class).getAsJsonObject();
    }

    private class ByteArraySerializer implements JsonSerializer<byte[]> {
        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            for (byte element : src) {
                arr.add(element & 0xff);
            }
            return arr;
        }

    }

}
