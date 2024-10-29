package com.prohect.sql_frontend.common;

import io.netty.buffer.ByteBuf;

public class PacketDecodeCell {
    final ByteBuf in;
    /**
     * records the final i in foriLoop last time decoding non packet from the Bytebuf
     */
    int lastFailIndex;

    public PacketDecodeCell(ByteBuf in) {
        this.in = in;
        this.lastFailIndex = 0;
    }

    public ByteBuf getIn() {
        return in;
    }

    public int getLastFailIndex() {
        return lastFailIndex;
    }

    public void setLastFailIndex(int lastFailIndex) {
        this.lastFailIndex = lastFailIndex;
    }
}
