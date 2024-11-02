package com.prohect.sql_frontend_common.packet;

import java.io.Serializable;

public class SDeletePacket extends AbstractPacket implements Serializable {
    long theID;

    public SDeletePacket(long theID) {
        this.theID = theID;
    }

    public SDeletePacket() {
    }

    public long getTheID() {
        return theID;
    }

    public void setTheID(long theID) {
        this.theID = theID;
    }
}
