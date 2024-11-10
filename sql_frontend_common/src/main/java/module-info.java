module sql.frontend.common {
    exports com.prohect.sqlFrontendCommon;
    exports com.prohect.sqlFrontendCommon.packet;
    requires com.alibaba.fastjson2;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
}