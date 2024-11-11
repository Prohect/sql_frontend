package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
public final class SInsertPacket extends AbstractPacket implements Serializable {
    int theID;

    public SInsertPacket(int theID) {
        this.theID = theID;
    }

    public SInsertPacket() {
    }

    public int getTheID() {
        return theID;
    }

    public void setTheID(int theID) {
        this.theID = theID;
    }
}
