package com.prohect.sql_frontend.common.packet;

import com.prohect.sql_frontend.common.Packet;

public class AbstractPacket implements Packet {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AbstractPacket() {
        id = (long) ((2 * Math.random() - 1) * Long.MAX_VALUE);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "id=" + id +
                '}';
    }
}
