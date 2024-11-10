package com.prohect.sqlFrontendCommon;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.filter.Filter;
import com.prohect.sqlFrontendCommon.packet.*;

import java.util.ArrayList;
import java.util.List;

public class PacketManager {
    static Filter autoTypeFilter;
    static boolean initialized = false;

    static {
        init();
    }

    public static Packet convertPacket(byte[] bytes) throws JSONException {
        if (!initialized) init();
        Object object = JSONB.parseObject(bytes, Object.class, autoTypeFilter);
        if (object instanceof Packet packet)
            return packet;
        else throw new JSONException("nonSupported packet type");
    }

    private static void init() {
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
        List<Class<? extends Packet>> packetClassList = new ArrayList<>();
        list.forEach((packet) -> packetClassList.add(packet.getClass()));
        autoTypeFilter = JSONReader.autoTypeFilter(packetClassList.toArray(new Class[list.size()]));
        initialized = true;
    }
}