package com.prohect.sqlFrontendServer;

import com.prohect.sqlFrontendCommon.Util;
import com.prohect.sqlFrontendCommon.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;


@ChannelHandler.Sharable
public class ServerHandlerAdapter extends ChannelInboundHandlerAdapter {
    EventLoopGroup workerGroup;

    /**
     * each inBuffer for a specified ctx, should be final once created, don't replace it with new one
     */
    Map<Channel, ByteBuf> channel2in;
    Map<Channel, ReentrantLock> channel2lockOfIn;
    Map<Channel, ScheduledFuture<?>> channel2packetsEncoder;
    Map<Channel, Future<?>> channel2packetDecoderFuture;

    public ServerHandlerAdapter(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;

        channel2in = new ConcurrentHashMap<>();
        channel2lockOfIn = new ConcurrentHashMap<>();
        channel2packetsEncoder = new ConcurrentHashMap<>();
        channel2packetDecoderFuture = new ConcurrentHashMap<>();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        channel2packetsEncoder.get(ctx.channel()).cancel(true);
        Server.ctx2packetReceivedMap.remove(ctx);
        Server.ctx2packetToBeSentMap.remove(ctx);
        Server.logger.log("exceptionCaught\t" + "ctx = " + ctx + ", cause = " + cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channel2packetsEncoder.get(ctx.channel()).cancel(true);
        Server.ctx2packetReceivedMap.remove(ctx);
        Server.ctx2packetToBeSentMap.remove(ctx);
        Server.logger.log("channelInactive\t" + "ctx = " + ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LinkedBlockingQueue<Packet> packets = new LinkedBlockingQueue<>();
        channel2packetsEncoder.put(ctx.channel(), Util.encoderRegister(workerGroup, ctx, packets, 25));
        Server.ctx2packetToBeSentMap.put(ctx, packets);
        Server.ctx2packetReceivedMap.put(ctx, new LinkedBlockingQueue<>());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        channel2packetDecoderFuture.put(ctx.channel(), Util.getPackets_concurrent(workerGroup, channel2packetDecoderFuture.get(ctx.channel()), (ByteBuf) msg, channel2lockOfIn.computeIfAbsent(ctx.channel(), _ -> new ReentrantLock()), channel2in.computeIfAbsent(ctx.channel(), _ -> ctx.alloc().buffer(1048576)), Server.ctx2packetReceivedMap.get(ctx)));
    }

}
