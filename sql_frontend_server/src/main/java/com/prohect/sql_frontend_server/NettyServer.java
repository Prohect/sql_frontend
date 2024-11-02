package com.prohect.sql_frontend_server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class NettyServer {

    final ServerBootstrap b;
    private final int port;
    EventLoopGroup workerGroup;
    EventLoopGroup bossGroup;
    ChannelInboundHandlerAdapter[] channelInboundHandlerAdapters;

    /**
     * @param channelInboundHandlerAdapters each instance of this list must be annotated by @ChannelHandler.Sharable,
     *                                      which could mean the inside properties that every connection have differing from other connection's must be mapped from channelHandlerContext.
     *                                      If u don't think that much, just write all properties as HashMap&lt;ChannelHandlerContext, propertyClass&gt;
     */
    public NettyServer(int port, ServerBootstrap b, EventLoopGroup workerGroup, EventLoopGroup bossGroup, ChannelInboundHandlerAdapter... channelInboundHandlerAdapters) {
        this.port = port;
        this.b = b; // (1)
        this.workerGroup = workerGroup;
        this.bossGroup = bossGroup;// (2)
        this.channelInboundHandlerAdapters = channelInboundHandlerAdapters;
        ServerBootstrap bootstrap = b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class); // (3)
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        for (ChannelInboundHandlerAdapter channelInboundHandlerAdapter : channelInboundHandlerAdapters) {
                            ch.pipeline().addLast(channelInboundHandlerAdapter);
                        }
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true) // (6)
                .childOption(ChannelOption.SO_RCVBUF, 262144)
                .childOption(ChannelOption.SO_SNDBUF, 262144);
    }

    public void run() throws Exception {
        try {
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}