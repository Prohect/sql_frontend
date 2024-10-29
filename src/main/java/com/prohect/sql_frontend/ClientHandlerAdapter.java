package com.prohect.mysql_frontend;

import com.prohect.mysql_frontend.common.*;
import com.prohect.mysql_frontend.common.packet.*;
import com.prohect.mysql_frontend.login.ClientConfig;
import com.prohect.mysql_frontend.login.LoginLogic;
import com.prohect.mysql_frontend.main.Main;
import com.prohect.mysql_frontend.main.MainLogic;
import com.prohect.mysql_frontend.main.UpdateOfCellOfTable;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientHandlerAdapter extends ChannelInboundHandlerAdapter {

    String host;
    int port;
    Bootstrap b;
    /**
     * should be final once created, don't replace it with new one
     */
    PacketDecodeCell packetDecodeCell;
    EventLoopGroup workerGroup;
    LinkedBlockingQueue<Packet> packets;
    ScheduledFuture<?> packetsEncoder;
    Future<?> packetDecoderFuture;

    public ClientHandlerAdapter(String host, int port, Bootstrap b, EventLoopGroup workerGroup) {
        this.host = host;
        this.port = port;
        this.b = b;
        this.workerGroup = workerGroup;
        this.packets = new LinkedBlockingQueue<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Main.ctx = ctx;
        packetsEncoder = CommonUtil.encoderRegister(workerGroup, packets, ctx);
        Main.ctx2packetsMap.put(ctx, packets);
        System.out.println("执行登录操作");
        //Json:8种基本数据类型,只有char用双引号为"a", 数字直接为1.2, Boolean为true | false, 引用类型字符串为"string..."
//        byte[] bytes = JSON.toJSONBytes(new SInfoPacket("}[]{0}[]{"));//result: stringBuilder = {"id":-212632573705707520,"info":"}[]{0}[]{","prefix":"SInfoPacket\\"}
//        byte[] bytes = JSON.toJSONBytes(new TestJsonEncode('g'));//result:  stringBuilder = {"aChar":"g"}

        packets.add((new CLoginPacket(Main.user == null ? new User(Main.loginLogic.getUsernameField().getText(), Main.loginLogic.getPasswordField().getText(), 0L) : Main.user)));
        workerGroup.execute(() -> {
            try {
                while (!workerGroup.isShuttingDown()) {
                    for (; ; ) {
                        Packet packet = Main.packetReceivedQueue.poll();
                        if (packet == null) break;
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
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (packetDecodeCell == null) packetDecodeCell = new PacketDecodeCell(ctx.alloc().buffer(1048576));//1MegaBytes
        packetDecoderFuture = CommonUtil.getPackets(workerGroup, packetDecoderFuture, (ByteBuf) msg, packetDecodeCell, Main.packetReceivedQueue);
    }

    public void reconnect() {
        b.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                future.channel().eventLoop().schedule(this::reconnect, 5, TimeUnit.SECONDS);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        packetsEncoder.cancel(true);

        Main.ctx2packetsMap.remove(ctx);
        reconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        packetsEncoder.cancel(true);
        LoginLogic.logged.set(false);
        if (cause instanceof SocketException) {
            Platform.runLater(() -> Main.mainLogic.getInfoLabel().setText("Connection reset, try reconnect"));
        }
        Main.ctx2packetsMap.remove(ctx);
        reconnect();
    }

    private void processInsertPacket(SInsertPacket sInsertPacket) {
        long id = sInsertPacket.getTheID();
        Object[] objects = Main.packetID2insertedValueMap.get(id);
        Main.insertLogic.getTheInsertTableView().getItems().remove(objects);
    }

    private void processDeletePacket(SDeletePacket sDeletePacket) {
        long id = sDeletePacket.getTheID();
        Object[] object = Main.packetID2DeletedValueMap.get(id);
        Main.mainLogic.getMainTable().getItems().remove(object);
    }


    private void processSQueryReplyPacket(SQueryReplyPacket sQueryReplyPacket) {
        Platform.runLater(() -> {
            try {
                String databaseName = sQueryReplyPacket.getDatabaseName();
                String tableName = sQueryReplyPacket.getTableName();
                ArrayList<String> columnNames = sQueryReplyPacket.getColumnNames();
                ObservableList<Object[]> data = FXCollections.observableArrayList(sQueryReplyPacket.getRows());

                Main.mainLogic.setDataBase4tableView(databaseName.toLowerCase());
                Main.mainLogic.setTableName4tableView(tableName.toLowerCase());

                int columnCount = columnNames.size();
                TableView<Object[]> tableView = Main.mainLogic.getMainTable();
                tableView.getColumns().clear();
                HashMap<String, HashMap<String, Boolean[]>> permission4thisDatabase = Main.user.getPermissions().get(databaseName);
                HashMap<String, Boolean[]> permission4thisTable = permission4thisDatabase == null ? null : permission4thisDatabase.get(tableName);
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    TableColumn<Object[], Object> column = CommonUtil.getTableColumn(columnNames.get(columnIndex), columnIndex);
                    if (Main.user.isOP() || (permission4thisTable != null && permission4thisTable.getOrDefault(columnNames.get(columnIndex), new Boolean[]{true, false})[1])) {
                        CommonUtil.setCellFactory(column);
                        column.setOnEditCommit(event -> {
                            // 更新数据,同步提交到服务器
                            int targetRowIndex = event.getTablePosition().getRow();
                            int targetColumnIndex = event.getTablePosition().getColumn();
                            Object[] row = event.getTableView().getItems().get(targetRowIndex);
                            String newValue = (String) event.getNewValue();
                            Object o1 = row[(targetColumnIndex == 0) ? 1 : 0];
                            ObservableList<TableColumn<Object[], ?>> columns = Main.mainLogic.getMainTable().getColumns();
                            StringBuilder condition = new StringBuilder("UPDATE " + Main.mainLogic.getTableName4tableView() + " SET " + columns.get(targetColumnIndex).getText() + " = " + (CommonUtil.isNumber(newValue) ? newValue : "'" + newValue + "'") + " WHERE " + (columns.get(targetColumnIndex == 0 ? 1 : 0)).getText() + " = " + CommonUtil.convert2SqlServerContextString(o1));
                            for (int i = 1; i < row.length; i++) {
                                if (i == targetColumnIndex) continue;
                                String convert2SqlServerContextString = CommonUtil.convert2SqlServerContextString(row[i]);
                                String columnName = columns.get(i).getText();
                                if (convert2SqlServerContextString == null || convert2SqlServerContextString.isEmpty()) {
                                    continue;
                                }
                                condition.append(" AND ").append(columnName).append(" = ").append(convert2SqlServerContextString);
                            }
                            CUpdatePacket cUpdatePacket = new CUpdatePacket(Main.user.getUuid(), condition.toString(), Main.mainLogic.getDataBase4tableView());
                            Main.packetID2updatedValueMap.put(cUpdatePacket.getId(), new UpdateOfCellOfTable(targetRowIndex, targetColumnIndex, newValue));
                            Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(cUpdatePacket);
                        });
                    }
                    tableView.getColumns().add(column);
                }
                tableView.setEditable(true);
                tableView.setItems(data);
                if (MainLogic.stage4InsertNewRowsWindow != null && MainLogic.stage4InsertNewRowsWindow.isShowing())
                    Platform.runLater(() -> Main.mainLogic.insertNewRowMenuItemOnAction(new ActionEvent()));
            } catch (Exception e) {
                Main.mainLogic.getInfoLabel().setText(e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private void processSInfoPacket(SInfoPacket sInfoPacket) {
        Platform.runLater(() -> Main.mainLogic.getInfoLabel().setText(sInfoPacket.getInfo()));
    }

    private void processSUpdatePacket(SUpdatePacket sUpdatePacket) {
        long id = sUpdatePacket.getTheID();
        UpdateOfCellOfTable update = Main.packetID2updatedValueMap.get(id);
        Platform.runLater(() -> Main.mainLogic.getMainTable().getItems().get(update.getTargetRowIndex())[update.getTargetColumnIndex()] = update.getNewValue());
    }

    private void processSLoginPacket(SLoginPacket sLoginPacket) {
        String info = sLoginPacket.getInfo();
        HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap = sLoginPacket.getDb2table2columnMap();
        Platform.runLater(() -> Main.loginLogic.getLoginInfo().setText(info));
        if (info.equals("success") && !LoginLogic.logged.get()) {
            Main.user = sLoginPacket.getUser();
            Main.clientConfig.setTheUsersDatabaseName(sLoginPacket.getTheUsersDatabaseName());
            Main.clientConfig.setTheUsersTableName(sLoginPacket.getTheUsersTableName());
            ClientConfig.saveConfig(Main.clientConfig);
            Main.db2table2columnMap = db2table2columnMap;
            Platform.runLater(() -> {
                ObservableList<String> databaseList = FXCollections.observableArrayList();
                Main.db2table2columnMap.forEach((db, table2column) -> databaseList.add(db));
                String databaseListFirst = databaseList.getFirst();
                ChoiceBox<String> databaseSourceChoiceBox = Main.mainLogic.getDatabaseSourceChoiceBox();
                databaseSourceChoiceBox.setItems(databaseList);
                if (databaseListFirst != null) {
                    databaseSourceChoiceBox.setValue(databaseListFirst);
                }

                ObservableList<String> tableList = FXCollections.observableArrayList();
                if (databaseListFirst != null) {
                    Main.db2table2columnMap.get(databaseListFirst).forEach((table2column, column) -> tableList.add(table2column));
                }
                String tableListFirst = tableList.getFirst();
                Main.mainLogic.getTableChoiceBox().setItems(tableList);
                if (tableListFirst != null) {
                    Main.mainLogic.getTableChoiceBox().setValue(tableListFirst);
                }

                databaseSourceChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    ObservableList<String> list = FXCollections.observableArrayList();
                    HashMap<String, ArrayList<ColumnMetaData>> table2columnsMap = Main.db2table2columnMap.get(databaseSourceChoiceBox.getValue());
                    if (table2columnsMap != null) table2columnsMap.forEach((k, v) -> list.add(k));
                    Main.mainLogic.getTableChoiceBox().setItems(list);
                    if (newValue.equalsIgnoreCase(Main.clientConfig.getTheUsersDatabaseName()))
                        list.add(Main.clientConfig.getTheUsersTableName());
                    if (!list.isEmpty()) Main.mainLogic.getTableChoiceBox().setValue(list.getFirst());
                });

                if (Main.user.isOP())
                    databaseSourceChoiceBox.getItems().add(Main.clientConfig.getTheUsersDatabaseName());
                Main.mainLogic.getInfoLabel().setText("连接成功");
                LoginLogic.logged.set(true);
                LoginUi.getWindow().setScene(LoginUi.mainScene);
            });
        } else if (sLoginPacket.getInfo().equals("reconnect success")) {
            Main.user.setPermissions(sLoginPacket.getUser().getPermissions());
            Main.db2table2columnMap = db2table2columnMap;
            LoginLogic.logged.set(true);
        }
    }
}
