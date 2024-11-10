package com.prohect.sql_frontend_common.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
public class SInsertPacket extends AbstractPacket implements Serializable {
    long theID;

    public SInsertPacket(long theID) {
        this.theID = theID;
    }

    public SInsertPacket() {
    }

    public long getTheID() {
        return theID;
    }

    public void setTheID(long theID) {
        this.theID = theID;
    }
}
