package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
public final class SInfoPacket extends Packet implements Serializable {
    String info;

    public SInfoPacket(String info) {
        this.info = info;
    }

    public SInfoPacket() {
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}