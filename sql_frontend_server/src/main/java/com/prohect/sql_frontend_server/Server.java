package com.prohect.sql_frontend_server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.User;
import com.prohect.sql_frontend_common.packet.*;
import com.prohect.sql_frontend_server.sqlUtil.SqlUtil;
import com.prohect.sql_frontend_server.sqlUtil.SqlUtil4Login;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class Server {

    public final static ConcurrentHashMap<ChannelHandlerContext, LinkedBlockingQueue<Packet>> ctx2packetToBeSentMap = new ConcurrentHashMap<>();
    private static final HashMap<String, Connection> databaseName2connectionMap = new HashMap<>();
    private static final File configFile = new File("serverConfig.json");
    private static final HashMap<Long, User> uuid2userMap = new HashMap<>();
    public static ConcurrentHashMap<ChannelHandlerContext, LinkedBlockingQueue<Packet>> ctx2packetReceivedMap = new ConcurrentHashMap<>();
    /**
     * note: turn the key lower case when using!!
     */
    private static HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> database2Table2ColumnMap = new HashMap<>();
    private static Connection connection2UsersDB;
    private final ServerConfig serverConfig;

    public Server(ServerConfig config) {
        this.serverConfig = config;
    }

    public static void main(String[] args) {
        try {
            new Server(loadConfig()).run();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage() + "\r\n" + "检查SQL配置是否正确");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ServerConfig loadConfig() throws IOException {
        if (configFile.createNewFile()) {
            resetConfig();
            return null;
        } else {
            try {
                return JSON.parseObject(Files.readAllBytes(configFile.toPath()), ServerConfig.class);
            } catch (IOException | JSONException e) {
                return resetConfig();
            }
        }
    }

    private static ServerConfig resetConfig() throws IOException {
        DatabaseAdmin databaseAdmin = new DatabaseAdmin("admin114", "114514");
        ServerConfig serverConfig = new ServerConfig("users", "users", List.of("projectsInfo"), List.of("sysdiagrams", "trace_xe_action_map", "trace_xe_event_map"), databaseAdmin, 19336);
        File configFile = new File("serverConfig.json");
        configFile.delete();
        configFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(configFile);
        fileOutputStream.write(JSON.toJSONBytes(serverConfig));
        fileOutputStream.flush();
        fileOutputStream.close();
        return serverConfig;
    }

    public void run() throws SQLException, IOException {
        if (serverConfig == null) {
            System.out.println("没有设置文件，现在已经自动生成，请配置好设置文件之后再启动！");
            return;
        }
        try {
            int serverPort = serverConfig.getServerPort();
            connection2UsersDB = SqlUtil4Login.getConnection4UsersDB(serverConfig);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();//workerGroup处理与sqlServer的通信和与client的通信
            workerGroup.scheduleAtFixedRate(() -> {
                for (Map.Entry<ChannelHandlerContext, LinkedBlockingQueue<Packet>> entry : ctx2packetReceivedMap.entrySet()) {
                    ChannelHandlerContext ctx = entry.getKey();
                    LinkedBlockingQueue<Packet> packets = entry.getValue();
                    for (; ; ) {
                        try {
                            Packet packet = packets.poll();
                            if (packet == null) break;
                            checkConnection();
                            switch (packet) {
                                case CLoginPacket loginPacket -> processLoginPacket(ctx, loginPacket);
                                case CQueryPacket cQueryPacket -> processQueryPacket(ctx, cQueryPacket);
                                case CAlterPacket cAlterPacket -> processAlterPacket(ctx, cAlterPacket);
                                case CUpdatePacket cUpdatePacket -> processUpdatePacket(ctx, cUpdatePacket);
                                case CInsertPacket cInsertPacket -> processInsertPacket(ctx, cInsertPacket);
                                case CDeletePacket cDeletePacket -> processDeletePacket(ctx, cDeletePacket);
                                default -> {
                                }
                            }
                        } catch (SQLException ignored) {
                        }
                    }
                }
            }, 0, 20, TimeUnit.MILLISECONDS);
            ServerBootstrap b = new ServerBootstrap();
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);//bossGroup处理新连接的接入和packet拆箱
            new NettyServer(serverPort, b, workerGroup, bossGroup, new ServerHandlerAdapter(workerGroup)).run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConnection() throws SQLException {
        if (!connection2UsersDB.isValid(1))
            connection2UsersDB = SqlUtil4Login.getConnection4UsersDB(serverConfig);
        for (String databaseName0 : serverConfig.getTheTargetDatabaseNameList()) {
            String databaseName = databaseName0.toLowerCase();
            if (!(databaseName2connectionMap.containsKey(databaseName) && databaseName2connectionMap.get(databaseName).isValid(1)))
                databaseName2connectionMap.put(databaseName, SqlUtil.getConnection4TargetDB(serverConfig.getDataBaseAdmin(), databaseName));
        }
    }

    private void processDeletePacket(ChannelHandlerContext ctx, CDeletePacket cDeletePacket) {
        try {
            User user = uuid2userMap.get(cDeletePacket.getUuid());
            String cmd = cDeletePacket.getCmd();
            String databaseName = cDeletePacket.getDatabaseName().toLowerCase();
            long id = cDeletePacket.getId();

            String tableName = cmd.substring(12).split(" ")[0].toLowerCase();//"DELETE FROM "->12


            if (!user.isOp())
                for (ColumnMetaData columnMetaData : database2Table2ColumnMap.get(databaseName).get(tableName))
                    if (!user.getPermissions().get(databaseName).get(tableName).get(columnMetaData.getColumnName())[1])
                        throw new SQLException("您没有足够的写入权限!");

            Statement statement = (user.isOp() && databaseName.equals(serverConfig.getTheUsersDatabaseName()) ? connection2UsersDB : databaseName2connectionMap.get(databaseName)).createStatement();
            int i = statement.executeUpdate(cmd);
            ctx2packetToBeSentMap.get(ctx).add(new SDeletePacket(id));
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("删除行成功, " + i + "行受影响"));
        } catch (Exception e) {
            e.printStackTrace();
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket(e.getMessage()));
        }
    }

    private void processInsertPacket(ChannelHandlerContext ctx, CInsertPacket cInsertPacket) {
        try {
            long uuid = cInsertPacket.getUuid();
            String cmd = cInsertPacket.getCmd();
            String databaseName = cInsertPacket.getDatabaseName().toLowerCase();
            long id = cInsertPacket.getId();
            User user = uuid2userMap.get(uuid);
            String[] split = cmd.substring(11).split("\\(");//"INSERT INTO"->11
            String tableName = split[0].toLowerCase();
            List<String> columnNames = Arrays.asList(split[1].split("\\)")[0].split(","));
            if (!user.isOp())
                try {
                    for (String columnName : columnNames)
                        if (!user.getPermissions().get(databaseName).get(tableName).get(columnName)[1])
                            throw new SQLException("没有足够的写入权限!");
                } catch (NullPointerException ignored) {
                    throw new RuntimeException("没有足够的写入权限!");
                }
            Statement statement = (user.isOp() && databaseName.equals(serverConfig.getTheUsersDatabaseName()) ? connection2UsersDB : databaseName2connectionMap.get(databaseName)).createStatement();
            int i = statement.executeUpdate(cmd);
            ctx2packetToBeSentMap.get(ctx).add(new SInsertPacket(id));
            ctx2packetToBeSentMap.get(ctx).add((new SInfoPacket("插入行成功! " + i + "行受影响")));
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket(e.getMessage()));
        }
    }

    private void processUpdatePacket(ChannelHandlerContext ctx, CUpdatePacket cUpdatePacket) {
        try {
            long id = cUpdatePacket.getId();
            User user = uuid2userMap.get(cUpdatePacket.getUuid());
            String databaseName = cUpdatePacket.getDatabaseName();
            String updateCMD = cUpdatePacket.getUpdateCMD();
            String[] split = updateCMD.substring(7).split(" ");
            String tableName = split[0];//7->"UPDATE "
            String columnName = split[2];
            if (user.isOp() || user.getPermissions().get(databaseName).get(tableName).get(columnName)[1]) {
                Statement statement = (user.isOp() && databaseName.equals(serverConfig.getTheUsersDatabaseName()) ? connection2UsersDB : databaseName2connectionMap.get(databaseName)).createStatement();
                int i = statement.executeUpdate(updateCMD);
                if (i > 0) {
                    ctx2packetToBeSentMap.get(ctx).add(new SUpdatePacket(id));
                    ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("修改成功," + i + "行受影响"));
                }
                statement.close();
            } else {
                ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("你无权进行这个操作"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket(e.getMessage()));
        }
    }

    private void processAlterPacket(ChannelHandlerContext ctx, CAlterPacket cAlterPacket) {
        String cmd = cAlterPacket.getCmd();
        String databaseName = cAlterPacket.getDatabaseName();
        User user = uuid2userMap.get(cAlterPacket.getUuid());
        if (!user.isOp()) {
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("你无权进行这个操作"));
            return;
        }
        try {
            Statement statement = (user.isOp() && databaseName.equals(serverConfig.getTheUsersDatabaseName()) ? connection2UsersDB : databaseName2connectionMap.get(databaseName)).createStatement();
            int i = statement.executeUpdate(cmd);
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("成功, " + i + "行受影响"));
            statement.close();

            loadMetaDataFromConnection();
            ctx2packetToBeSentMap.get(ctx).add(new SLoginPacket(new User("", "", user.getUuid()), database2Table2ColumnMap, "update metadata", "", ""));

        } catch (SQLException e) {
            e.printStackTrace();
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket(e.getMessage()));
        }
    }

    private void processQueryPacket(ChannelHandlerContext ctx, CQueryPacket cQueryPacket) {
        try {
            m1(ctx, cQueryPacket);
        } catch (SQLException e) {
            loadMetaDataFromConnection();
            try {
                m1(ctx, cQueryPacket);
            } catch (Exception e1) {
                ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("SQLException = " + e1.getMessage()));
                e1.printStackTrace();
            }
        } catch (Exception e) {
            ctx2packetToBeSentMap.get(ctx).add(new SInfoPacket("Exception = " + e.getMessage()));
            e.printStackTrace();
        }
    }

    protected void m1(ChannelHandlerContext ctx, CQueryPacket cQueryPacket) throws SQLException {
        String databaseName = cQueryPacket.getDatabaseName().toLowerCase();
        StringBuilder query = new StringBuilder(cQueryPacket.getQuery().toLowerCase());
        User user = uuid2userMap.get(cQueryPacket.getUuid());

        Statement finalStatement = (user.isOp() && databaseName.equals(serverConfig.getTheUsersDatabaseName()) ? connection2UsersDB : databaseName2connectionMap.get(databaseName)).createStatement();
        String[] querySplitByFrom = query.toString().split("from");
        String tableName;
        List<String> selectedColumnList;
        if (querySplitByFrom.length > 1) {
            selectedColumnList = new ArrayList<>(List.of(querySplitByFrom[0].replaceFirst("select ", "").split(",")));
            String[] split1 = querySplitByFrom[1].split(" ");
            tableName = split1[1];
        } else throw new SQLException("语法错误");//no "from", bad sql query

        HashMap<String, HashMap<String, Boolean[]>> permissions4ThisDatabase = user.getPermissions().get(databaseName);
        if (permissions4ThisDatabase != null && !user.isOp()) {//do no limit if there's no permission about this table or this user is OP
            if (querySplitByFrom[0].contains("*")) {
                selectedColumnList.clear();
                database2Table2ColumnMap.get(databaseName).get(tableName).forEach(columnMetaData -> selectedColumnList.add(columnMetaData.getColumnName()));
            }
            List<String> columnNamesWithNoPermission =
                    selectedColumnList.stream().filter(columnName0 -> !permissions4ThisDatabase.getOrDefault(tableName, new HashMap<>()).getOrDefault(columnName0, new Boolean[]{true, false})[0]).toList();
            selectedColumnList.removeAll(columnNamesWithNoPermission);
        }
        query = new StringBuilder("select ");//produce the query command to be performed
        if (!selectedColumnList.isEmpty()) query.append(selectedColumnList.get(0));
        for (int i = 1; i < selectedColumnList.size(); i++) {
            query.append(",").append(selectedColumnList.get(i));
        }
        query.append(" from ").append(querySplitByFrom[1]);
        ResultSet resultSet = finalStatement.executeQuery(query.toString());//do query from database
        ArrayList<String> columnNames = new ArrayList<>();
        ArrayList<Object[]> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }
        while (resultSet.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = resultSet.getObject(i);
            }
            rows.add(row);
        }
        ctx2packetToBeSentMap.get(ctx).add(new SQueryReplyPacket(databaseName, tableName, columnNames, rows));
        finalStatement.close();
    }

    private void processLoginPacket(ChannelHandlerContext ctx, CLoginPacket loginPacket) {
        try {
            loadMetaDataFromConnection();
            User user = loginPacket.getUser();
            final Statement finalStatement = connection2UsersDB.createStatement();
            SLoginPacket sLoginPacket;
            User user1 = SqlUtil4Login.getUserByUsername(serverConfig.getTheUsersTableName(), user.getUsername(), finalStatement);
            if (user1 != null) {
                uuid2userMap.put(user1.getUuid(), user1);
                if (user.getUuid() == user1.getUuid()) {
                    sLoginPacket = new SLoginPacket(user1, new HashMap<>(), "reconnect success", "", "");
                } else {
                    if (user1.getPassword().equals(user.getPassword())) {
                        sLoginPacket = new SLoginPacket(user1, database2Table2ColumnMap, "success", serverConfig.getTheUsersTableName(), serverConfig.getTheUsersTableName());
                    } else sLoginPacket = new SLoginPacket(user, new HashMap<>(), "wrong password", "", "");
                }
            } else sLoginPacket = new SLoginPacket(user, new HashMap<>(), "no such user", "", "");


            SLoginPacket finalSLoginPacket = sLoginPacket;
            ctx2packetToBeSentMap.get(ctx).add(finalSLoginPacket);
            System.out.println("登录请求处理完毕");

            finalStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMetaDataFromConnection() {
        HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> tempDatabase2Table2ColumnMap = new HashMap<>();
        databaseName2connectionMap.forEach((databaseName, connection) -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet tableMetadatas = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                List<String> tableNameBlackList = serverConfig.getTheTargetDatabaseNameBlackList();
                while (tableMetadatas.next()) {
                    String tableName = tableMetadatas.getString("TABLE_NAME").toLowerCase();
                    if (tableNameBlackList.contains(tableName)) continue;
                    ArrayList<String> primaryKeyList = new ArrayList<>();
                    ArrayList<ColumnMetaData> columnList = tempDatabase2Table2ColumnMap.computeIfAbsent(databaseName.toLowerCase(), k -> new HashMap<>()).computeIfAbsent(tableName, k -> new ArrayList<>());
                    ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);
                    while (primaryKeys.next()) primaryKeyList.add(primaryKeys.getString("COLUMN_NAME").toLowerCase());
                    ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                    int i = 1;
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME").toLowerCase();
                        String columnType = columns.getString("TYPE_NAME").toLowerCase();
                        boolean isAutoIncrement = columns.getBoolean("IS_AUTOINCREMENT");
                        boolean isNullable = "YES".equalsIgnoreCase(columns.getString("IS_NULLABLE"));
                        boolean autoIncrement = columns.getMetaData().isAutoIncrement(i);
                        if (autoIncrement) System.out.println(columnName + " is autoIncrement");
                        columnList.add(new ColumnMetaData(columnName, columnType, primaryKeyList.contains(columnName), isAutoIncrement, isNullable));
                        ++i;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        database2Table2ColumnMap = tempDatabase2Table2ColumnMap;
    }
}