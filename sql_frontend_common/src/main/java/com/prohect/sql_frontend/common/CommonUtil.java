package com.prohect.sql_frontend.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommonUtil {

    public static ScheduledFuture<?> encoderRegister(final EventLoopGroup workerGroup, final LinkedBlockingQueue<Packet> packets, final ChannelHandlerContext ctx) {
        return workerGroup.scheduleAtFixedRate(() -> {
            try {
                Packet packet = packets.poll();
                if (packet == null) return;
                System.out.print("发送" + packet);
                ctx.writeAndFlush(Unpooled.copiedBuffer(JSON.toJSONBytes(packet)));
                System.out.println("成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 125, TimeUnit.MILLISECONDS);
    }

    public static Future<?> getPackets(EventLoopGroup workerGroup, Future<?> future, ByteBuf msg, PacketDecodeCell packetDecodeCell, LinkedBlockingQueue<Packet> out) {
        final ByteBuf copiedMsg = msg.copy();
        msg.release();
        return workerGroup.submit(() -> {
            if (future != null && !future.isDone())
                future.addListener(f -> processInIntoPackets2Out(copiedMsg, packetDecodeCell, out));
            else processInIntoPackets2Out(copiedMsg, packetDecodeCell, out);
        });
    }

    private static void processInIntoPackets2Out(ByteBuf msg, PacketDecodeCell packetDecodeCell, LinkedBlockingQueue<Packet> out) {
        //上次成功解包指针位于packetDecodeCell.in.readerIndex(), 失败的指针位于packetDecodeCell.lastFailIndex
        ByteBuf in = packetDecodeCell.in;
        in.writeBytes(msg);
        msg.release();
        int lastSuccessReaderIndex = in.readerIndex();
        in.readerIndex(packetDecodeCell.lastFailIndex);
        int readableBytes = in.readableBytes();//本次要读的量,已经扣除上次失败时读过的量
        List<Packet> packets = new ArrayList<>();
        for (int i = 0; i < readableBytes; i++) {
            byte b = in.readByte();
            if ((char) b == '}') {
                byte[] bytes = new byte[i + 1 + packetDecodeCell.lastFailIndex];
                in.readerIndex(lastSuccessReaderIndex);
                in.readBytes(bytes);
                try {
                    Packet packet = PacketManager.convertPacket(bytes);
                    packets.add(packet);
                    System.out.println("收到" + packet);
                    //解包成功, 重置fori的循环参数, 记录此时的readerIndex
                    readableBytes = in.readableBytes();
                    lastSuccessReaderIndex = in.readerIndex();
                    i = 0;
                } catch (JSONException | NullPointerException ignored) {
                }
            }
        }
        in.readerIndex(lastSuccessReaderIndex);
        in.discardReadBytes();
        packetDecodeCell.setLastFailIndex(in.readerIndex());// 先丢弃已解包数据，再保存已经尝试过的指针
        if (packets.isEmpty()) {
            debug_nonPacketUnpacked(in, lastSuccessReaderIndex);
        }
        out.addAll(packets);
    }

    private static void debug_nonPacketUnpacked(ByteBuf in, int lastSuccessReaderIndex) {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        in.readerIndex(lastSuccessReaderIndex);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append((char) aByte);
        }
        System.out.println("nonPacket is unpacked, in = " + stringBuilder);
        System.out.println("the length of in is " + bytes.length);
    }

    public static TableColumn<Object[], Object> getTableColumn(String columnName, int columnIndex) {
        TableColumn<Object[], Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> {
            return cellData.getValue()[columnIndex] != null ? new SimpleObjectProperty<>(cellData.getValue()[columnIndex]) : new SimpleObjectProperty<>(null);
        });
        return column;
    }

    public static void setCellFactory(TableColumn<Object[], Object> column) {
        column.setCellFactory(TextFieldTableCell.<Object[], Object>forTableColumn(new StringConverter<Object>() {

            @Override
            public String toString(Object object) {
                if (object == null) {
                    return "";
                }
                return object.toString();
            }

            @Override
            public Object fromString(String string) {
                return string;
            }
        }));
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
