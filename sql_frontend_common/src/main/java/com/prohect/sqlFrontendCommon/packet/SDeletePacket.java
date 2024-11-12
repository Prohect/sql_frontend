package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;

@SuppressWarnings("unused")
public final class SDeletePacket extends AbstractPacket implements Serializable {
    int theID;

    public SDeletePacket(int theID) {
        this.theID = theID;
    }

    public SDeletePacket() {
    }

    public int getTheID() {
        return theID;
    }

    public void setTheID(int theID) {
        this.theID = theID;
    }
}
