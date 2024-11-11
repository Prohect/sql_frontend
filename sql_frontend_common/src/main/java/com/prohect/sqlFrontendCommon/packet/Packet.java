package com.prohect.sqlFrontendCommon.packet;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;

import java.io.Serializable;

public sealed interface Packet extends Serializable permits AbstractPacket {
    default byte[] toBytes() {
        return JSONB.toBytes(this, JSONWriter.Feature.WriteClassName);
    }
}
