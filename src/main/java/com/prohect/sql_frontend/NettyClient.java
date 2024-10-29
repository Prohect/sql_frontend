package com.prohect.sql_frontend;

import com.prohect.sql_frontend.main.Main;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


public class NettyClient {
    public static CopyOnWriteArrayList<EventLoopGroup> threadGroups = new CopyOnWriteArrayList<>();
    final EventLoopGroup workerGroup = new NioEventLoopGroup(12);
    String host;
    int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
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

    public ChannelFuture run(Bootstrap b) throws Exception {
        threadGroups.add(workerGroup);

        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_RCVBUF, 262144).option(ChannelOption.SO_SNDBUF, 262144); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ClientHandlerAdapter(Main.client.getHost(), Main.client.getPort(), b, workerGroup));
            }
        });

        // Start the client.
        return b.connect(host, port); // (5)
    }

}