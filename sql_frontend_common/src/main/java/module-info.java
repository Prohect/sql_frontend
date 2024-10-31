module sql.frontend.common {
    exports com.prohect.sql_frontend_common;
    exports com.prohect.sql_frontend_common.packet;
    requires com.alibaba.fastjson2;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
}