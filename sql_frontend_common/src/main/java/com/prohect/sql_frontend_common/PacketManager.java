package com.prohect.sql_frontend_common;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.prohect.sql_frontend_common.packet.*;

import java.util.ArrayList;
import java.util.HashMap;

public class PacketManager {

    static boolean initialized = false;

    final static HashMap<String, Packet> prefixPacketMap = new HashMap<>();

    public static Class<? extends Packet> getPacketClassByPrefix(String prefix) {
        if (!initialized) init();
        Packet packet = prefixPacketMap.get(prefix);
        return packet.getClass();
    }

    public static Packet convertPacket(byte[] bytes) throws JSONException, NullPointerException {
        Packet packet0 = JSONB.parseObject(bytes, Packet.class);
        Packet packet = JSONB.parseObject(bytes, PacketManager.getPacketClassByPrefix(packet0.getPrefix()));
        if (packet == null) throw new NullPointerException("packet == null");
        return packet;
    }

    private static void init() {
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
        initialized = true;
    }

    static {
        init();
    }
}
