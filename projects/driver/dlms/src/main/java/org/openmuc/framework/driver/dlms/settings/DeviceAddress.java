package org.openmuc.framework.driver.dlms.settings;

import java.net.InetAddress;

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class DeviceAddress extends Preferences {

	public static final PreferenceType TYPE = PreferenceType.ADDRESS_DEVICE;

    @Option("t")
    private String connectionType = null;

    @Option("h")
    private InetAddress hostAddress = null;

    @Option("p")
    private int port = 4059;

    @Option("hdlc")
    private boolean useHdlc = false;

    @Option("sp")
    private String serialPort = "";

    @Option("bd")
    private int baudrate = 9600;

    @Option("d")
    private long baudRateChangeDelay = 0;

    @Option("eh")
    private boolean enableBaudRateHandshake = false;

    @Option("iec")
    private String iec21Address = "";

    @Option("pd")
    private int physicalDeviceAddress = 0;

	@Override
	public PreferenceType getPreferenceType() {
		return TYPE;
	}

    public String getConnectionType() {
        return connectionType;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean useHdlc() {
        return useHdlc;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public long getBaudRateChangeDelay() {
        return baudRateChangeDelay;
    }

    public boolean enableBaudRateHandshake() {
        return enableBaudRateHandshake;
    }

    public String getIec21Address() {
        return iec21Address;
    }

    public int getPhysicalDeviceAddress() {
        return physicalDeviceAddress;
    }

}
