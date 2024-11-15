package com.prohect.sqlFrontendCommon.packet;

import java.nio.channels.SocketChannel;

public record Channel2Packet(SocketChannel key, Packet value) {
}

