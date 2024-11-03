package com.prohect.sql_frontend.main;

import com.prohect.sql_frontend.NettyClient;
import com.prohect.sql_frontend.main.login.ClientConfig;
import com.prohect.sql_frontend.main.login.LoginLogic;
import com.prohect.sql_frontend.main.newRow.InsertNewRowLogic;
import com.prohect.sql_frontend.main.newColumn.InsertNewColumnLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.User;
import com.prohect.sql_frontend_common.packet.Packet;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public final static ConcurrentHashMap<ChannelHandlerContext, LinkedBlockingQueue<Packet>> ctx2packetsMap = new ConcurrentHashMap<>();
    public static ClientConfig clientConfig;
    public static LoginLogic loginLogic;
    public static MainLogic mainLogic;
    public static InsertNewRowLogic insertNewRowLogic;
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
        return Main.client.run();
    }

}
