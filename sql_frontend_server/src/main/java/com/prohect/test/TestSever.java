package com.prohect.test;

import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.packet.Packet;
import com.prohect.sql_frontend_common.packet.SInfoPacket;
import com.prohect.sql_frontend_server.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class TestSever {
    public static void main(String[] args) throws Exception {
/*        String num = "1.23335e2";
        try {
            System.out.println("Integer.parseInt(num) = " + Integer.parseInt(num));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("(int)Double.parseDouble(num) = " + (int)Double.parseDouble(num));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }*/
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(12);
        LinkedBlockingQueue<Packet> packetReceivedQueue = new LinkedBlockingQueue<>();
        final ByteBuf[] in = new ByteBuf[1];
        final Future<?>[] future = {null};
        final ReentrantLock[] lock = {new ReentrantLock()};
        AtomicBoolean first = new AtomicBoolean(true);
        workerGroup.scheduleAtFixedRate(() -> {
            if (first.get()) {
                System.out.println("TestSever.main.packet processor online");
                first.set(false);
            }
            for (; ; ) {
                Packet packet = packetReceivedQueue.poll();
                if (packet == null) break;
                switch (packet) {
                    case SInfoPacket sInfoPacket ->
                            System.out.println("sInfoPacket.getInfo() = " + sInfoPacket.getInfo());
                    case null, default -> throw new IllegalStateException("Unexpected value: %s".formatted(packet));
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
        new NettyServer(7891, new ServerBootstrap(), workerGroup, new NioEventLoopGroup(2), new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                in[0] = ctx.alloc().buffer(1048576);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                ByteBuf buf = (ByteBuf) msg;
//                byte[] bytes = new byte[buf.readableBytes()];
//                buf.readBytes(bytes);
//                StringBuilder stringBuilder = new StringBuilder();
//                for (int i = 0; i < bytes.length; i++) {
//                    stringBuilder.append((char) bytes[i]);
//                }
//                Thread.sleep(150);
//                System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
//                System.out.printf("stringBuilder = %s%n", stringBuilder);
                future[0] = CommonUtil.getPackets_concurrent(workerGroup, future[0], (ByteBuf) msg, lock[0], in[0], packetReceivedQueue);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {

            }
        }).run();
    }
}
