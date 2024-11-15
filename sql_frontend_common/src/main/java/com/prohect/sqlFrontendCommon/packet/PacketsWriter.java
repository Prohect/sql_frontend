package com.prohect.sqlFrontendCommon.packet;

import com.prohect.sqlFrontendCommon.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public final class PacketsWriter {
    /**
     * 存储每个channel需要发送的Packet对象
     */
    private final ConcurrentMap<SocketChannel, ConcurrentLinkedQueue<Packet>> channel2packet = new ConcurrentHashMap<>();
    /**
     * 存储每个channel需要发送的数据
     */
    private final ConcurrentMap<SocketChannel, ConcurrentLinkedQueue<byte[]>> channel2lengthFollowedByPacketBytes = new ConcurrentHashMap<>();
    /**
     * 存储每个channel的锁，保证每个channel的写操作是串行化的
     */
    private final ConcurrentMap<SocketChannel, ReentrantLock> channel2lock4write = new ConcurrentHashMap<>();
    /**
     * 存储每个channel的写等待selector，写入缓冲区阻滞时候，会阻塞当前线程，另一起个schedule用于写后续channel的数据到内核，直到selector被唤醒
     */
    private final ConcurrentMap<SocketChannel, Selector> channel2selector4write = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService;
    /**
     * 写任务的执行周期，单位为毫秒
     */
    private final long period;


    /**
     * 构造一个写任务，用于将数据写入内核
     *
     * @param executorService 该对象使用的线程池
     * @param period          写任务的执行周期，单位为毫秒
     * @see java.util.concurrent.ScheduledExecutorService
     */
    public PacketsWriter(ScheduledExecutorService executorService, long period) {
        this.executorService = executorService;
        this.period = period;

        this.executorService.scheduleAtFixedRate(new Task(), 0, period, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unused")
    private PacketsWriter() {
        throw new IllegalStateException();
    }

    /**
     * 将一个packet的地址加入到发送队列中然后直接返回不会阻塞当前线程，再并发通过scheduleAtFixedRate的方式，在合适的时机将数据写入内核
     *
     * @param channel 数据发往的channel
     */
    public void addPacket2send(SocketChannel channel, Packet packet) {
        if (channel == null) {
            Logger.logger.log(new IllegalArgumentException());
            return;
        }
        if (packet == null) {
            Logger.logger.log(new IllegalArgumentException());
            return;
        }
        if (executorService.isShutdown()) {
            Logger.logger.log(new IllegalStateException());
            return;
        }
        channel2packet.computeIfAbsent(channel, _ -> new ConcurrentLinkedQueue<>()).add(packet);
    }

    public void shutdown() {
        for (var selector : channel2selector4write.values()) {
            try {
                selector.close();
            } catch (IOException e) {
                Logger.logger.log(e);
            }
        }
    }


    private final class Task implements Runnable {

        private Task() {
        }

        @Override
        public void run() {
            long timeStart = System.currentTimeMillis();
            long timeSpent;
            for (Map.Entry<SocketChannel, ConcurrentLinkedQueue<Packet>> entry : channel2packet.entrySet()) {
                for (; ; ) {
                    var packet = entry.getValue().poll();
                    if (packet == null) break;
                    var bytes = packet.toBytesWithLength();
                    channel2lengthFollowedByPacketBytes.computeIfAbsent(entry.getKey(), _ -> new ConcurrentLinkedQueue<>()).addAll(bytes);
                }
            }
            int count = 0;
            var iterator = channel2lengthFollowedByPacketBytes.entrySet().iterator();
            int size = channel2lengthFollowedByPacketBytes.entrySet().size();
            for (int i = 0; i < size; i++) {
                var entry = iterator.next();
                var channel = entry.getKey();
                var lock4write = channel2lock4write.computeIfAbsent(channel, _ -> new ReentrantLock());
                if (!lock4write.tryLock()) continue;
                var bytesOnQueuing = entry.getValue();
                var bytes = bytesOnQueuing.poll();
                if (bytes == null) continue;
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                int written;
                try {
                    do {
                        try {
                            written = channel.write(buffer);
                            if (written == 0) {
                                // 阻塞等待 channel 可写, 创建新的单次schedule执行别的channel的写入
                                executorService.schedule(this, 0, java.util.concurrent.TimeUnit.MILLISECONDS);
                                var selector4write = channel2selector4write.computeIfAbsent(channel, _ -> {
                                    try {
                                        Selector selector = Selector.open();
                                        channel.register(selector, SelectionKey.OP_WRITE);
                                        return selector;
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                channel.keyFor(selector4write).interestOps(SelectionKey.OP_WRITE);
                                selector4write.select();
                                continue;
                            }
                        } catch (IOException e) {
                            Logger.logger.log(e);
                        }
                        if (!buffer.hasRemaining()) {
                            count++;
                            bytes = bytesOnQueuing.poll();
                            if (bytes == null) break;
                            buffer = ByteBuffer.wrap(bytes);
                        }
                    } while (true);
                } catch (Exception e) {
                    Logger.logger.log(e);
                } finally {
                    lock4write.unlock();
                }
                timeSpent = System.currentTimeMillis() - timeStart;
                Logger.logger.log(List.of("写入" + count + "个packet, 耗时" + timeSpent + "ms"));
            }
            timeSpent = System.currentTimeMillis() - timeStart;
            if (timeSpent > period) Logger.logger.log("写入超时:" + timeSpent + "ms, 目标" + period + "ms");
        }
    }
}
