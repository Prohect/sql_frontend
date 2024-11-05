package com.prohect.test;

import com.prohect.sql_frontend.NettyClient;
import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.packet.Packet;
import com.prohect.sql_frontend_common.packet.SInfoPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TestClient {
    public static void main(String[] args) throws Exception {
        LinkedBlockingQueue<Packet> packet2sendQueue = new LinkedBlockingQueue<>();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        final ScheduledFuture<?>[] encoderFuture = new ScheduledFuture<?>[1];
        Future<Void> run = new NettyClient("127.0.0.1", 7891, new Bootstrap(), workerGroup, new ChannelInboundHandlerAdapter() {

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                System.out.println("TestClient.channelUnregistered");
                super.channelUnregistered(ctx);
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                System.out.println("TestClient.channelReadComplete");
                super.channelReadComplete(ctx);
            }

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                System.out.println("TestClient.userEventTriggered");
                super.userEventTriggered(ctx, evt);
            }

            @Override
            public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                System.out.println("TestClient.channelWritabilityChanged");
                super.channelWritabilityChanged(ctx);
            }

            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                System.out.println("TestClient.channelRegistered");
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                System.out.println("TestClient.channelActive");
                workerGroup.submit(() -> {
                    encoderFuture[0] = CommonUtil.encoderRegister(workerGroup, ctx, packet2sendQueue, 1);
                    int loops = 100;
                    for (int i = 0; i < loops; i++) {
                        if (i == 70) {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        Channel channel = ctx.channel();
                        Packet msg = new Packet() {
                            @Override
                            public String getPrefix() {
                                return "a";
                            }
                        };
                        channel.write(msg);
                        ctx.write(msg);
                        packet2sendQueue.add(new SInfoPacket("Test Packet #" + i));
//                    byte[] jsonBytes = JSONB.toBytes(new SInfoPacket("Test Packet #" + i));
//                    byte[] lengthBytes = new byte[4];
//                    for (int j = 1; j < 5; j++) {
//                        lengthBytes[j - 1] = (byte) (jsonBytes.length >> 32 - j * 8);
//                    }
//                    ctx.write(Unpooled.copiedBuffer(lengthBytes));
//                    ctx.writeAndFlush(Unpooled.copiedBuffer(jsonBytes));
                    }
                });


            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("TestClient.channelRead");
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                System.out.println("TestClient.channelInactive");
                encoderFuture[0].cancel(true);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println("TestClient.exceptionCaught");
                encoderFuture[0].cancel(true);
            }
        }).run();
        run.addListener(f -> {
            System.out.println("TestClient.main.run.listener-----------------");
        });
        workerGroup.scheduleAtFixedRate(() -> {
            System.out.println("run.isDone() = " + run.isDone());
            System.out.println("run.isSuccess() = " + run.isSuccess());
        }, 0, 50, TimeUnit.MILLISECONDS);
        try {
            run.sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            workerGroup.shutdownGracefully();
        }

    }
}
