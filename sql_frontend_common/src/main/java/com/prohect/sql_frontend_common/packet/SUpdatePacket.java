package com.prohect.sql_frontend_common.packet;

public class SUpdatePacket extends AbstractPacket {
    long theID;

    public SUpdatePacket() {
    }

    public SUpdatePacket(long theID) {
        this.theID = theID;
    }

    public long getTheID() {
        return theID;
    }

    public void setTheID(long theID) {
        this.theID = theID;
    }
}
