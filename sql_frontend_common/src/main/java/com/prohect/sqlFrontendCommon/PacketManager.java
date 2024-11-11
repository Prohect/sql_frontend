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

    public static Packet convertPacket(byte[] bytes) throws Exception {
        if (!initialized) init();
        Packet object = null;
        Exception e = null;
        try {
            object = JSONB.parseObject(bytes, Packet.class, autoTypeFilter);
        } catch (Exception e1) {
            e = e1;
        }
        if (object instanceof Packet packet)
            return packet;
        else {
            StringBuilder s = new StringBuilder();
            for (byte b : bytes) s.append((char) b);
            if (Logger.logger != null) Logger.logger.log("cant convert to packet:\t" + s);
            if (e != null) throw e;
            throw new JSONException("parsed object is not a packet");
        }
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
