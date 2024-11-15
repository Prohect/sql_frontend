package com.prohect.sqlFrontendCommon.packet;

import com.prohect.sqlFrontendCommon.Logger;

import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public final class Packets {
    private static final ConcurrentMap<SocketChannel, ConcurrentLinkedQueue<Packet>> channel2packetsRead = new ConcurrentHashMap<>();

    public static void addPacketRead(SocketChannel channel, Packet packet) {
        if (channel == null) {
            Logger.logger.log(new IllegalArgumentException());
            return;
        }
        if (packet == null) {
            Logger.logger.log(new IllegalArgumentException());
            return;
        }
        channel2packetsRead.computeIfAbsent(channel, _ -> new ConcurrentLinkedQueue<>()).add(packet);
    }

    public static Optional<Channel2Packet> getPacketRead() {
        return channel2packetsRead.entrySet().stream().filter(e -> !e.getValue().isEmpty()).findFirst().map(entry -> new Channel2Packet(entry.getKey(), entry.getValue().poll()));
    }

    public static void remove(SocketChannel channel) {
        channel2packetsRead.remove(channel);
    }
}
