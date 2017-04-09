package org.openmuc.framework.config.info;

import org.openmuc.framework.config.ChannelInfo;

public class ChannelSyntax extends ChannelInfo {

    private final String addressSyntax;
    private final String scanSettingsSyntax;

    public ChannelSyntax(String addressSyntax, String scanSettingsSyntax) {
        
        this.addressSyntax = addressSyntax;
        this.scanSettingsSyntax = scanSettingsSyntax;
    }

    @Override
    public String getAddressSyntax() {
        return addressSyntax;
    }

    @Override
    public String getScanSettingsSyntax() {
        return scanSettingsSyntax;
    }
}
