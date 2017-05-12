package org.openmuc.framework.config.options;

import org.openmuc.framework.config.DeviceInfo;

public class DeviceSyntax extends DeviceInfo {

    private final String addressSyntax;
    private final String settingsSyntax;
    private final String scanSettingsSyntax;

    public DeviceSyntax(String addressSyntax, String settingsSyntax, String scanSettingsSyntax) {

        this.addressSyntax = addressSyntax;
        this.settingsSyntax = settingsSyntax;
        this.scanSettingsSyntax = scanSettingsSyntax;
    }

    @Override
    public String getAddressSyntax() {
        return addressSyntax;
    }

    @Override
    public String getSettingsSyntax() {
        return settingsSyntax;
    }

    @Override
    public String getScanSettingsSyntax() {
        return scanSettingsSyntax;
    }
}
