package com.prohect.sqlFrontend.main;

import com.prohect.sqlFrontend.ClientHandlerAdapter;
import com.prohect.sqlFrontend.MainUi;
import com.prohect.sqlFrontend.main.newRow.InsertNewRowLogic;
import com.prohect.sqlFrontendCommon.ColumnMetaData;
import com.prohect.sqlFrontendCommon.Util;
import com.prohect.sqlFrontendCommon.packet.CAlterPacket;
import com.prohect.sqlFrontendCommon.packet.CDeletePacket;
import com.prohect.sqlFrontendCommon.packet.CQueryPacket;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class MainLogic implements Initializable {
    private static final FXMLLoader insertFXMLLoader = new FXMLLoader(MainUi.class.getResource("insert-view.fxml"));
    private static final FXMLLoader insertNewColumnFXMLLoader = new FXMLLoader(MainUi.class.getResource("newColumn-view.fxml"));
    public static Stage stage4InsertNewRowsWindow;
    private Scene scene4InsertNewRowsScene;
    private Stage stage4InsertNewColumnWindow;
    private Scene scene4InsertNewColumnScene;
    private TextInputDialog textInputDialog4newTableName;
    @FXML
    private ChoiceBox<String> databaseChoiceBox;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<Object[]> tableView;
    @FXML
    private MenuItem setThisColumnCouldInspectCouldChangePermissionAsDefaultMenuItem;
    @FXML
    private MenuItem setThisColumnCouldInspectNoChangePermissionAsDefaultMenuItem;
    @FXML
    private MenuItem setThisColumnNoInspectNoChangePermissionAsDefaultMenuItem;
    @FXML
    private ChoiceBox<String> tableChoiceBox;
    private TableColumn<?, ?> selectedColumn;
    private int selectedRowIndex;

    public MainLogic() {
        Main.mainLogic = this;
    }

    /**
     * need at least two args, so is not (String db, String... commands),
     * not allowing sendSqlAlterCommands2targetDB(String db) which do nothing.
     */
    public static void sendSqlAlterCommands2targetDB(String db, String cmd, String... commands) {
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), cmd, db));
        for (String command : commands)
            Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), command, db));
    }

    public void tableColumnUpdate() {
        ObservableList<TableColumn<Object[], ?>> currentColumns = Main.mainLogic.getTableView().getColumns();
        Main.mainLogic.getTableView().setItems(FXCollections.observableArrayList());
        currentColumns.clear();
        String db = Main.mainLogic.getCurrentDataBaseName();
        String tb = Main.mainLogic.getCurrentTableName();
        ObservableList<TableColumn<Object[], ?>> c = Main.db2tb2tableColumn.get(db).get(tb);
        if (c != null) currentColumns.addAll(c);
        else Main.logger.log("mainUI.tableChoiceBox.valueProperty().Listener(): c = null ", "db = ", db, " tb = ", tb);
        Main.mainLogic.getTableView().setItems(Main.db2tb2items.get(db).get(tb));
    }

    public String getCurrentDataBaseName() {
        return getDatabaseChoiceBox().getValue();
    }

    public String getCurrentTableName() {
        return getTableChoiceBox().getValue();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            try {
                //for this UI
                getInfoLabel().textProperty().addListener((_, oldValue, newValue) -> {
                    if (newValue != null && newValue.equals(oldValue)) return;
                    new Transition() {
                        {
                            setCycleDuration(Duration.millis(5000));
                            setCycleCount(1);
                            setAutoReverse(false);
                        }

                        @Override
                        protected void interpolate(double frac) {
                            infoLabel.textFillProperty().setValue(Color.color(1f - frac, 0f, 0f));
                        }
                    }.playFromStart();
                });

                //load insertNewRows UI
                scene4InsertNewRowsScene = new Scene(insertFXMLLoader.load(), 640, 400);
                stage4InsertNewRowsWindow = new Stage();
                stage4InsertNewRowsWindow.setTitle("Insert New Row");
                stage4InsertNewRowsWindow.setScene(scene4InsertNewRowsScene);
                stage4InsertNewRowsWindow.initOwner(MainUi.getWindow());
                stage4InsertNewRowsWindow.initModality(Modality.WINDOW_MODAL);
                stage4InsertNewRowsWindow.setMinWidth(520);
                stage4InsertNewRowsWindow.setMinHeight(260);
                Main.insertNewRowLogic.getTheInsertTableView().setEditable(true);
                stage4InsertNewRowsWindow.setOnCloseRequest(_ -> {
                    if (Main.insertNewRowLogic.isNeedUpdateMainTable()) {
                        onCustomQueryButtonClicked();
                        Main.insertNewRowLogic.setNeedUpdateMainTable(false);
                    }
                });

                //load create newColumn UI
                scene4InsertNewColumnScene = new Scene(insertNewColumnFXMLLoader.load(), 359, 127);
                stage4InsertNewColumnWindow = new Stage();
                stage4InsertNewColumnWindow.setTitle("Alter New Column");
                stage4InsertNewColumnWindow.setScene(scene4InsertNewColumnScene);
                stage4InsertNewColumnWindow.setResizable(false);
                Main.insertNewColumnLogic.getNotNullCheckBox().selectedProperty().addListener((_, _, newValue) -> {
                    if (!newValue) Main.insertNewColumnLogic.getHasDefaultCheckBox().selectedProperty().set(false);
                });
                Main.insertNewColumnLogic.getHasDefaultCheckBox().selectedProperty().addListener((_, _, newValue) -> {
                    if (newValue) Main.insertNewColumnLogic.getNotNullCheckBox().selectedProperty().set(true);
                });
                ObservableList<String> list = FXCollections.observableArrayList(List.of("int", "bigint", "decimal(38, 15)", "float(8)", "REAL", "nchar(20)", "varchar(50)", "nvarchar(max)", "bit", "money", "date", "time", "datetime2", "datetimeOffset"));
                Main.insertNewColumnLogic.getColumnTypeChoiceBox().setItems(list);
                stage4InsertNewColumnWindow.setOnCloseRequest(_ -> {
                    if (Main.insertNewColumnLogic.isNeedUpdateMainTable()) {
                        onCustomQueryButtonClicked();
                        Main.insertNewColumnLogic.setNeedUpdateMainTable(false);
                    }
                });

                //load create newTable UI
                textInputDialog4newTableName = new TextInputDialog("表1");
                textInputDialog4newTableName.setTitle("创建新表");
                textInputDialog4newTableName.setHeaderText("请输入要创建的表的名称:");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public ChoiceBox<String> getTableChoiceBox() {
        return tableChoiceBox;
    }

    public TableView<Object[]> getTableView() {
        return tableView;
    }

    public Label getInfoLabel() {
        return infoLabel;
    }

    public ChoiceBox<String> getDatabaseChoiceBox() {
        return databaseChoiceBox;
    }

    @FXML
    void insertNewColumnMenuItemOnAction() {
        if (getTableView().getColumns().isEmpty()) {
            infoLabel.setText("请先选择表并查询以获取列的元数据");
            return;
        }
        stage4InsertNewColumnWindow.show();
    }

    @FXML
    public void insertNewRowMenuItemOnAction() {
        try {
            if (getTableView().getColumns().isEmpty()) {
                infoLabel.setText("请先选择表并查询以获取列的元数据");
                return;
            }
            if (!(getCurrentTableName().equals(InsertNewRowLogic.tableName) && getCurrentDataBaseName().equals(InsertNewRowLogic.databaseName))) {//当表名和数据库名变化时更新
                updateColumnMetaDataOfInsertNewRowTable();
            }
            InsertNewRowLogic.databaseName = getCurrentDataBaseName();
            InsertNewRowLogic.tableName = getCurrentTableName();
            stage4InsertNewRowsWindow.show();
        } catch (RuntimeException e) {
            Main.logger.log(e);
            Main.mainLogic.getInfoLabel().setText("不支持的操作：向本应用的users表手动中添加行");
        }
    }

    public void updateColumnMetaDataOfInsertNewRowTable() {
        Main.insertNewRowLogic.getTheInsertTableView().setItems(FXCollections.observableArrayList());
        ObservableList<TableColumn<Object[], ?>> columnObservableList = Main.insertNewRowLogic.getTheInsertTableView().getColumns();
        columnObservableList.clear();
        ArrayList<ColumnMetaData> columnMetaDataList = Main.db2tb2columnMD.get(getCurrentDataBaseName()).get(getCurrentTableName());
        Main.insertNewRowLogic.setHasIdentifier(false);
        Main.insertNewRowLogic.setIdentifierIndex(-1);
        for (int i = 0; i < columnMetaDataList.size(); i++) {
            ColumnMetaData columnMetaData = columnMetaDataList.get(i);
            String columnName = columnMetaData.getColumnName();
            TableColumn<Object[], Object> tableColumn = ClientHandlerAdapter.getTableColumn(columnName, i);
            columnObservableList.add(tableColumn);
            if (columnMetaData.isAutoIncrement()) {
                Main.insertNewRowLogic.setHasIdentifier(true);
                Main.insertNewRowLogic.setIdentifierIndex(i);
                continue;
            }
            ClientHandlerAdapter.setCellFactory(tableColumn);
            tableColumn.setOnEditCommit(event -> {
                // 直接更新数据，点击提交按钮时再处理
                int targetRowIndex = event.getTablePosition().getRow();
                int targetColumnIndex = event.getTablePosition().getColumn();
                Object[] row = event.getTableView().getItems().get(targetRowIndex);
                String newValue = (String) event.getNewValue();
                row[targetColumnIndex] = newValue;
            });
        }
    }

    @FXML
    void insertNewTableMenuItemOnAction() {
        textInputDialog4newTableName.showAndWait().ifPresent(tableName -> {
            if (!Main.user.isOp()) {
                Main.mainLogic.getInfoLabel().setText("您没有足够的权限");
                return;
            }
            if (tableName.contains("_")) {
                Main.mainLogic.getInfoLabel().setText("表名不能包含下划线_!");
                return;
            }
            String cmd;
            cmd = "CREATE TABLE " + tableName + " (" + "ID INT IDENTITY(1,1) NOT NULL, " + "PRIMARY KEY (ID))";
            sendSqlAlterCommands2targetDB(Main.mainLogic.getCurrentDataBaseName(), cmd);
        });
    }

    @FXML
    @SuppressWarnings("all")
    void mainTableOnMouseClicked() {
        selectedRowIndex = getTableView().getSelectionModel().getSelectedIndex();
        ObservableList<TablePosition> selectedCells = getTableView().getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) return;
        TablePosition position = selectedCells.getFirst();
        selectedColumn = position.getTableColumn();
        if (Main.user.isOp()) {
            setThisColumnNoInspectNoChangePermissionAsDefaultMenuItem.setVisible(true);
            setThisColumnCouldInspectNoChangePermissionAsDefaultMenuItem.setVisible(true);
            setThisColumnCouldInspectCouldChangePermissionAsDefaultMenuItem.setVisible(true);
        }
    }

    @FXML
    void onDragDropped(DragEvent event) {
        insertNewRowMenuItemOnAction();
        Main.insertNewRowLogic.onDragDropped(event);
    }

    @FXML
    void onDragOver(DragEvent event) {
        Main.insertNewRowLogic.onDragOver(event);
    }

    @FXML
    void deleteRowMenuItemOnAction() {
        if (!Main.user.isOp()) {
            List<ColumnMetaData> columnMetaDataList = Main.db2tb2columnMD.get(getCurrentDataBaseName()).get(getCurrentTableName());
            HashMap<String, Boolean[]> column2permissions = Main.user.getPermissions().get(getCurrentDataBaseName()).get(getCurrentTableName());
            for (ColumnMetaData columnMetaData : columnMetaDataList) {
                if (column2permissions.containsKey(columnMetaData.getColumnName())) {
                    if (column2permissions.get(columnMetaData.getColumnName())[1]) {
                        infoLabel.setText("您没有足够的写入权限这么做！");
                        return;
                    }
                }
            }
        }
        Object o = tableView.getItems().get(selectedRowIndex)[0];
        assert o != null;//第一个一般是主键，断言不为null
        StringBuilder cmd = new StringBuilder("DELETE FROM " + getCurrentTableName() + " WHERE [" + tableView.getColumns().getFirst().getText() + "] = " + Util.convert2SqlServerContextString(o));
        for (int i = 1; i < tableView.getColumns().size(); i++) {
            Object object = tableView.getItems().get(selectedRowIndex)[i];
            if (object == null) continue;
            cmd.append(" AND [").append(tableView.getColumns().get(i).getText()).append("] = ").append(Util.convert2SqlServerContextString(object));
        }
        CDeletePacket cDeletePacket = new CDeletePacket(Main.user.getUuid(), cmd.toString(), getCurrentDataBaseName());
        Main.packetID2DeletedValueMap.put(cDeletePacket.getId(), tableView.getItems().get(selectedRowIndex));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(cDeletePacket);
    }

    private boolean cantModifyPermission(String columnName) {
        if (getCurrentDataBaseName().equals(Main.clientConfig.getTheUsersDatabaseName())) {
            if (getCurrentTableName().equals(Main.clientConfig.getTheUsersTableName())) {
                getInfoLabel().setText("不能为本软件的用户表设定权限,因为它只对OP可见");
                return true;
            }
        } else {
            Optional<ColumnMetaData> columnMetaData = Main.db2tb2columnMD.get(getCurrentDataBaseName()).get(getCurrentTableName()).stream().filter(c -> c.getColumnName().equals(columnName)).findFirst();
            if (columnMetaData.isPresent()) {
                if (columnMetaData.get().isAutoIncrement()) {
                    getInfoLabel().setText("不能为标识列设定权限, 因为它由sqlServer自动生成管理");
                    return true;
                }
            }
        }
        if (Main.user.getPermissions().getOrDefault(getCurrentDataBaseName(), new HashMap<>()).getOrDefault(getCurrentTableName(), new HashMap<>()).get(columnName) != null) {
            getInfoLabel().setText("不能为标识列设定权限, 因为已经设置过");
            return true;
        } else return false;
    }

    @FXML
    void setThisColumnCouldInspectCouldChangePermissionAsDefault() {
        String columnName = selectedColumn.getText().toLowerCase();
        if (cantModifyPermission(columnName)) return;
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", Util.permissionColumnNameEncode(getCurrentDataBaseName(), getCurrentTableName(), columnName, false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", Util.permissionColumnNameEncode(getCurrentDataBaseName(), getCurrentTableName(), columnName, true));
        sendSqlAlterCommands2targetDB(Main.clientConfig.getTheUsersDatabaseName(), sql1, sql2);
    }

    @FXML
    void setThisColumnCouldInspectNoChangePermissionAsDefault() {
        String columnName = selectedColumn.getText().toLowerCase();
        if (cantModifyPermission(columnName)) return;
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", Util.permissionColumnNameEncode(getCurrentDataBaseName(), getCurrentTableName(), columnName, false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", Util.permissionColumnNameEncode(getCurrentDataBaseName(), getCurrentTableName(), columnName, true));
        sendSqlAlterCommands2targetDB(Main.clientConfig.getTheUsersDatabaseName(), sql1, sql2);
    }

    @FXML
    void setThisColumnNoInspectNoChangePermissionAsDefault() {
        String columnName = selectedColumn.getText().toLowerCase();
        if (cantModifyPermission(columnName)) return;
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", Util.permissionColumnNameEncode(getCurrentDataBaseName(), getCurrentTableName(), columnName, false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", Util.permissionColumnNameEncode(getCurrentDataBaseName(), getCurrentTableName(), columnName, true));
        sendSqlAlterCommands2targetDB(Main.clientConfig.getTheUsersDatabaseName(), sql1, sql2);
    }

    @FXML
    public void onCustomQueryButtonClicked() {//removed args: MouseEvent event
        try {
            ChoiceBox<String> choiceBox = Main.mainLogic.getDatabaseChoiceBox();
            String databaseName = choiceBox.getValue() == null ? choiceBox.getItems().getFirst() : choiceBox.getValue();
            CQueryPacket cQueryPacket = new CQueryPacket(Main.user.getUuid(), "select * from " + getCurrentTableName(), databaseName);
            Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(cQueryPacket);
        } catch (Exception e) {
            Main.logger.log(e);
        }
    }
}
