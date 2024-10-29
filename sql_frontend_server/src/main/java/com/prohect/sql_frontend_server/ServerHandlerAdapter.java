package com.prohect.sql_frontend_server;

import com.prohect.sql_frontend.common.CommonUtil;
import com.prohect.sql_frontend.common.Packet;
import com.prohect.sql_frontend.common.PacketDecodeCell;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.LinkedBlockingQueue;

import static com.prohect.sql_frontend_server.Server.ctx2packetReceivedMap;
import static com.prohect.sql_frontend_server.Server.ctx2packetToBeSentMap;

public class ServerHandlerAdapter extends ChannelInboundHandlerAdapter {
    ScheduledFuture<?> packetsEncoder;
    EventLoopGroup workerGroup;
    LinkedBlockingQueue<Packet> packets;
    Future<?> packetDecoderFuture;
    /**
     * should be final once created, don't replace it with new one
     */
    PacketDecodeCell packetDecodeCell;

    public ServerHandlerAdapter(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
        this.packets = new LinkedBlockingQueue<>();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        packetsEncoder.cancel(true);
        ctx2packetReceivedMap.remove(ctx);
        ctx2packetToBeSentMap.remove(ctx);
        System.out.println("exceptionCaught\t" + "ctx = " + ctx + ", cause = " + cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        packetsEncoder.cancel(true);
        ctx2packetReceivedMap.remove(ctx);
        ctx2packetToBeSentMap.remove(ctx);
        System.out.println("channelInactive\t" + "ctx = " + ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.packetsEncoder = CommonUtil.encoderRegister(workerGroup, packets, ctx);
        Server.ctx2packetToBeSentMap.put(ctx, packets);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (packetDecodeCell == null) packetDecodeCell = new PacketDecodeCell(ctx.alloc().buffer(1048576));//1MegaBytes
        packetDecoderFuture = CommonUtil.getPackets(workerGroup, packetDecoderFuture, (ByteBuf) msg, packetDecodeCell, ctx2packetReceivedMap.computeIfAbsent(ctx, k -> new LinkedBlockingQueue<>()));
    }

}
