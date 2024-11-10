package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
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
