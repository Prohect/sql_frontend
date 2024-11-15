package com.prohect.sqlFrontendCommon;

import com.prohect.sqlFrontendCommon.packet.PacketsWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.*;


class SeverTest {
    //    static ExecutorService executorService = Executors.newCachedThreadPool();
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    static int port = 25566;
    static ByteBuffer byteBuffer = ByteBuffer.allocate(65536);
    static PacketsWriter packetsWriter;
    static Selector selector2accept_AND_read;
    static ServerSocketChannel serverSocketChannel;

    @BeforeAll
    @SuppressWarnings("resource")
    static void setUp() throws IOException {
        System.out.println("SeverTest.setUp");

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        selector2accept_AND_read = Selector.open();

        serverSocketChannel.register(selector2accept_AND_read, SelectionKey.OP_ACCEPT);

        System.out.println("服务器启动，监听端口 " + port);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                selector2accept_AND_read.select();
                Iterator<SelectionKey> iterator = selector2accept_AND_read.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        ServerSocketChannel acceptChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = acceptChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector2accept_AND_read, SelectionKey.OP_READ);
                        System.out.println("新连接: " + socketChannel.getRemoteAddress());
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        int read = 0;
                        try {
                            read = socketChannel.read(byteBuffer);
                        } catch (IOException e) {
                            socketChannel.close();
                        }
                        if (read > 0) {
                            byteBuffer.flip();
                            byte[] data = new byte[byteBuffer.remaining()];
                            byteBuffer.get(data);
                            String message = new String(data);
                            System.out.println("收到消息: " + message);

                            String response = "消息已收到";
                            byteBuffer.clear();
                            byteBuffer.put(response.getBytes());
                            byteBuffer.flip();
                            do socketChannel.write(byteBuffer);
                            while (byteBuffer.hasRemaining());
                            byteBuffer.clear();
                        } else if (read == -1) {
                            // 客户端断开连接
                            socketChannel.close();
                            System.out.println("客户端断开连接");
                        }
                    }
                    // 移除已处理的键
                    iterator.remove();
                }
            } catch (IOException e) {
                Logger.logger.log(e);
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

//        packetsWriter = new PacketsWriter(scheduledExecutorService, 50);
    }

    @AfterEach
    @SuppressWarnings("PrintStackTrace")
    void tearDown() {
        System.out.println("SeverTest.tearDown");
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            Logger.logger.log(e);
        }
        packetsWriter.shutdown();
        scheduledExecutorService.shutdown();
        System.out.println("服务器关闭");
    }

    @Test
    void rw() throws InterruptedException {
        System.out.println("SeverTest.rw");

        Thread.sleep(1000000);
    }
}