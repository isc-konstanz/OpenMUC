package org.openmuc.framework.driver;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Configurations;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.config.option.DeviceOptions;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.driver.spi.ConnectionException;

public abstract class DeviceContext implements DeviceOptions, DeviceFactory, DeviceScannerFactory, DeviceConnection.Callbacks {

    private Class<? extends DeviceConnection> deviceClass;

    private Class<? extends DeviceScanner> scannerClass;

    private class ChannelFactory extends ChannelContext {

        ChannelFactory(Class<? extends ChannelContext> context) {
            super(context);
        }

    }

    // TODO: This could be replaced with a dynamic approach, depending on device options
    ChannelContext channel;

    DriverContext context;

    DeviceContext() {
        DeviceFactory.Factory factory = getClass().getAnnotation(DeviceFactory.Factory.class);
        if (factory != null) {
            scannerClass = factory.scanner();
            deviceClass = factory.device();
        }
        this.channel = new ChannelFactory(deviceClass);
    }

    @Override
    public final ChannelContext getChannel() {
        return channel;
    }

    public final DriverContext getContext() {
        return context;
    }

    @Override
    public final Options getAddressOptions() {
        Options deviceAddress = null;
        if (deviceClass != null) {
            deviceAddress = Options.parseAddress(deviceClass);
        }
        return deviceAddress;
    }

    @Override
    public final Options getSettingsOptions() {
        Options deviceSettings = null;
        if (deviceClass != null) {
            deviceSettings = Options.parseSettings(deviceClass);
        }
        return deviceSettings;
    }

    @Override
    public final Options getScanSettingsOptions() {
        Options scanSettings = null;
        if (scannerClass != null && !scannerClass.equals(DeviceScanner.class)) {
            scanSettings = Options.parseSettings(scannerClass);
        }
        return scanSettings;
    }

    final void bindDevice(Class<? extends DeviceConnection> deviceClass) {
        this.deviceClass = deviceClass;
    }

    public DeviceConnection newDevice(String address, String settings) throws ArgumentSyntaxException, ConnectionException {
        return this.newDevice(Configurations.parseAddress(address, deviceClass),
                              Configurations.parseSettings(settings, deviceClass));
    }

    @Override
    public DeviceConnection newDevice(Address address, Settings settings) throws ArgumentSyntaxException, ConnectionException {
        return this.newDevice();
    }

    protected DeviceConnection newDevice() throws ConnectionException {
        return DriverContext.newInstance(deviceClass);
    }

    final void bindScanner(Class<? extends DeviceScanner> scannerClass) {
        this.scannerClass = scannerClass;
    }

    public DeviceScanner newScanner(String settings) throws ArgumentSyntaxException {
        return this.newScanner(Configurations.parseSettings(settings, scannerClass));
    }

    @Override
    public DeviceScanner newScanner(Settings settings) throws ArgumentSyntaxException {
        return this.newScanner();
    }

    protected DeviceScanner newScanner() {
        return DriverContext.newInstance(scannerClass);
    }

}
