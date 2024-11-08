package com.prohect.sql_frontend;


import com.prohect.sql_frontend.main.Main;
import com.prohect.sql_frontend.main.MainLogic;
import com.prohect.sql_frontend.main.UpdateOfCellOfTable;
import com.prohect.sql_frontend.main.login.ClientConfig;
import com.prohect.sql_frontend.main.login.LoginLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.User;
import com.prohect.sql_frontend_common.Util;
import com.prohect.sql_frontend_common.packet.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static com.prohect.sql_frontend_common.CollectionUtil.merge;
import static com.prohect.sql_frontend_common.CollectionUtil.structureCloneAndMerge;

@ChannelHandler.Sharable
public class ClientHandlerAdapter extends ChannelInboundHandlerAdapter {

    private final AtomicBoolean alreadyOnReconnecting = new AtomicBoolean(false);

    String host;
    int port;
    Bootstrap b;
    EventLoopGroup workerGroup;
    /**
     * should be final once created, don't replace it with new one
     */
    Map<Channel, ByteBuf> channel2in;
    Map<Channel, ReentrantLock> channel2lockOfIn;
    Map<Channel, ScheduledFuture<?>> channel2packetsEncoder;
    Map<Channel, Future<?>> channel2packetDecoderFuture;

    public ClientHandlerAdapter(String host, int port, Bootstrap b, EventLoopGroup workerGroup) {
        this.host = host;
        this.port = port;
        this.b = b;
        this.workerGroup = workerGroup;
        channel2in = new ConcurrentHashMap<>();
        channel2lockOfIn = new ConcurrentHashMap<>();
        channel2packetsEncoder = new ConcurrentHashMap<>();
        channel2packetDecoderFuture = new ConcurrentHashMap<>();
    }

    public static void setCellFactory(TableColumn<Object[], Object> column) {
        column.setCellFactory(TextFieldTableCell.<Object[], Object>forTableColumn(new StringConverter<Object>() {

            @Override
            public String toString(Object object) {
                if (object == null) {
                    return "";
                }
                return object.toString();
            }

            @Override
            public Object fromString(String string) {
                return string;
            }
        }));
    }

    public static TableColumn<Object[], Object> getTableColumn(String columnName, int columnIndex) {
        TableColumn<Object[], Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> {
            return cellData.getValue()[columnIndex] != null ? new SimpleObjectProperty<>(cellData.getValue()[columnIndex]) : new SimpleObjectProperty<>(null);
        });
        return column;
    }

    public static void updateTableChoiceBox(String oldValue, String newValue) {
        if (newValue != null && newValue.equals(oldValue)) return;
        ObservableList<String> list = FXCollections.observableArrayList();
        HashMap<String, ArrayList<ColumnMetaData>> tb2column = Main.db2tb2columnMD.get(Main.mainLogic.getDatabaseChoiceBox().getValue());
        if (tb2column != null) tb2column.forEach((tableName, _) -> list.add(tableName));
        if (Main.clientConfig.getTheUsersDatabaseName().equalsIgnoreCase(newValue))
            list.add(Main.clientConfig.getTheUsersTableName());
        Main.mainLogic.getTableChoiceBox().setItems(list);
        if (!list.isEmpty()) Main.mainLogic.getTableChoiceBox().setValue(list.getFirst());
    }

    private static void initEditFactory(TableColumn<Object[], Object> column) {
        ClientHandlerAdapter.setCellFactory(column);
        column.setOnEditCommit(event -> {
            // 更新数据,同步提交到服务器
            int targetRowIndex = event.getTablePosition().getRow();
            int targetColumnIndex = event.getTablePosition().getColumn();
            Object[] row = event.getTableView().getItems().get(targetRowIndex);
            String newValue = (String) event.getNewValue();
            Object o1 = row[(targetColumnIndex == 0) ? 1 : 0];
            ObservableList<TableColumn<Object[], ?>> columns = Main.mainLogic.getTableView().getColumns();
            StringBuilder condition = new StringBuilder("UPDATE " + Main.mainLogic.getTableName4tableView() + " SET [" + columns.get(targetColumnIndex).getText() + "] = " + (Util.isNumber(newValue) ? newValue : "'" + newValue + "'") + " WHERE [" + (columns.get(targetColumnIndex == 0 ? 1 : 0)).getText() + "] = " + Util.convert2SqlServerContextString(o1));
            for (int i = 1; i < row.length; i++) {
                if (i == targetColumnIndex) continue;
                if (row[i] == null) continue;
                String convert2SqlServerContextString = Util.convert2SqlServerContextString(row[i]);
                String columnName = columns.get(i).getText();
                if (convert2SqlServerContextString == null || convert2SqlServerContextString.isEmpty()) {
                    continue;
                }
                condition.append(" AND [").append(columnName).append("] = ").append(convert2SqlServerContextString);
            }
            CUpdatePacket cUpdatePacket = new CUpdatePacket(Main.user.getUuid(), condition.toString(), Main.mainLogic.getDataBaseName4tableView());
            Main.packetID2updatedValueMap.put(cUpdatePacket.getId(), new UpdateOfCellOfTable(targetRowIndex, targetColumnIndex, newValue));
            Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(cUpdatePacket);
        });
    }

    private static void tableColumnUpdate() {
        ObservableList<TableColumn<Object[], ?>> currentColumns = Main.mainLogic.getTableView().getColumns();
        Main.mainLogic.getTableView().setItems(FXCollections.observableArrayList());
        currentColumns.clear();
        String db = Main.mainLogic.getDatabaseChoiceBox().getValue();
        String tb = Main.mainLogic.getTableChoiceBox().getValue();
        ArrayList<TableColumn<Object[], ?>> c = Main.db2tb2tableColumn.get(db).get(tb);
        if (c != null) currentColumns.addAll(c);
        else
            Main.logger.log("mainUI.tableChoiceBox.valueProperty().Listener(): c = null ", "db = ", db, " tb = ", tb);
        Main.mainLogic.getTableView().setItems(Main.db2tb2items.get(db).get(tb));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Main.ctx = ctx;
        LinkedBlockingQueue<Packet> packets = new LinkedBlockingQueue<>();
        channel2packetsEncoder.put(ctx.channel(), Util.encoderRegister(workerGroup, ctx, packets, 25));
        Main.channel2packetsMap.put(ctx.channel(), packets);
        //Json:8种基本数据类型,只有char用双引号为"a", 数字直接为1.2, Boolean为true | false, 引用类型字符串为"string..."
//        byte[] bytes = JSON.toJSONBytes(new SInfoPacket("}[]{0}[]{"));//result: stringBuilder = {"id":-212632573705707520,"info":"}[]{0}[]{","prefix":"SInfoPacket\\"}
//        byte[] bytes = JSON.toJSONBytes(new TestJsonEncode('g'));//result:  stringBuilder = {"aChar":"g"}

        packets.add((new CLoginPacket(Main.user == null ? new User(Main.loginLogic.getUsernameField().getText(), Main.loginLogic.getPasswordField().getText(), 0L) : new User(Main.user.getUsername(), Main.user.getPassword(), Main.user.getUuid()))));

        workerGroup.scheduleAtFixedRate(() -> {
            try {
                for (; ; ) {
                    Packet packet = Main.packetReceivedQueue.poll();
                    if (packet == null) break;
                    Main.logger.log("接收%s".formatted(packet));
                    switch (packet) {
                        case SQueryReplyPacket sQueryReplyPacket -> processSQueryReplyPacket(sQueryReplyPacket);
                        case SInfoPacket sInfoPacket -> processSInfoPacket(sInfoPacket);
                        case SLoginPacket sLoginPacket -> processSLoginPacket(sLoginPacket);
                        case SUpdatePacket sUpdatePacket -> processSUpdatePacket(sUpdatePacket);
                        case SInsertPacket sInsertPacket -> processInsertPacket(sInsertPacket);
                        case SDeletePacket sDeletePacket -> processDeletePacket(sDeletePacket);
                        default -> {
                        }
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        channel2packetDecoderFuture.put(ctx.channel(), Util.getPackets_concurrent(workerGroup, channel2packetDecoderFuture.get(ctx.channel()), (ByteBuf) msg, channel2lockOfIn.computeIfAbsent(ctx.channel(), _ -> new ReentrantLock()), channel2in.computeIfAbsent(ctx.channel(), _ -> ctx.alloc().buffer(1048576)), Main.packetReceivedQueue));
    }

    public void reconnect() {
        if (alreadyOnReconnecting.get()) {
            return;
        }
        alreadyOnReconnecting.set(true);
        Future<Void> connect = b.connect(host, port);
        try {
            connect.sync();
        } catch (Exception e) {
            alreadyOnReconnecting.set(false);
            workerGroup.schedule(this::reconnect, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Main.logger.log("ClientHandlerAdapter.channelUnregistered()");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Main.logger.log("ClientHandlerAdapter.channelInactive");
        try {
            channel2packetsEncoder.get(ctx.channel()).cancel(true);
            channel2packetDecoderFuture.get(ctx.channel()).cancel(true);
            channel2lockOfIn.remove(ctx.channel());
            channel2in.get(ctx.channel()).release();
            channel2in.remove(ctx.channel());
            channel2packetsEncoder.remove(ctx.channel());
            channel2packetDecoderFuture.remove(ctx.channel());
            Main.channel2packetsMap.remove(ctx.channel());
        } catch (Exception e) {//might catch nullPointer
            Main.logger.log(e);
        }

        Platform.runLater(() -> Main.mainLogic.getInfoLabel().setText("Connection reset, try reconnect"));
        alreadyOnReconnecting.set(false);
        reconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Main.logger.log(cause);
    }

    private void processInsertPacket(SInsertPacket sInsertPacket) {
        long id = sInsertPacket.getTheID();
        Object[] objects = Main.packetID2insertedValueMap.get(id);
        Main.insertNewRowLogic.getTheInsertTableView().getItems().remove(objects);
        Main.insertNewRowLogic.setNeedUpdateMainTable(true);
    }

    private void processDeletePacket(SDeletePacket sDeletePacket) {
        long id = sDeletePacket.getTheID();
        Object[] object = Main.packetID2DeletedValueMap.get(id);
        Main.mainLogic.getTableView().getItems().remove(object);
    }

    private void processSQueryReplyPacket(SQueryReplyPacket sQueryReplyPacket) {
        Platform.runLater(() -> {
            try {
                String databaseName = sQueryReplyPacket.getDatabaseName();
                String tableName = sQueryReplyPacket.getTableName();
                ArrayList<String> columnNames = sQueryReplyPacket.getColumnNames();
                ArrayList<Object[]> itemsFromPacket = sQueryReplyPacket.getRows();

                //update the tableColumns for users DB's users table since the db2tb2columnMD don't have metadata of users table
                if (Main.clientConfig.getTheUsersDatabaseName().equals(databaseName)) {
                    if (Main.clientConfig.getTheUsersTableName().equals(tableName)) {
                        TableView<Object[]> tableView = Main.mainLogic.getTableView();
                        tableView.getColumns().clear();
                        for (int i = 0; i < columnNames.size(); i++) {
                            TableColumn<Object[], Object> column = ClientHandlerAdapter.getTableColumn(columnNames.get(i), i);
                            initEditFactory(column);
                            tableView.getColumns().add(column);
                        }
                        ArrayList<TableColumn<Object[], ?>> tableColumns = Main.db2tb2tableColumn.get(databaseName).get(tableName);
                        tableColumns.clear();
                        tableColumns.addAll(tableView.getColumns());
                    }
                }
                ObservableList<Object[]> items = Main.db2tb2items.get(databaseName).get(tableName);
                items.clear();
                items.addAll(itemsFromPacket);
                if (MainLogic.stage4InsertNewRowsWindow != null && MainLogic.stage4InsertNewRowsWindow.isShowing())
                    Platform.runLater(() -> Main.mainLogic.insertNewRowMenuItemOnAction());
                Main.mainLogic.getInfoLabel().setText("查询成功");
            } catch (Exception e) {
                Main.mainLogic.getInfoLabel().setText(e.getMessage());
                Main.logger.log(e);
            }
        });
    }

    private void processSInfoPacket(SInfoPacket sInfoPacket) {
        Platform.runLater(() -> Main.mainLogic.getInfoLabel().setText(sInfoPacket.getInfo()));
    }

    private void processSUpdatePacket(SUpdatePacket sUpdatePacket) {
        long id = sUpdatePacket.getTheID();
        UpdateOfCellOfTable update = Main.packetID2updatedValueMap.get(id);
        int targetRowIndex = update.getTargetRowIndex();
        int targetColumnIndex = update.getTargetColumnIndex();

        TableView<Object[]> tableView = Main.mainLogic.getTableView();
        String columnName = tableView.getColumns().get(targetColumnIndex).getText();
        ArrayList<ColumnMetaData> columnMetaDataArrayList = Main.db2tb2columnMD.get(Main.mainLogic.getDataBaseName4tableView()).get(Main.mainLogic.getTableName4tableView());
        Platform.runLater(() -> {
            ObservableList<Object[]> items = tableView.getItems();
            Object[] objects = items.get(targetRowIndex);
            Object oldValue = objects[targetColumnIndex];
            Object newValue = update.getNewValue();
            if (oldValue != null) {
                String clazzName = oldValue.getClass().getSimpleName();
                Main.logger.log("oldValue.getClass().getSimpleName() = " + clazzName);
                switch (clazzName) {
                    case "Integer":
                        if (Util.isNumber((String) newValue)) newValue = Integer.parseInt((String) newValue);
                        else newValue = (int) Double.parseDouble((String) newValue);
                        break;
                    case "Long":
                        if (Util.isNumber((String) newValue)) newValue = Long.parseLong((String) newValue);
                        else newValue = (long) Double.parseDouble((String) newValue);
                        break;
                    case "Double":
                        newValue = Double.parseDouble((String) newValue);
                        break;
                    case "Float":
                        newValue = (float) Double.parseDouble((String) newValue);
                        break;
                    case "Boolean":
                        newValue = "true".equalsIgnoreCase((String) newValue) || "1".equals(newValue);
                        break;
                    default:
                        break;
                }
            }
            Object[] clone = objects.clone();
            clone[targetColumnIndex] = newValue;
            items.set(targetRowIndex, clone);
        });
    }

    private void processSLoginPacket(SLoginPacket sLoginPacket) {
        User userFromPacket = sLoginPacket.getUser();
        HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap = sLoginPacket.getDb2table2columnMap();
        SLoginPacket.Info info = sLoginPacket.getInfo();

        Platform.runLater(() -> Main.mainLogic.getInfoLabel().setText(SLoginPacket.toString(info)));
        switch (info) {
            case RS -> LoginLogic.logged.set(true);
            case S -> {
                if (!LoginLogic.logged.get()) {
                    Main.user = userFromPacket;
                    String theUsersDatabaseName = sLoginPacket.getTheUsersDatabaseName();
                    String theUsersTableName = sLoginPacket.getTheUsersTableName();

                    merge(Main.db2tb2columnMD, db2table2columnMap);

                    Main.clientConfig.setTheUsersDatabaseName(theUsersDatabaseName);
                    Main.clientConfig.setTheUsersTableName(theUsersTableName);
                    ClientConfig.saveConfig(Main.clientConfig);

                    //db2tb2permittedColumn update
                    HashMap<String, HashMap<String, ArrayList<TableColumn<Object[], ?>>>> db2tb2tcl = new HashMap<>();
                    HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> m0;//m0 -> readable
                    HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> m1;//m1 -> writable
                    HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions = userFromPacket.getPermissions();
                    if (userFromPacket.isOp()) {
                        db2table2columnMap.put(theUsersDatabaseName, new HashMap<>() {{
                            put(theUsersTableName, new ArrayList<>());
                        }});
                        Main.db2tb2items.put(theUsersDatabaseName, new HashMap<>() {{
                            put(theUsersTableName, FXCollections.observableArrayList());
                        }});
                        m0 = db2table2columnMap;
                        m1 = db2table2columnMap;
                    } else {
                        m0 = structureCloneAndMerge(db2table2columnMap);
                        m0.forEach((db, tb2cml) -> tb2cml.forEach((tb, cml) -> cml.removeAll(cml.stream().filter(cm -> !permissions.getOrDefault(db, new HashMap<>()).getOrDefault(tb, new HashMap<>()).getOrDefault(cm.getColumnName(), new Boolean[]{true, false})[0]).toList())));
                        m1 = structureCloneAndMerge(m0);
                        m1.forEach((db, tb2cml) -> tb2cml.forEach((tb, cml) -> cml.removeAll(cml.stream().filter(cm -> !permissions.getOrDefault(db, new HashMap<>()).getOrDefault(tb, new HashMap<>()).getOrDefault(cm.getColumnName(), new Boolean[]{true, false})[1]).toList())));
                    }
                    m0.forEach((db, tb2cml) -> tb2cml.forEach((tb, cml) -> {
                        for (int i = 0; i < cml.size(); i++) {
                            ColumnMetaData columnMetaData = cml.get(i);
                            TableColumn<Object[], Object> column = getTableColumn(columnMetaData.getColumnName(), i);
                            if (m1.getOrDefault(db, new HashMap<>()).getOrDefault(tb, new ArrayList<>()).stream().anyMatch(c1 -> c1.hashCode() == columnMetaData.hashCode())) {
                                if (!columnMetaData.isAutoIncrement()) {
                                    initEditFactory(column);
                                }
                            }
                            db2tb2tcl.computeIfAbsent(db, _ -> new HashMap<>()).computeIfAbsent(tb, _ -> new ArrayList<>()).add(column);
                        }
                    }));
                    merge(Main.db2tb2tableColumn, db2tb2tcl);

                    //update db2tb2items. this won't remove data when reconnecting for it's using computeIfAbsent
                    m0.forEach((db, tb2cml) -> tb2cml.forEach((tb, _) -> Main.db2tb2items.computeIfAbsent(db, _ -> new HashMap<>()).computeIfAbsent(tb, _ -> FXCollections.observableArrayList())));

                    Platform.runLater(() -> {
                        ObservableList<String> databaseList = FXCollections.observableArrayList();
                        Main.db2tb2columnMD.forEach((db, _) -> databaseList.add(db));
                        ChoiceBox<String> databaseSourceChoiceBox = Main.mainLogic.getDatabaseChoiceBox();
                        databaseSourceChoiceBox.setItems(databaseList);
                        String databaseListFirst = databaseList.getFirst();
                        ObservableList<String> tableList = FXCollections.observableArrayList();
                        String value = databaseSourceChoiceBox.getValue();
                        if (value == null || value.isEmpty()) {
                            if (databaseListFirst != null) {
                                if (databaseList.contains(Main.clientConfig.getLastDB()))
                                    databaseListFirst = Main.clientConfig.getLastDB();
                                databaseSourceChoiceBox.setValue(databaseListFirst);
                                Main.db2tb2columnMD.get(databaseListFirst).forEach((table2column, _) -> tableList.add(table2column));
                            }
                            Main.mainLogic.getTableChoiceBox().setItems(tableList);
                            String tableListFirst = tableList.getFirst();
                            if (tableListFirst != null) {
                                if (tableList.contains(Main.clientConfig.getLastTB()))
                                    tableListFirst = Main.clientConfig.getLastTB();
                                Main.mainLogic.getTableChoiceBox().setValue(tableListFirst);
                            }
                        }

                        if (Main.user.isOp())
                            databaseSourceChoiceBox.getItems().add(Main.clientConfig.getTheUsersDatabaseName());
                        Main.mainLogic.getInfoLabel().setText("连接成功");
                        LoginLogic.logged.set(true);
                        Stage window = MainUi.getWindow();
                        window.close();
                        window.setScene(MainUi.mainScene);
                        window.setMinWidth(800);
                        window.setMinHeight(400);
                        window.setWidth(Main.clientConfig.getSizeOfMainGUI()[0]);
                        window.setHeight(Main.clientConfig.getSizeOfMainGUI()[1]);
                        window.widthProperty().addListener((observable, oldValue, newValue) -> Main.clientConfig.getSizeOfMainGUI()[0] = newValue.doubleValue());
                        window.heightProperty().addListener((observable, oldValue, newValue) -> Main.clientConfig.getSizeOfMainGUI()[1] = newValue.doubleValue());
                        window.setResizable(true);
                        window.show();

                        //load stuff for this UI
                        Main.mainLogic.onCustomQueryButtonClicked();
                        tableColumnUpdate();
                        databaseSourceChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                            ClientHandlerAdapter.updateTableChoiceBox(oldValue, newValue);
                        });
                        Main.mainLogic.getTableChoiceBox().valueProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue != null && !newValue.equals(oldValue))
                                Main.mainLogic.onCustomQueryButtonClicked();
                            tableColumnUpdate();
                        });
                    });
                }
            }
            case UM -> {
                String value = Main.mainLogic.getTableChoiceBox().getValue();
                merge(Main.db2tb2columnMD, db2table2columnMap);
                Platform.runLater(() -> updateTableChoiceBox(value, value));
                Main.mainLogic.updateColumnMetaDataOfInsertNewRowTable();
            }
            case UP -> merge(Main.user.getPermissions(), userFromPacket.getPermissions());
        }
    }
}
