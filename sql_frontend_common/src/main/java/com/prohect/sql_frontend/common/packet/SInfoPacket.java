package com.prohect.sql_frontend.common.packet;

public class SInfoPacket extends AbstractPacket {
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
