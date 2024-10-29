package com.prohect.sql_frontend.common.packet;

public class SDeletePacket extends AbstractPacket {
    long theID;

    public long getTheID() {
        return theID;
    }

    public void setTheID(long theID) {
        this.theID = theID;
    }

    public SDeletePacket(long theID) {
        this.theID = theID;
    }

    public SDeletePacket() {
    }
}
