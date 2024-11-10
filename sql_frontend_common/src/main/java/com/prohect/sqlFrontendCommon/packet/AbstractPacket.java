package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;


@SuppressWarnings("unused")
public class AbstractPacket implements Packet, Serializable {
    private long id;

    public AbstractPacket() {
        id = (long) ((2 * Math.random() - 1) * Long.MAX_VALUE);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "id=" + id +
                '}';
    }
}