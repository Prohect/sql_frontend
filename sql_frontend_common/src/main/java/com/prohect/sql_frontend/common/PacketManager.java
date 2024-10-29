package com.prohect.sql_frontend.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.prohect.sql_frontend.common.packet.*;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketManager {

    static AtomicBoolean inited = new AtomicBoolean(false);

    static HashMap<String, Packet> prefixPacketMap = new HashMap<>();

    public static Class<? extends Packet> getPacketClassByPrefix(String prefix) {
        if (!inited.get()) init();
        Packet packet = prefixPacketMap.get(prefix);
        return packet.getClass();
    }

    public static Packet convertPacket(String string) {
        Packet packet = JSON.parseObject(string, Packet.class);
        return JSON.parseObject(string, getPacketClassByPrefix(packet.getPrefix()));
    }


    public static Packet convertPacket(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append((char) aByte);
        }
        System.out.println("builder = " + builder);

        Packet packet = JSON.parseObject(bytes, Packet.class);
        Packet packet1 = null;
        try {
            packet1 = JSON.parseObject(bytes, PacketManager.getPacketClassByPrefix(packet.getPrefix()));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("packet1.getPrefix() = " + packet1.getPrefix());
        return packet1;
    }

    public static Packet convertPacket(byte[] bytes) throws JSONException, NullPointerException {
        Packet packet = JSON.parseObject(bytes, Packet.class);

        Packet packet1 = JSON.parseObject(bytes, PacketManager.getPacketClassByPrefix(packet.getPrefix()));
        if (packet1 == null) throw new NullPointerException("packet1 == null");
        return packet1;
    }

    public static void init() {
        prefixPacketMap.clear();
        ArrayList<Packet> list = new ArrayList<>();
        list.add(new CLoginPacket());
        list.add(new SLoginPacket());
        list.add(new CQueryPacket());
        list.add(new SQueryReplyPacket());
        list.add(new SInfoPacket());
        list.add(new CAlterPacket());
        list.add(new CUpdatePacket());
        list.add(new SUpdatePacket());
        list.add(new CInsertPacket());
        list.add(new SInsertPacket());
        list.add(new CDeletePacket());
        list.add(new SDeletePacket());
        list.forEach((packet) -> prefixPacketMap.put(packet.getPrefix(), packet));
        inited.set(true);
    }

    static {
        init();
    }
}
