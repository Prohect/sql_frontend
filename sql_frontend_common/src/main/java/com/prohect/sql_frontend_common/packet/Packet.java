package com.prohect.sql_frontend_common.packet;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;

public interface Packet extends java.io.Serializable {
    default String getPrefix() {
        return "%s".formatted(this.getClass().getSimpleName());
    }

    default byte[] toBytesWithClassInfo() {
        return JSONB.toBytes(this, JSONWriter.Feature.WriteClassName);
    }
}
