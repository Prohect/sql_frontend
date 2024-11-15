package com.prohect.sqlFrontendCommon;

import com.prohect.sqlFrontendCommon.packet.PacketsWriter;
import com.prohect.sqlFrontendCommon.packet.SInfoPacket;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.*;

class ClientTest {
    //    static ExecutorService executorService = Executors.newCachedThreadPool();
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    static int port = 25566;
    static SocketChannel socketChannel;
    static Selector selector;
    static ByteBuffer byteBuffer = ByteBuffer.allocate(65535);
    static PacketsWriter packetsWriter;


    @BeforeAll
    @SuppressWarnings("resource")
    static void setUp() throws IOException {
        System.out.println("ClientTest.setUp");
        // 创建SocketChannel
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        // 连接到服务器
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", port);
        boolean isConnected = socketChannel.connect(serverAddress);

        // 创建Selector
        selector = Selector.open();

        // 注册SocketChannel到Selector
        if (!isConnected) {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } else {
            socketChannel.register(selector, SelectionKey.OP_READ);
        }

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                // 等待事件发生
                selector.select();

                // 获取已选择的键集
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isConnectable()) {
                        // 处理连接请求
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        channel.register(selector, SelectionKey.OP_READ);

                        // 发送消息给服务器
                        String message = "Hello, Server!";
                        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                        channel.write(buffer);
                    } else if (key.isReadable()) {
                        // 处理读事件
                        SocketChannel channel = (SocketChannel) key.channel();
                        int read = channel.read(byteBuffer);
                        if (read > 0) {
                            byteBuffer.flip();
                            byte[] data = new byte[byteBuffer.remaining()];
                            byteBuffer.get(data);
                            String message = new String(data);
                            System.out.println("收到消息: " + message);
                            byteBuffer.clear();
//                            byteBuffer.put("hello".getBytes());
//                            byteBuffer.flip();
//                            do channel.write(byteBuffer);
//                            while (byteBuffer.hasRemaining());
//                            byteBuffer.clear();
                        } else if (read == -1) {
                            // 服务器关闭连接
                            channel.close();
                            System.out.println("服务器关闭连接");
                        }
                    }

                    // 移除已处理的键
                    iterator.remove();
                }
            } catch (IOException e) {
                Logger.logger.log(e);
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

        packetsWriter = new PacketsWriter(scheduledExecutorService, 50);
    }

    @AfterAll
    static void tearDown() throws IOException {
        System.out.println("ClientTest.tearDown");
        socketChannel.close();
        selector.close();
        packetsWriter.shutdown();
    }

    @Test
    void rw() throws InterruptedException {
        System.out.println("ClientTest.rw");
        for (int i = 0; i < 100000; i++) {
            packetsWriter.addPacket2send(socketChannel, new SInfoPacket("hello" + i));
        }
        Thread.sleep(1000000);
    }
}