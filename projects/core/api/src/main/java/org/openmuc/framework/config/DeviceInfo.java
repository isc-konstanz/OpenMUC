package org.openmuc.framework.config;

import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.config.info.OptionSelection;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.ValueType;

public abstract class DeviceInfo {

    public abstract String getAddressSyntax();

    public abstract String getSettingsSyntax();

    public abstract String getScanSettingsSyntax();

    public static OptionCollection configOptions() {
        
        OptionCollection config = new OptionCollection();
        config.add(samplingTimeout());
        config.add(connectRetryInterval());
        
        return OptionCollection.unmodifiableOptions(config);
    }

    private static Option samplingTimeout() {
        
        Option samplingTimeout = new Option("samplingTimeout", "Sampling timeout", ValueType.INTEGER);
        samplingTimeout.setDescription("Time waited for a read operation to complete. Overwrites the sampling timeout of its Driver.");
        samplingTimeout.setMandatory(false);
        samplingTimeout.setDefaultValue(new IntValue(0));
        samplingTimeout.setValueSelection(OptionSelection.timeSelection());
        
        return samplingTimeout;
    }

    private static Option connectRetryInterval() {
        
        Option connectRetryInterval = new Option("connectRetryInterval", "Connect retry interval", ValueType.INTEGER);
        connectRetryInterval.setDescription("Time waited until a failed connection attempt is repeated. Overwrites the connect retry interval of its Driver.");
        connectRetryInterval.setMandatory(false);
        connectRetryInterval.setDefaultValue(new IntValue(60000));
        connectRetryInterval.setValueSelection(OptionSelection.timeSelection());
        
        return connectRetryInterval;
    }

}
