package com.prohect.sqlFrontendCommon;

import com.prohect.sqlFrontendCommon.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Utils {
    public static int packetSizeOverCapacityOfByteBuf = -1;
    public static ByteBuf packetBytes;

    /**
     * this loop do not shut itself down from inside
     *
     * @return a future for u to close this infinite run loop, which could be used to shut this loop down
     */
    public static ScheduledFuture<?> encoderRegister(final EventLoopGroup workerGroup, final ChannelHandlerContext ctx, final LinkedBlockingQueue<Packet> packets, final long period) {
        return workerGroup.scheduleAtFixedRate(() -> {
            try {
                for (; ; ) {
                    Packet packet = packets.poll();
                    if (packet == null) break;
                    byte[] lengthBytes = new byte[4];
                    byte[] jsonBytes = packet.toBytes();
                    for (int i = 1; i < 5; i++) lengthBytes[i - 1] = (byte) (jsonBytes.length >> 32 - i * 8);
                    ctx.write(Unpooled.copiedBuffer(lengthBytes));
                    ctx.write(Unpooled.copiedBuffer(jsonBytes));
                    ctx.flush();
                    if (Logger.logger != null) Logger.logger.log("发送" + packet + "\tsize = " + jsonBytes.length);
                }
            } catch (Exception e) {
                if (Logger.logger != null) Logger.logger.log(e);
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    /**
     * copy the msg to in and submit decode mission to a new thread of the threadGroup.
     */
    public static void getPackets_concurrent(EventLoopGroup workerGroup, ByteBuf msg, ReentrantLock lock, SynchronizedByteBuf in, LinkedBlockingQueue<Packet> out) {
        ByteBuf copy = msg.copy();
        msg.release();
        workerGroup.submit(() -> {
            in.writeBytes(copy);
            if (!lock.tryLock()) return;
            try {
                try {
                    processByteBufIntoPackets2Out(in, out);
                } catch (Exception e) {
                    if (Logger.logger != null) Logger.logger.log(e);
                }
            } finally {
                lock.unlock();
            }
        });
    }

    private static void processByteBufIntoPackets2Out(SynchronizedByteBuf in, LinkedBlockingQueue<Packet> out) {
        int lastReaderIndex = in.readerIndex();
        while (in.readableBytes() > 0) {
            if (packetSizeOverCapacityOfByteBuf != -1) {
                in.readBytes(packetBytes, Math.min(packetBytes.writableBytes(), in.readableBytes()));
                lastReaderIndex = in.readerIndex();
                if (packetBytes.isWritable()) continue;
                byte[] array = new byte[packetSizeOverCapacityOfByteBuf];
                packetBytes.readBytes(array);
                Packet.fromBytes(array).ifPresent(out::offer);
                packetSizeOverCapacityOfByteBuf = -1;
                packetBytes.release();
                packetBytes = null;
                continue;
            }

            int packetLength = 0;
            for (int i = 1; i < 5; i++) packetLength |= ((in.readByte() & 0xFF) << 32 - i * 8);
            int readable = in.readableBytes();
            if (readable < packetLength) {
                if (in.isWritable()) break;
                packetSizeOverCapacityOfByteBuf = packetLength;
                packetBytes = in.alloc().buffer(packetLength);
                in.readBytes(packetBytes, readable);
                lastReaderIndex = in.readerIndex();
                continue;
            }

            byte[] bytes = new byte[packetLength];
            in.readBytes(bytes);
            Packet.fromBytes(bytes).ifPresent(out::offer);
            lastReaderIndex = in.readerIndex();
        }
        in.readerIndex(lastReaderIndex);
        in.discardReadBytes();
    }

    @SuppressWarnings("unused")
    private static void debug_nonPacketUnpacked(ByteBuf in, int lastSuccessReaderIndex) {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        in.readerIndex(lastSuccessReaderIndex);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append((char) aByte);
        }
        System.out.printf("nonPacket is unpacked, in = %s%n", stringBuilder);
        System.out.printf("the length of in is %d%n", bytes.length);
    }


    public static String permissionColumnNameEncode(String dataBase4tableView, String table4tableView, String columnName, boolean read4falseWrite4true) {
        return "[P_" + (dataBase4tableView + "_" + table4tableView + "_" + columnName).toLowerCase() + "_" + (read4falseWrite4true ? "Write" : "Read") + "]";
    }

    public static String[] permissionColumnNameDecode(String context) {
        return context.substring(3, context.length() - 1).split("_");
    }

    public static String convert2SqlServerContextString(Object o) {
        if (o == null) {
            throw new NullPointerException("o is null");
        }
        return o instanceof String string ? "'%s'".formatted(string) : (o instanceof Boolean b) ? b ? "1" : "0" : o.toString();
    }

    public static Object isNumber(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e1) {
                return null;
            }
        }
    }
}
