package com.prohect.sql_frontend.main;

import com.prohect.sql_frontend.NettyClient;
import com.prohect.sql_frontend.main.login.ClientConfig;
import com.prohect.sql_frontend.main.login.LoginLogic;
import com.prohect.sql_frontend.main.newColumn.InsertNewColumnLogic;
import com.prohect.sql_frontend.main.newRow.InsertNewRowLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.Logger;
import com.prohect.sql_frontend_common.User;
import com.prohect.sql_frontend_common.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public final static ConcurrentHashMap<Channel, LinkedBlockingQueue<Packet>> channel2packetsMap = new ConcurrentHashMap<>();
    public static final Logger logger;
    public static final HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2tb2columnMD = new HashMap<>();
    /**
     * notice: this contains the user db's db name and table name but, that columnMetadata list would be empty
     */
    public static final HashMap<String, HashMap<String, ArrayList<TableColumn<Object[], ?>>>> db2tb2tableColumn = new HashMap<>();
    public static final HashMap<String, HashMap<String, ObservableList<Object[]>>> db2tb2items = new HashMap<>();
    public static ClientConfig clientConfig;
    public static LoginLogic loginLogic;
    public static MainLogic mainLogic;
    public static InsertNewRowLogic insertNewRowLogic;
    public static InsertNewColumnLogic insertNewColumnLogic;
    public static User user;
    public static ChannelHandlerContext ctx;
    public static NettyClient client;
    public static LinkedBlockingQueue<Packet> packetReceivedQueue = new LinkedBlockingQueue<>();
    public static HashMap<Long, UpdateOfCellOfTable> packetID2updatedValueMap = new HashMap<>();
    public static HashMap<Long, Object[]> packetID2insertedValueMap = new HashMap<>();
    public static HashMap<Long, Object[]> packetID2DeletedValueMap = new HashMap<>();

    static {
        try {
            logger = new Logger("client");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setAndRunNewNettyClient(NettyClient client) {
        if (Main.client != null) {
            Main.client.close();
        }
        Main.client = client;
        Main.client.run();
    }

}
