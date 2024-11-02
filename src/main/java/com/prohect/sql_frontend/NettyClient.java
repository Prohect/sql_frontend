package com.prohect.sql_frontend;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    public static CopyOnWriteArrayList<EventLoopGroup> threadGroups = new CopyOnWriteArrayList<>();
    final EventLoopGroup workerGroup;
    final ChannelInboundHandlerAdapter[] channelInboundHandlerAdapters;
    final Bootstrap b;
    String host;
    int port;

    /**
     * @param channelInboundHandlerAdapters each instance of this list must be annotated by @ChannelHandler.Sharable,
     *                                      which could mean the inside properties that every connection have differing from other connection's must be mapped from channelHandlerContext.
     *                                      If u don't think that much, just write all properties as HashMap&lt;ChannelHandlerContext, propertyClass&gt;
     */
    public NettyClient(String host, int port, Bootstrap b, EventLoopGroup workerGroup, ChannelInboundHandlerAdapter... channelInboundHandlerAdapters) {
        this.host = host;
        this.port = port;
        this.b = b;
        this.workerGroup = workerGroup;
        this.channelInboundHandlerAdapters = channelInboundHandlerAdapters;

        threadGroups.add(workerGroup);
        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_RCVBUF, 262144).option(ChannelOption.SO_SNDBUF, 262144); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                for (ChannelInboundHandlerAdapter channelInboundHandlerAdapter : channelInboundHandlerAdapters) {
                    ch.pipeline().addLast(channelInboundHandlerAdapter);
                }
            }
        });
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void close() {
        workerGroup.shutdownGracefully(200, 200, TimeUnit.MILLISECONDS);
    }

    public ChannelFuture run() throws Exception {
        // Start the client.
        return b.connect(host, port); // (5)
    }

}