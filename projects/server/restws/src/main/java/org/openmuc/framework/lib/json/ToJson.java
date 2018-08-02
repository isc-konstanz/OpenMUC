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
package org.openmuc.framework.lib.json;

import static org.openmuc.framework.lib.json.Const.VALUE_STRING;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.ParseException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.ChannelState;
import org.openmuc.framework.dataaccess.DeviceState;
import org.openmuc.framework.lib.json.rest.objects.RestChannelConfig;
import org.openmuc.framework.lib.json.rest.objects.RestChannelConfigMapper;
import org.openmuc.framework.lib.json.rest.objects.RestChannelDetail;
import org.openmuc.framework.lib.json.rest.objects.RestChannelInfo;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceConfig;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceConfigMapper;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceDetail;
import org.openmuc.framework.lib.json.rest.objects.RestDeviceInfo;
import org.openmuc.framework.lib.json.rest.objects.RestDriverConfig;
import org.openmuc.framework.lib.json.rest.objects.RestDriverConfigMapper;
import org.openmuc.framework.lib.json.rest.objects.RestDriverDetail;
import org.openmuc.framework.lib.json.rest.objects.RestDriverInfo;
import org.openmuc.framework.lib.json.rest.objects.RestDriverSyntax;
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

    public void addRecord(Record record, ValueType valueType) throws ClassCastException {

        jsonObject.add(Const.RECORD, getRecordAsJsonElement(record, valueType));
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

    public void addChannelRecordList(List<Channel> channels) throws ClassCastException {

        JsonArray jsa = new JsonArray();

        for (Channel channel : channels) {
            jsa.add(channelRecordToJson(channel));
        }
        jsonObject.add(Const.RECORDS, jsa);
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

    public void addDeviceState(DeviceState deviceState) {

        jsonObject.addProperty(Const.STATE, deviceState.name());
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

    public void addString(String propertyName, String value) {

        jsonObject.addProperty(propertyName, value);
    }

    public void addStringList(String propertyName, List<String> stringList) {

        jsonObject.add(propertyName, gson.toJsonTree(stringList).getAsJsonArray());
    }

    public void addDriverList(List<DriverConfig> driverConfigList) {

        JsonArray jsa = new JsonArray();

        for (DriverConfig driverConfig : driverConfigList) {
            jsa.add(gson.toJsonTree(driverConfig.getId()));
        }
        jsonObject.add(Const.DRIVERS, jsa);
    }

    public void addChannelList(List<Channel> channelList) {

        JsonArray jsa = new JsonArray();

        for (Channel channelConfig : channelList) {
            jsa.add(gson.toJsonTree(channelConfig.getId()));
        }
        jsonObject.add(Const.CHANNELS, jsa);
    }

    public void addDriverDescriptionList(List<DriverInfo> infoList) {

        JsonArray jsa = new JsonArray();

        for (DriverInfo driverInfo : infoList) {
            RestDriverInfo restDriverDesc = RestDriverInfo.getRestDriverDescription(driverInfo);
            jsa.add(gson.toJsonTree(restDriverDesc, RestDriverInfo.class).getAsJsonObject());
        }
        jsonObject.add(Const.DRIVERS, jsa);
    }

    public void addDriverSyntax(DriverInfo driverInfo) {

        RestDriverSyntax restDriverSyntax = RestDriverSyntax.setDriverSyntax(driverInfo);
        jsonObject.add(Const.INFOS, gson.toJsonTree(restDriverSyntax, RestDriverSyntax.class).getAsJsonObject());
    }

    public void addDriverInfoFull(DriverInfo driverInfo) throws ParseException, IOException {

        RestDriverInfo restDriverInfo = RestDriverInfo.getRestDriverInfoFull(driverInfo);
        jsonObject.add(Const.INFOS, gson.toJsonTree(restDriverInfo, RestDriverInfo.class).getAsJsonObject());
    }

    public void addDriverInfo(DriverInfo driverInfo) throws ParseException, IOException {

        RestDriverInfo restDriverInfo = RestDriverInfo.getRestDriverInfo(driverInfo);
        jsonObject.add(Const.INFOS, gson.toJsonTree(restDriverInfo, RestDriverInfo.class).getAsJsonObject());
    }

    public void addDeviceInfo(DriverInfo driverInfo) throws ParseException, IOException {

        RestDeviceInfo restDeviceInfo = RestDeviceInfo.getRestDeviceInfo(driverInfo);
        restDeviceInfo.setDescription(driverInfo.getDescription());
        
        jsonObject.add(Const.INFOS, gson.toJsonTree(restDeviceInfo, RestDeviceInfo.class).getAsJsonObject());
    }

    public void addChannelInfo(DriverInfo driverInfo) throws ParseException, IOException {

        RestChannelInfo restChannelInfo = RestChannelInfo.getRestChannelInfo(driverInfo);
        restChannelInfo.setDescription(driverInfo.getDescription());
        
        jsonObject.add(Const.INFOS, gson.toJsonTree(restChannelInfo, RestChannelInfo.class).getAsJsonObject());
    }

    public void addDriverConfig(DriverConfig config) {

        RestDriverConfig restConfig = RestDriverConfigMapper.getRestDriverConfig(config);
        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restConfig, RestDriverConfig.class).getAsJsonObject());
    }

    public void addDriverConfigList(List<DriverConfig> configList) {

        JsonArray jsa = new JsonArray();

        for (DriverConfig driverConfig : configList) {
            RestDriverConfig restConfig = RestDriverConfigMapper.getRestDriverConfig(driverConfig);
            jsa.add(gson.toJsonTree(restConfig, RestDriverConfig.class).getAsJsonObject());
        }
        jsonObject.add(Const.CONFIGS, jsa);
    }

    public void addDriverDetail(DriverInfo info, DriverConfig config) {

        RestDriverDetail restDetail = RestDriverDetail.getRestDriverDetail(info, config);
        jsonObject.add(Const.DETAILS, gson.toJsonTree(restDetail, RestDriverDetail.class).getAsJsonObject());
    }

    public void addDriverDetailList(List<RestDriverDetail> detailList) {

        JsonArray jsa = new JsonArray();
        
        for (RestDriverDetail detail : detailList) {
            jsa.add(gson.toJsonTree(detail, RestDriverDetail.class).getAsJsonObject());
        }
        jsonObject.add(Const.DETAILS, jsa);
    }

    public void addDeviceConfig(DeviceConfig config) {

        RestDeviceConfig restConfig = RestDeviceConfigMapper.getRestDeviceConfig(config);
        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restConfig, RestDeviceConfig.class).getAsJsonObject());
    }

    public void addDeviceConfigList(List<DeviceConfig> configList) {

        JsonArray jsa = new JsonArray();

        for (DeviceConfig deviceConfig : configList) {
            RestDeviceConfig restConfig = RestDeviceConfigMapper.getRestDeviceConfig(deviceConfig);
            jsa.add(gson.toJsonTree(restConfig, RestDeviceConfig.class).getAsJsonObject());
        }
        jsonObject.add(Const.CONFIGS, jsa);
    }

    public void addDeviceDetail(DeviceState state, DeviceConfig config, DriverInfo info) {

        RestDeviceDetail restDetail = RestDeviceDetail.getRestDeviceDetail(state, config, info);
        jsonObject.add(Const.DETAILS, gson.toJsonTree(restDetail, RestDeviceDetail.class).getAsJsonObject());
    }

    public void addDeviceDetailList(List<RestDeviceDetail> detailList) {

        JsonArray jsa = new JsonArray();
        
        for (RestDeviceDetail detail : detailList) {
            jsa.add(gson.toJsonTree(detail, RestDeviceDetail.class).getAsJsonObject());
        }
        jsonObject.add(Const.DETAILS, jsa);
    }

    public void addChannelConfig(ChannelConfig config) {

        RestChannelConfig restConfig = RestChannelConfigMapper.getRestChannelConfig(config);
        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restConfig, RestChannelConfig.class).getAsJsonObject());
    }

    public void addChannelConfigList(List<ChannelConfig> configList) {

        JsonArray jsa = new JsonArray();
        
        for (ChannelConfig channelConfig : configList) {
            RestChannelConfig restConfig = RestChannelConfigMapper.getRestChannelConfig(channelConfig);
            jsa.add(gson.toJsonTree(restConfig, RestChannelConfig.class).getAsJsonObject());
        }
        jsonObject.add(Const.CONFIGS, jsa);
    }

    public void addChannelDetail(Channel channel, ChannelConfig config) {

        RestChannelDetail restDetail = RestChannelDetail.getRestChannelDetail(channel, config);
        jsonObject.add(Const.DETAILS, gson.toJsonTree(restDetail, RestChannelDetail.class).getAsJsonObject());
    }

    public void addChannelDetailList(List<RestChannelDetail> detailList) {

        JsonArray jsa = new JsonArray();
        
        for (RestChannelDetail detail : detailList) {
            jsa.add(gson.toJsonTree(detail, RestChannelDetail.class).getAsJsonObject());
        }
        jsonObject.add(Const.DETAILS, jsa);
    }

    public void addDeviceScanProgressInfo(RestScanProgressInfo restScanProgressInfo) {

        jsonObject.add(Const.SCAN_PROGRESS_INFO, gson.toJsonTree(restScanProgressInfo));
    }

    public void addDeviceScanInfoList(List<DeviceScanInfo> deviceScanInfoList) {

        JsonArray jsa = new JsonArray();
        for (DeviceScanInfo deviceScanInfo : deviceScanInfoList) {
            JsonObject jso = new JsonObject();
            jso.addProperty(Const.ID, deviceScanInfo.getId());
            jso.addProperty(Const.DEVICEADDRESS, deviceScanInfo.getDeviceAddress());
            jso.addProperty(Const.SETTINGS, deviceScanInfo.getSettings());
            jso.addProperty(Const.DESCRIPTION, deviceScanInfo.getDescription());
            jsa.add(jso);
        }
        jsonObject.add(Const.DEVICES, jsa);
    }

    public void addChannelScanInfoList(List<ChannelScanInfo> channelScanInfoList) {

        JsonArray jsa = new JsonArray();
        for (ChannelScanInfo channelScanInfo : channelScanInfoList) {
            JsonObject jso = new JsonObject();
            jso.addProperty(Const.CHANNELADDRESS, channelScanInfo.getChannelAddress());
            jso.addProperty(Const.CHANNELSETTINGS, channelScanInfo.getChannelSettings());
            jso.addProperty(Const.VALUETYPE, channelScanInfo.getValueType().name());
            jso.addProperty(Const.VALUETYPELENGTH, channelScanInfo.getValueTypeLength());
            jso.addProperty(Const.DESCRIPTION, channelScanInfo.getDescription());
            jso.addProperty(Const.METADATA, channelScanInfo.getMetaData());
            jso.addProperty(Const.UNIT, channelScanInfo.getUnit());
            jsa.add(jso);
        }
        jsonObject.add(Const.CHANNELS, jsa);
    }

    public void addRestUserConfig(RestUserConfig restUserConfig) {

        jsonObject.add(Const.CONFIGS, gson.toJsonTree(restUserConfig, RestUserConfig.class).getAsJsonObject());
    }

    public static JsonObject getDriverConfigAsJsonObject(DriverConfig config) {

        RestDriverConfig restConfig = RestDriverConfigMapper.getRestDriverConfig(config);
        return new Gson().toJsonTree(restConfig, RestDriverConfig.class).getAsJsonObject();
    }

    public static JsonObject getDeviceConfigAsJsonObject(DeviceConfig config) {
        RestDeviceConfig restConfig = RestDeviceConfigMapper.getRestDeviceConfig(config);
        return new Gson().toJsonTree(restConfig, RestDeviceConfig.class).getAsJsonObject();
    }

    public static JsonObject getChannelConfigAsJsonObject(ChannelConfig config) {
        RestChannelConfig restConfig = RestChannelConfigMapper.getRestChannelConfig(config);
        return new Gson().toJsonTree(restConfig, RestChannelConfig.class).getAsJsonObject();
    }

    private JsonObject channelRecordToJson(Channel channel) throws ClassCastException {

        JsonObject jso = new JsonObject();

        jso.addProperty(Const.ID, channel.getId());
        jso.addProperty(Const.VALUETYPE, channel.getValueType().toString());
        jso.add(Const.RECORD, getRecordAsJsonElement(channel.getLatestRecord(), channel.getValueType()));
        return jso;
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
            flag = handleInfinityAndNaNValue(value, valueType, flag);
        } catch (TypeConversionException e) {
            flag = Flag.DRIVER_ERROR_CHANNEL_VALUE_TYPE_CONVERSION_EXCEPTION;
        }
        if (flag != Flag.VALID) {
            rrc.setFlag(flag);
            rrc.setValue(null);
            return rrc;
        }

        rrc.setFlag(flag);
        setRestRecordValue(valueType, value, rrc);

        return rrc;
    }

    private void setRestRecordValue(ValueType valueType, Value value, RestRecord rrc) throws ClassCastException {

        if (value == null) {
            rrc.setValue(null);
            return;
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
    }

    private Flag handleInfinityAndNaNValue(Value value, ValueType valueType, Flag flag) {

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

}
