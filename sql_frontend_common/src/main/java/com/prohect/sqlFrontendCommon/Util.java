package com.prohect.sqlFrontendCommon;

import com.alibaba.fastjson2.JSONException;
import com.prohect.sqlFrontendCommon.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Util {

    /**
     * @return a future for u to close this infinite run loop
     * @apiNote this loop do not shut itself down from inside
     */
    public static ScheduledFuture<?> encoderRegister(final EventLoopGroup workerGroup, final ChannelHandlerContext ctx, final LinkedBlockingQueue<Packet> packets, final long period) {
        return workerGroup.scheduleAtFixedRate(() -> {
            try {
                if (packets.isEmpty()) return;
                for (; ; ) {
                    Packet packet = packets.poll();
                    if (packet == null) break;
                    byte[] jsonBytes = packet.toBytesWithClassInfo();
                    byte[] lengthBytes = new byte[4];
                    for (int i = 1; i < 5; i++) {
                        lengthBytes[i - 1] = (byte) (jsonBytes.length >> 32 - i * 8);
                    }
                    ctx.write(Unpooled.copiedBuffer(lengthBytes));
                    ctx.write(Unpooled.copiedBuffer(jsonBytes));
                }
                ctx.flush();
            } catch (Exception ignored) {
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    /**
     * copy the msg to in and submit decode mission to a new thread of the threadGroup.
     *
     * @param future the returned future of the last call of this method
     * @return a future for this method itself to check if last call of this method is already done
     */
    public static Future<?> getPackets_concurrent(EventLoopGroup workerGroup, final Future<?> future, ByteBuf msg, ReentrantLock lock, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        in.writeBytes(msg);
        msg.release();
        return workerGroup.submit(() -> {
            if (future != null) future.addListener(_ -> processInIntoPackets2Out_concurrent(lock, in, out));
            else processInIntoPackets2Out_concurrent(lock, in, out);
        });
    }

    private static void processInIntoPackets2Out_concurrent(ReentrantLock lock, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        if (in.readableBytes() < 4) return;
        int lastSuccessReaderIndex = in.readerIndex();
        if (lock.tryLock()) {
            try {
                while (in.readableBytes() >= 4) {
                    int packetLength = 0;
                    for (int i = 1; i < 5; i++) packetLength |= ((in.readByte() & 0xFF) << 32 - i * 8);
                    try {
                        byte[] bytes = new byte[packetLength];
                        in.readBytes(bytes);
                        Packet packet = PacketManager.convertPacket(bytes);
                        out.offer(packet);
                        lastSuccessReaderIndex = in.readerIndex();
                    } catch (IndexOutOfBoundsException | JSONException ignored) {
                        break;
                    }
                }
                in.readerIndex(lastSuccessReaderIndex);
                in.discardReadBytes();
            } finally {
                lock.unlock();
            }
        }
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
        if (str == null) return false;
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
