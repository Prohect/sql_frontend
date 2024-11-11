package com.prohect.sqlFrontendCommon;

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
     * note: only be used when the buf of NIO 's size is lower than the packet size!
     */
    public static int packetSize = -1;
    /**
     * note: only be used when the buf of NIO 's size is lower than the packet size!
     */
    public static byte[] packetBytes;
    public static int index = 0;

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
                    if (Logger.logger != null) Logger.logger.log("发送" + packet);
                    byte[] jsonBytes = packet.toBytes();
                    byte[] lengthBytes = new byte[4];
                    for (int i = 1; i < 5; i++) {
                        lengthBytes[i - 1] = (byte) (jsonBytes.length >> 32 - i * 8);
                    }
                    ctx.write(Unpooled.copiedBuffer(lengthBytes));
                    ctx.write(Unpooled.copiedBuffer(jsonBytes));
                    ctx.flush();
                }
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
            if (future != null)
                future.addListener(_ -> processInIntoPackets2Out_concurrent(workerGroup, lock, in, out));
            else processInIntoPackets2Out_concurrent(workerGroup, lock, in, out);
        });
    }

    private static void processInIntoPackets2Out_concurrent(EventLoopGroup workerGroup, ReentrantLock lock, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        if (in.readableBytes() < 4) return;
        int lastReaderIndex = in.readerIndex();
        if (lock.tryLock()) {
            try {
                while (in.readableBytes() >= 4) {
                    if (packetSize != -1) {//meaning there is a packet on receiving whose size is larger than the size of ByteBuf of NIO, so keep reading to a byte array
                        byte[] bytes = new byte[Math.min(in.readableBytes(), packetSize - index)];
                        in.readBytes(bytes);//1
                        int read = (in.readerIndex() - lastReaderIndex);
                        lastReaderIndex = in.readerIndex();
                        System.arraycopy(bytes, 0, packetBytes, index, read);
                        index += read;//5
                        if (index == packetSize) {
                            try {
                                Packet packet = PacketManager.convertPacket(packetBytes);
                                out.offer(packet);
                            } catch (Exception e) {
                                if (Logger.logger != null) Logger.logger.log(e);
                            }
                            index = 0;
                            packetSize = -1;
                            packetBytes = null;
                        }
                    } else {
                        int packetLength = 0;
                        for (int i = 1; i < 5; i++) packetLength |= ((in.readByte() & 0xFF) << 32 - i * 8);
                        if (in.readableBytes() < packetLength) {
                            if (!in.isWritable()) {
                                packetSize = packetLength;
                                packetBytes = new byte[packetLength];
                                index = 0;
                                byte[] bytes = new byte[Math.min(packetLength, in.readableBytes())];
                                in.readBytes(bytes);//1
                                int read = (in.readerIndex() - lastReaderIndex - 4);//-4 for the length header
                                lastReaderIndex = in.readerIndex();
                                System.arraycopy(bytes, 0, packetBytes, index, read);
                                index += read;//5
                            } else break;//wait for chanel.read to write and call this the next time
                        } else {
                            try {
                                int readableBytes = in.readableBytes();//NegativeArraySizeException: -1837549514
                                byte[] bytes = new byte[readableBytes < 0 ? packetLength : Math.min(packetLength, in.readableBytes())];
                                in.readBytes(bytes);
                                Packet packet = PacketManager.convertPacket(bytes);
                                out.offer(packet);
                            } catch (Exception e) {
                                if (Logger.logger != null) Logger.logger.log(e);
                                break;
                            } finally {
                                lastReaderIndex = in.readerIndex();
                            }
                        }
                    }
                }
                in.readerIndex(lastReaderIndex);
                in.discardReadBytes();
            } finally {
                lock.unlock();
            }
        } else {
            workerGroup.schedule(() -> processInIntoPackets2Out_concurrent(workerGroup, lock, in, out), 50, TimeUnit.MILLISECONDS);
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
