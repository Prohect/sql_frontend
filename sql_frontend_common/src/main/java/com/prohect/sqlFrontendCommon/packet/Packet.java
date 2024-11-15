package com.prohect.sqlFrontendCommon.packet;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.prohect.sqlFrontendCommon.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("unused")
public sealed abstract class Packet implements Serializable permits CAlterPacket, CDeletePacket, CInsertPacket, CLoginPacket, CQueryPacket, CUpdatePacket, SDeletePacket, SInfoPacket, SInsertPacket, SLoginPacket, SQueryReplyPacket, SUpdatePacket {
    private static final Filter autoTypeFilter = JSONReader.autoTypeFilter(new ArrayList<Class<? extends Packet>>() {{
        add(Packet.class);
        add(CAlterPacket.class);
        add(CDeletePacket.class);
        add(CInsertPacket.class);
        add(CLoginPacket.class);
        add(CQueryPacket.class);
        add(CUpdatePacket.class);
        add(SDeletePacket.class);
        add(SInfoPacket.class);
        add(SInsertPacket.class);
        add(SLoginPacket.class);
        add(SQueryReplyPacket.class);
        add(SUpdatePacket.class);
    }}.toArray(new Class[0]));
    private int id;

    public Packet() {
        id = (int) ((2 * Math.random() - 1) * Integer.MAX_VALUE);
    }

    public static Optional<Packet> fromBytes(byte[] bytes) {
        try {
            var object = JSONB.parseObject(bytes, Packet.class, autoTypeFilter);
            if (object instanceof Packet packet) return Optional.of(packet);
            return Optional.empty();
        } catch (Exception e) {
            try {
                //bytes转为string可能太长干扰日志，所以只在文件里记录完整的bytes
                Logger.logger.log2fileLn("cant convert to packet:\t" + new String(bytes));
                Logger.logger.log2consoleLn("cant convert to packet, check log for more information");
                Logger.logger.log(e);
            } catch (Exception ignored) {
            }
            return Optional.empty();
        }
    }

    public final byte[] toBytes() {
        return JSONB.toBytes(this, JSONWriter.Feature.WriteClassName);
    }


    public final List<byte[]> toBytesWithLength() {
        byte[] bytes = toBytes();
        byte[] lengthBytes = new byte[4];
        for (int i = 1; i < 5; i++) lengthBytes[i - 1] = ((byte) (bytes.length >> 32 - i * 8));
        return List.of(lengthBytes, bytes);
    }


    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "{" + "id=" + id + '}';
    }
}
