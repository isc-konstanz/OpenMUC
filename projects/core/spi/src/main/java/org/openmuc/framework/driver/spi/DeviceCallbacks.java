package org.openmuc.framework.driver.spi;

public interface DeviceCallbacks {

	public void onConnected(DeviceConnection<?> configs);

	public void onDisconnected(DeviceConnection<?> configs);

}
