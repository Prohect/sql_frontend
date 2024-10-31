package com.prohect.sql_frontend_common;

import com.alibaba.fastjson2.JSONB;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommonUtil {

    public static ScheduledFuture<?> encoderRegister(final EventLoopGroup workerGroup, final LinkedBlockingQueue<Packet> packets, final ChannelHandlerContext ctx) {
        return workerGroup.scheduleAtFixedRate(() -> {
            try {
                Packet packet = packets.poll();
                if (packet == null) return;
                System.out.printf("发送%s", packet);
                byte[] jsonBytes = JSONB.toBytes(packet);
                byte[] packetBytes = new byte[jsonBytes.length + 4];
                byte[] lengthBytes = new byte[4];
                for (int i = 1; i < 5; i++) {
                    lengthBytes[i - 1] = (byte) (jsonBytes.length >> i * 8);
                }
                System.arraycopy(lengthBytes, 0, packetBytes, 0, 4);
                System.arraycopy(jsonBytes, 0, packetBytes, 4, jsonBytes.length);
                ctx.writeAndFlush(Unpooled.copiedBuffer(packetBytes));
                System.out.println("成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 125, TimeUnit.MILLISECONDS);
    }

    private static void processInIntoPackets2Out(ByteBuf msg, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        in.writeBytes(msg);
        msg.release();
        if (in.readableBytes() < 4) return;
        int lastSuccessReaderIndex = in.readerIndex();
        while (in.readableBytes() >= 4) {
            int packetLength = 0;
            for (int i = 1; i < 5; i++) {
                packetLength |= ((in.readByte() & 0xFF) << i * 8);
            }
            byte[] bytes = new byte[packetLength];
            try {
                in.readBytes(bytes);
                out.add(PacketManager.convertPacket(bytes));
                lastSuccessReaderIndex = in.readerIndex();
            } catch (IndexOutOfBoundsException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        in.readerIndex(lastSuccessReaderIndex);
        in.discardReadBytes();
    }

    public static Future<?> getPackets(EventLoopGroup workerGroup, Future<?> future, ByteBuf msg, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        final ByteBuf copiedMsg = msg.copy();
        msg.release();
        return workerGroup.submit(() -> {
            if (future != null && !future.isDone())
                future.addListener(f -> processInIntoPackets2Out(copiedMsg, in, out));
            else processInIntoPackets2Out(copiedMsg, in, out);
        });
    }

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
        return "P_" + (dataBase4tableView + "_" + table4tableView + "_" + columnName).toLowerCase() + "_" + (read4falseWrite4true ? "Write" : "Read");
    }

    public static String[] permissionColumnNameDecode(String context) {
        return context.substring(2).split("_");
    }

    public static String convert2SqlServerContextString(Object o) {
        if (o == null) {
            return "";
        }
        return o instanceof String string ? "'" + string + "'" : (o instanceof Boolean b) ? b ? "1" : "0" : o.toString();
    }

    public static boolean isNumber(String str) {
        if (str == null) return false;
        try {
            long l = Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(str);
                return true;
            } catch (NumberFormatException e1) {
                return false;
            }
        }
    }
}
