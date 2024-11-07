package com.prohect.sql_frontend_common.packet;

import java.io.Serializable;

public class SInfoPacket extends AbstractPacket implements Serializable {
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