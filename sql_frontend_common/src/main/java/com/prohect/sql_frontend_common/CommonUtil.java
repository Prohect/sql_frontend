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
            if (future != null) future.addListener(_ -> processInIntoPackets2Out_concurrent(lock, in, out));
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
                        Packet packet = PacketManager.convertPacket(bytes);
                        System.out.printf("接收%s%n", packet);
                        out.offer(packet);
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

    public static <M extends Map<K, V>, K, V, K1, V1, T> void mergeMap(M map, M diffMap) {
        for (Map.Entry<K, V> entry : diffMap.entrySet()) {
            K k1 = entry.getKey();
            V v1 = entry.getValue();
            if (map.containsKey(k1)) {
                V v = map.get(k1);
                if (v instanceof Map<?, ?> innerMap && v1 instanceof Map<?, ?> innerMapDiff)
                    mergeMap((Map<K1, V1>) innerMap, (Map<K1, V1>) innerMapDiff);
                else if (v instanceof List<?> innerList && v1 instanceof List<?> innerListDiff)
                    mergeList((List<T>) innerList, (List<T>) innerListDiff);
                else map.put(k1, v1);
            } else map.put(k1, v1);
        }
    }

    /**
     * when the contained things of the list is neither Map nor List,
     * probably simple POJO,
     * call hashcode() to find its old version one to be updated,
     * if no old version found simply add it to the list
     */
    public static <L extends List<T>, T, T1, K, V> void mergeList(L list, L diffList) {
        for (int i = 0; i < diffList.size(); i++) {// since we kept the structure of the list when computing diffList, diffList's size would always >= list's size when it's a list containing maps
            T t1 = diffList.get(i);
            if (i < list.size()) {
                T t = list.get(i);
                if (t instanceof Map<?, ?> innerMap && t1 instanceof Map<?, ?> innerMapDiff) {
                    mergeMap((Map<K, V>) innerMap, (Map<K, V>) innerMapDiff);
                    continue;
                } else if (t instanceof List<?> innerList && t1 instanceof List<?> innerListDiff) {
                    mergeList((List<T1>) innerList, (List<T1>) innerListDiff);
                    continue;
                }
            }
            if (t1 instanceof Map<?, ?> || t1 instanceof List<?>) {
                list.add(t1);
                continue;
            }//list or map processed, now we process POJO
            boolean flag = false;
            for (int i1 = 0; i1 < list.size(); i1++) {
                T t = list.get(i1);
                if (t != null && t1 != null && t.hashCode() == t1.hashCode()) {
                    list.set(i1, t1);
                    flag = true;
                    break;
                }
            }
            if (!flag) list.add(t1);
        }
    }

    /**
     * map should be larger than map1, and is somehow created by adding something to map1 or update some of its contents.
     * then we find what's been changed by calling equals()
     */
    public static <M extends Map<K, V>, K, V, K1, V1, T> M diffMap(M map, M map1) {
        M diffMap = structureCloneMap(map);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue();
            if (map1.containsKey(k)) {
                V v1 = map1.get(k);
                if (v instanceof Map<?, ?> innerMap && v1 instanceof Map<?, ?> innerMap1) {//no need to remain structure because we can know whom to merge by the key
                    Map<K1, V1> subDiffMap = diffMap((Map<K1, V1>) innerMap, (Map<K1, V1>) innerMap1);
                    if (subDiffMap.isEmpty()) diffMap.remove(k);
                    else diffMap.put(k, (V) subDiffMap);
                } else if (v instanceof List<?> innerList && v1 instanceof List<?> innerList1) {
                    List<T> subDiffList = diffList((List<T>) innerList, (List<T>) innerList1);
                    if (subDiffList.isEmpty()) diffMap.remove(k);
                    else diffMap.put(k, (V) subDiffList);
                } else if (v != null && v1 != null && !v.equals(v1)) diffMap.put(k, v);
            } else diffMap.put(k, v);
        }
        return diffMap;
    }

    /**
     * list should be larger than list1, and is somehow created by adding something to list1 or update some of its contents.
     * then we find what's been changed by calling equals()
     */
    public static <L extends List<T>, T, T1, K, V> L diffList(L list, L list1) {
        L diffList = structureCloneList(list);
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            if (i < list1.size()) {
                T t1 = list1.get(i);
                if (t instanceof List<?> innerList && t1 instanceof List<?> innerList1) //remain the structure of contained list or innerMap even it's empty,
                    diffList.set(i, (T) diffList((List<T1>) innerList, (List<T1>) innerList1));// because we get things from list by index, we need that index to find out whom to merge
                else if (t instanceof Map<?, ?> innerMap && t1 instanceof Map<?, ?> innerMap1)
                    diffList.set(i, (T) diffMap((Map<K, V>) innerMap, (Map<K, V>) innerMap1));
                else if (t != null && t1 != null && !t.equals(t1)) diffList.add(t);
            } else diffList.add(t);
        }
        return diffList;
    }

    public static <M extends Map<K, V>, K, V, K1, V1, L> M structureCloneMap(M map) {
        M clone = switch (map) {
            case LinkedHashMap<?, ?> _ -> (M) new LinkedHashMap<>();
            case HashMap<?, ?> _ -> (M) new HashMap<>();
            case TreeMap<?, ?> _ -> (M) new TreeMap<>();
            case ConcurrentHashMap<?, ?> _ -> (M) new ConcurrentHashMap<>();
            case Properties _ -> (M) new Properties();
            case Hashtable<?, ?> _ -> (M) new Hashtable<>();
            case IdentityHashMap<?, ?> _ -> (M) new IdentityHashMap<>();
            case WeakHashMap<?, ?> _ -> (M) new WeakHashMap<>();
            case ConcurrentSkipListMap<?, ?> _ -> (M) new ConcurrentSkipListMap<>();
            default -> throw new IllegalStateException("Unexpected value: " + map.getClass());
        };
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue();
            if (v instanceof Map<?, ?> innerMap) clone.put(k, (V) structureCloneMap((Map<K1, V1>) innerMap));
            else if (v instanceof List<?> innerList) clone.put(k, (V) structureCloneList((List<L>) innerList));
        }
        return clone;
    }

    public static <L extends List<T>, T, T1, K, V> L structureCloneList(L list) {
        L clone = switch (list) {// who use Collections.SynchronizedList<?> & Collections.UnmodifiableList<?> as his list to work, never
            case ArrayList<?> _ -> (L) new ArrayList<>();
            case LinkedList<?> _ -> (L) new LinkedList<>();
            case Stack<?> _ -> (L) new Stack<>();
            case Vector<?> _ -> (L) new Vector<>();
            case CopyOnWriteArrayList<?> _ -> (L) new CopyOnWriteArrayList<>();
            default -> throw new IllegalStateException("Unexpected value: " + list.getClass());
        };
        for (T t : list)
            if (t instanceof List<?> innerList) clone.add((T) structureCloneList((List<T1>) innerList));
            else if (t instanceof Map<?, ?> innerMap) clone.add((T) structureCloneMap((Map<K, V>) innerMap));
        return clone;
    }
}
