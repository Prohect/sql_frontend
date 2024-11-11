package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
public final class SUpdatePacket extends AbstractPacket implements Serializable {
    int theID;

    public SUpdatePacket() {
    }

    public SUpdatePacket(int theID) {
        this.theID = theID;
    }

    public int getTheID() {
        return theID;
    }

    public void setTheID(int theID) {
        this.theID = theID;
    }
}
