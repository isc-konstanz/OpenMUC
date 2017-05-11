package org.openmuc.framework.config;

import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.config.info.OptionSelection;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.ValueType;

public abstract class ChannelInfo {

    public abstract String getAddressSyntax();

    public abstract String getScanSettingsSyntax();

    public static OptionCollection configs() {
        
        OptionCollection config = new OptionCollection();
        config.add(samplingInterval());
        config.add(samplingTimeOffset());
        config.add(samplingGroup());
        config.add(listening());
        config.add(loggingInterval());
        config.add(loggingTimeOffset());
        config.add(unit());
        config.add(valueType());
        config.add(valueLength());
        config.add(isReadable());
        config.add(isWritable());
        
        return OptionCollection.unmodifiableOptions(config);
    }

    private static Option samplingInterval() {
        
        Option samplingInterval = new Option("samplingInterval", "Sampling interval", ValueType.INTEGER);
        samplingInterval.setDescription("Time interval between two attempts to read this channel.</br>"
            + "Removing or omitting the samling interval parameter disables sampling on this channel.");
        samplingInterval.setMandatory(false);
        samplingInterval.setValueSelection(OptionSelection.timeSelection());
        
        return samplingInterval;
    }

    private static Option samplingTimeOffset() {
        
        Option samplingTimeOffset = new Option("samplingTimeOffset", "Sampling time offset", ValueType.INTEGER);
        samplingTimeOffset.setMandatory(false);
        samplingTimeOffset.setValueDefault(new IntValue(0));
        samplingTimeOffset.setValueSelection(OptionSelection.timeSelection());
        
        return samplingTimeOffset;
    }

    private static Option samplingGroup() {
        
        Option samplingGroup = new Option("samplingGroup", "Sampling group", ValueType.STRING);
        samplingGroup.setDescription("For grouping channels. All channels with the same samplingGroup and same samplingInterval are in one group.</br>"
            + "The purpose of samplingGroups is to improve the drivers performance – if possible.");
        samplingGroup.setMandatory(false);
        
        return samplingGroup;
    }

    private static Option listening() {
        
        Option listening = new Option("listening", "Listening", ValueType.BOOLEAN);
        listening.setDescription("Determines if this channel shall passively listen for incoming value changes from the driver.");
        listening.setMandatory(false);
        listening.setValueDefault(new BooleanValue(false));
        
        return listening;
    }

    private static Option loggingInterval() {
        
        Option loggingInterval = new Option("loggingInterval", "Logging interval", ValueType.INTEGER);
        loggingInterval.setDescription("Time difference until this channel is logged again.</br>"
            + "Setting the time interval to -1 or omitting the logging interval parameter disables logging on this channel.");
        loggingInterval.setMandatory(false);
        loggingInterval.setValueSelection(OptionSelection.timeSelection());
        
        return loggingInterval;
    }

    private static Option loggingTimeOffset() {
        
        Option loggingTimeOffset = new Option("loggingTimeOffset", "Logging time offset", ValueType.INTEGER);
        loggingTimeOffset.setMandatory(false);
        loggingTimeOffset.setValueDefault(new IntValue(0));
        loggingTimeOffset.setValueSelection(OptionSelection.timeSelection());
        
        return loggingTimeOffset;
    }

    private static Option unit() {
        
        Option unit = new Option("unit", "Unit", ValueType.STRING);
        unit.setDescription("Physical unit of this channel.</br>"
            + "For information only (info can be accessed by an app or driver).");
        unit.setMandatory(false);
        
        return unit;
    }

    private static Option valueType() {
        
        Option valueType = new Option("valueType", "Value type", ValueType.STRING);
        valueType.setDescription("Data type of the channel.</br>"
            + "Data loggers may use this setting. Driver implementations do NOT receive this settings.");
        valueType.setMandatory(false);
        
        OptionSelection selection = new OptionSelection(ValueType.STRING);
        selection.addString("DOUBLE", "Double");
        selection.addString("FLOAT", "Float");
        selection.addString("LONG", "Long");
        selection.addString("INTEGER", "Integer");
        selection.addString("SHORT", "Short");
        selection.addString("BYTE", "Byte");
        selection.addString("BYTE_ARRAY", "Byte array");
        selection.addString("BOOLEAN", "Boolean");
        selection.addString("STRING", "String");
        valueType.setValueSelection(selection);
        valueType.setValueDefault(new StringValue("DOUBLE"));
        
        return valueType;
    }

    private static Option valueLength() {
        
        Option valueLength = new Option("valueLength", "Value length", ValueType.INTEGER);
        valueLength.setDescription("Only used if valueType == BYTE_ARRAY or STRING.</br>"
            + "Determines the maximum length of the byte array or string.");
        valueLength.setMandatory(false);
        
        return valueLength;
    }

    private static Option isReadable() {
        
        Option isReadable = new Option("isReadable", "Readable", ValueType.BOOLEAN);
        isReadable.setDescription("For information only (info can be accessed by an app or driver).");
        isReadable.setMandatory(false);
        isReadable.setValueDefault(new BooleanValue(false));
        
        return isReadable;
    }

    private static Option isWritable() {
        
        Option isWritable = new Option("isWritable", "Writable", ValueType.BOOLEAN);
        isWritable.setDescription("For information only (info can be accessed by an app or driver).");
        isWritable.setMandatory(false);
        isWritable.setValueDefault(new BooleanValue(false));
        
        return isWritable;
    }

}
