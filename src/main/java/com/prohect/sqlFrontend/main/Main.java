package com.prohect.sqlFrontend.main;

import com.prohect.sqlFrontend.NettyClient;
import com.prohect.sqlFrontend.main.login.ClientConfig;
import com.prohect.sqlFrontend.main.login.LoginLogic;
import com.prohect.sqlFrontend.main.newColumn.InsertNewColumnLogic;
import com.prohect.sqlFrontend.main.newRow.InsertNewRowLogic;
import com.prohect.sqlFrontendCommon.ColumnMetaData;
import com.prohect.sqlFrontendCommon.Logger;
import com.prohect.sqlFrontendCommon.User;
import com.prohect.sqlFrontendCommon.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public final static ConcurrentHashMap<Channel, LinkedBlockingQueue<Packet>> channel2packetsMap = new ConcurrentHashMap<>();
    public static final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    public static final Logger logger;
    public static final HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2tb2columnMD = new HashMap<>();
    /**
     * notice: this contains the user db's db name and table name but, that columnMetadata list would be empty
     */
    public static final HashMap<String, HashMap<String, ObservableList<TableColumn<Object[], ?>>>> db2tb2tableColumn = new HashMap<>();
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
    public static HashMap<Integer, UpdateOfCellOfTable> packetID2updatedValueMap = new HashMap<>();
    public static HashMap<Integer, Object[]> packetID2insertedValueMap = new HashMap<>();
    public static HashMap<Integer, Object[]> packetID2DeletedValueMap = new HashMap<>();

    static {
        logger = new Logger("client");
    }

    public static void setAndRunNewNettyClient(NettyClient client) {
        if (Main.client != null) {
            Main.client.close();
        }
        Main.client = client;
        Main.client.run();
    }

}
