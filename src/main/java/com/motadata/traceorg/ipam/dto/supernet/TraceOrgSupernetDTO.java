package com.motadata.traceorg.ipam.dto.supernet;

public class TraceOrgSupernetDTO
{
    String networkAddress;

    String networkMask;

    public String getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }

    public String getNetworkMask() {
        return networkMask;
    }

    public void setNetworkMask(String networkMask) {
        this.networkMask = networkMask;
    }
}
