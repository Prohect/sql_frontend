package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
public class SUpdatePacket extends AbstractPacket implements Serializable {
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
