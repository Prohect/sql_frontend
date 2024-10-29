package com.prohect.sql_frontend.common.packet;

public class SInsertPacket extends AbstractPacket {
    long theID;

    public long getTheID() {
        return theID;
    }

    public void setTheID(long theID) {
        this.theID = theID;
    }

    public SInsertPacket(long theID) {
        this.theID = theID;
    }

    public SInsertPacket() {
    }
}
