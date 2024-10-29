package com.prohect.mysql_frontend.main;

import com.prohect.mysql_frontend.NettyClient;
import com.prohect.mysql_frontend.common.ColumnMetaData;
import com.prohect.mysql_frontend.common.Packet;
import com.prohect.mysql_frontend.common.User;
import com.prohect.mysql_frontend.login.ClientConfig;
import com.prohect.mysql_frontend.login.LoginLogic;
import com.prohect.mysql_frontend.main.insert.InsertLogic;
import com.prohect.mysql_frontend.main.insertNewColumn.InsertNewColumnLogic;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static ClientConfig clientConfig;

    public final static ConcurrentHashMap<ChannelHandlerContext, LinkedBlockingQueue<Packet>> ctx2packetsMap = new ConcurrentHashMap<>();
    public static LoginLogic loginLogic;
    public static MainLogic mainLogic;
    public static InsertLogic insertLogic;
    public static InsertNewColumnLogic insertNewColumnLogic;
    public static User user;
    public static HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap;
    public static ChannelHandlerContext ctx;
    public static NettyClient client;
    public static LinkedBlockingQueue<Packet> packetReceivedQueue = new LinkedBlockingQueue<>();
    public static HashMap<Long, UpdateOfCellOfTable> packetID2updatedValueMap = new HashMap<>();
    public static HashMap<Long, Object[]> packetID2insertedValueMap = new HashMap<>();
    public static HashMap<Long, Object[]> packetID2DeletedValueMap = new HashMap<>();

    public static ChannelFuture setAndRunNewNettyClient(NettyClient client) throws Exception {
        if (Main.client != null) {
            Main.client.close();
        }
        Main.client = client;
        try {
            Bootstrap b = new Bootstrap();
            return Main.client.run(b);
        } catch (Exception e) {
            throw e;
        }
    }

}
