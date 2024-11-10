package com.prohect.sqlFrontendCommon.packet;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;

public interface Packet extends java.io.Serializable {
    default byte[] toBytesWithClassInfo() {
        return JSONB.toBytes(this, JSONWriter.Feature.WriteClassName);
    }
}
