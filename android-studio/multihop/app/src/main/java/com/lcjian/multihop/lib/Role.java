package com.lcjian.multihop.lib;

public enum Role {

    SENDER,

    FORWARDER,

    RECEIVER;

    private String nextDeviceName;

    public String getNextDeviceName() {
        return nextDeviceName;
    }

    public Role setNextDeviceName(String nextDeviceName) {
        this.nextDeviceName = nextDeviceName;
        return this;
    }
}
