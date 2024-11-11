package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;


@SuppressWarnings("unused")
public sealed abstract class AbstractPacket implements Packet, Serializable permits CAlterPacket, CDeletePacket, CInsertPacket, CLoginPacket, CQueryPacket, CUpdatePacket, SDeletePacket, SInfoPacket, SInsertPacket, SLoginPacket, SQueryReplyPacket, SUpdatePacket {
    private int id;

    public AbstractPacket() {
        id = (int) ((2 * Math.random() - 1) * Integer.MAX_VALUE);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "id=" + id +
                '}';
    }
}
