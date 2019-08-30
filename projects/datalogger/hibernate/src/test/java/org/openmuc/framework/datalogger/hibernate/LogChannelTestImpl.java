package org.openmuc.framework.datalogger.hibernate;

import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.spi.LogChannel;

public class LogChannelTestImpl implements LogChannel {

    private final String id;
    private final ValueType valueType;
    private final Integer loggingInterval = 60000; // ms;
 
    public LogChannelTestImpl(String id, ValueType valueType) {
		this.id = id;
		this.valueType = valueType;
    }

    @Override
	public String getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUnit() {
		return null;
	}

	@Override
	public ValueType getValueType() {
		return valueType;
	}

	@Override
	public Integer getValueTypeLength() {
		return null;
	}

	@Override
	public Integer getLoggingInterval() {
		return loggingInterval;
	}

	@Override
	public Integer getLoggingTimeOffset() {
		return null;
	}

	@Override
	public String getLoggingSettings() {
		return null;
	}

}
