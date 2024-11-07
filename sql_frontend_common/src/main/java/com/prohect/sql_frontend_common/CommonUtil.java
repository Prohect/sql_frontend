package com.prohect.sql_frontend_common;

import com.alibaba.fastjson2.JSONException;
import com.prohect.sql_frontend_common.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class CommonUtil {

    /**
     * @return a future for u to close this infinite run loop
     * @apiNote this loop do not shut itself down from inside
     */
    public static ScheduledFuture<?> encoderRegister(final EventLoopGroup workerGroup, final ChannelHandlerContext ctx, final LinkedBlockingQueue<Packet> packets, final long period) {
        return workerGroup.scheduleAtFixedRate(() -> {
            try {
                if (packets.isEmpty()) return;
                for (; ; ) {
                    Packet packet = packets.poll();
                    if (packet == null) break;
                    System.out.printf("发送%s%n", packet);
                    byte[] jsonBytes = packet.toBytesWithClassInfo();
                    byte[] lengthBytes = new byte[4];
                    for (int i = 1; i < 5; i++) {
                        lengthBytes[i - 1] = (byte) (jsonBytes.length >> 32 - i * 8);
                    }
                    ctx.write(Unpooled.copiedBuffer(lengthBytes));
                    ctx.write(Unpooled.copiedBuffer(jsonBytes));
                }
                ctx.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    /**
     * copy the msg to in and submit decode mission to a new thread of the threadGroup.
     *
     * @param future the returned future of the last call of this method
     * @return a future for this method itself to check if last call of this method is already done
     */
    public static Future<?> getPackets_concurrent(EventLoopGroup workerGroup, final Future<?> future, ByteBuf msg, ReentrantLock lock, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        in.writeBytes(msg);
        msg.release();
        return workerGroup.submit(() -> {
            if (future != null)
                future.addListener(_ -> processInIntoPackets2Out_concurrent(lock, in, out));
            else processInIntoPackets2Out_concurrent(lock, in, out);
        });
    }

    private static void processInIntoPackets2Out_concurrent(ReentrantLock lock, ByteBuf in, LinkedBlockingQueue<Packet> out) {
        if (in.readableBytes() < 4) return;
//        System.out.printf("CommonUtil.processInIntoPackets2Out_concurrent called by\t%s%n", Thread.currentThread().getName());
        int lastSuccessReaderIndex = in.readerIndex();
        if (lock.tryLock()) {
            try {
                while (in.readableBytes() >= 4) {
                    int packetLength = 0;
                    for (int i = 1; i < 5; i++) packetLength |= ((in.readByte() & 0xFF) << 32 - i * 8);
                    try {
                        byte[] bytes = new byte[packetLength];
                        in.readBytes(bytes);
                        out.offer(PacketManager.convertPacket(bytes));
                        lastSuccessReaderIndex = in.readerIndex();
                    } catch (IndexOutOfBoundsException ignored) {
                        break;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                in.readerIndex(lastSuccessReaderIndex);
                in.discardReadBytes();
            } finally {
                lock.unlock();
            }
        }
//        System.out.printf("CommonUtil.processInIntoPackets2Out_concurrent ended by\t%s%n", Thread.currentThread().getName());
    }

    private static void debug_nonPacketUnpacked(ByteBuf in, int lastSuccessReaderIndex) {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        in.readerIndex(lastSuccessReaderIndex);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append((char) aByte);
        }
        System.out.printf("nonPacket is unpacked, in = %s%n", stringBuilder);
        System.out.printf("the length of in is %d%n", bytes.length);
    }


    public static String permissionColumnNameEncode(String dataBase4tableView, String table4tableView, String columnName, boolean read4falseWrite4true) {
        return "P_" + (dataBase4tableView + "_" + table4tableView + "_" + columnName).toLowerCase() + "_" + (read4falseWrite4true ? "Write" : "Read");
    }

    public static String[] permissionColumnNameDecode(String context) {
        return context.substring(2).split("_");
    }

    public static String convert2SqlServerContextString(Object o) {
        if (o == null) {
            throw new NullPointerException("o is null");
        }
        return o instanceof String string ? "'" + string + "'" : (o instanceof Boolean b) ? b ? "1" : "0" : o.toString();
    }

    public static boolean isNumber(String str) {
        if (str == null) return false;
        try {
            long l = Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(str);
                return true;
            } catch (NumberFormatException e1) {
                return false;
            }
        }
    }

    /**
     * map0 should be larger than map1, and is somehow created by adding something to map1.
     * then we find what's been changed
     */
    public static <M extends Map<K, V>, K, V, K1, V1, T> M diffMap(M map0, M map1) {
        M diffMap = deepCloneMap(map0);
        for (Map.Entry<K, V> entry1 : map1.entrySet()) {
            K k1 = entry1.getKey();
            V v1 = entry1.getValue();
            if (map0.containsKey(k1)) {
                V v = map0.get(k1);
                if (v instanceof Map<?, ?> innerMap0 && v1 instanceof Map<?, ?> innerMap1) {
                    Map<K1, V1> subDiffMap = diffMap((Map<K1, V1>) innerMap0, (Map<K1, V1>) innerMap1);
                    if (subDiffMap.isEmpty()) diffMap.remove(k1);
                    else diffMap.put(k1, (V) subDiffMap);
                } else if (v instanceof List<?> list0 && v1 instanceof List<?> list1) {
                    List<T> subDiffList = diffList((List<T>) list0, (List<T>) list1);
                    if (subDiffList.isEmpty()) diffMap.remove(k1);
                    else diffMap.put(k1, (V) subDiffList);
                } else if (v == null || v.equals(v1)) diffMap.remove(k1, v);
            }
        }
        return diffMap;
    }

    /**
     * list0 should be larger than list1, and is somehow created by adding something to list1.
     * then we find what's been changed
     */
    public static <L extends List<T>, T, T1, K, V> L diffList(L list0, L list1) {
        L diffList = deepCloneList(list0);
        L removed = (L) new ArrayList<>();
        for (int i = 0; i < list1.size(); i++) {
            T v1 = list1.get(i);
            T v0 = list0.get(i);
            if (v0 instanceof List<?> l0 && v1 instanceof List<?> l1) {
                List<T1> subDiffList = diffList((List<T1>) l0, (List<T1>) l1);
                if (subDiffList.isEmpty()) removed.add(v0);
                else diffList.set(i, (T) subDiffList);
            } else if (v0 instanceof Map<?, ?> map0 && v1 instanceof Map<?, ?> map1) {
                Map<K, V> subDiffMap = diffMap((Map<K, V>) map0, (Map<K, V>) map1);
                if (subDiffMap.isEmpty()) removed.add(v0);
                else diffList.set(i, (T) subDiffMap);
            } else if (v0 == null || v0.equals(v1)) removed.add(v0);
        }
        diffList.removeAll(removed);
        return diffList;
    }

    public static <M extends Map<K, V>, K, V, K1, V1, L> M deepCloneMap(M map) {
        M clone = switch (map) {
            case LinkedHashMap<?, ?> _ -> (M) new LinkedHashMap<>();
            case HashMap<?, ?> _ -> (M) new HashMap<>();
            case TreeMap<?, ?> _ -> (M) new TreeMap<>();
            case ConcurrentHashMap<?, ?> _ -> (M) new ConcurrentHashMap<>();
            case Properties _ -> (M) new Properties();
            case Hashtable<?, ?> _ -> (M) new Hashtable<>();
            case IdentityHashMap<?, ?> _ -> (M) new IdentityHashMap<>();
            case WeakHashMap<?, ?> _ -> (M) new WeakHashMap<>();
            case EnumMap<?, ?> _ -> (M) new EnumMap<>(map.keySet().iterator().next().getClass());
            case ConcurrentSkipListMap<?, ?> _ -> (M) new ConcurrentSkipListMap<>();
            default -> throw new IllegalStateException("Unexpected value: " + map.getClass());
        };
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (value instanceof Map<?, ?> innerMap) clone.put(key, (V) deepCloneMap((Map<K1, V1>) innerMap));
            else if (value instanceof List<?> innerList) clone.put(key, (V) deepCloneList((List<L>) innerList));
            else clone.put(key, value);
        }
        return clone;
    }

    public static <L extends List<T>, T, T1, K, V> L deepCloneList(L list) {
        L clone = switch (list) {// who use Collections.SynchronizedList<?> & Collections.UnmodifiableList<?> as his list to work, never
            case ArrayList<?> _ -> (L) new ArrayList<>();
            case LinkedList<?> _ -> (L) new LinkedList<>();
            case Stack<?> _ -> (L) new Stack<>();
            case Vector<?> _ -> (L) new Vector<>();
            case CopyOnWriteArrayList<?> _ -> (L) new CopyOnWriteArrayList<>();
            default -> throw new IllegalStateException("Unexpected value: " + list.getClass());
        };
        for (T t : list)
            if (t instanceof List<?> innerList) clone.add((T) deepCloneList((List<T1>) innerList));
            else if (t instanceof Map<?, ?> innerMap) clone.add((T) deepCloneMap((Map<K, V>) innerMap));
            else clone.add(t);
        return clone;
    }
}
