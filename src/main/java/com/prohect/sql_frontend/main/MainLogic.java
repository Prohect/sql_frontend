package com.prohect.sql_frontend.main;

import com.prohect.sql_frontend.ClientHandlerAdapter;
import com.prohect.sql_frontend.MainUi;
import com.prohect.sql_frontend.main.newRow.InsertNewRowLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.packet.CAlterPacket;
import com.prohect.sql_frontend_common.packet.CDeletePacket;
import com.prohect.sql_frontend_common.packet.CQueryPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private String dataBaseName4tableView;
    private String tableName4tableView;
    @FXML
    private TextField customQueryTextField;
    @FXML
    private ChoiceBox<String> databaseSourceChoiceBox;
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
    private int selectedColumnIndex;

    public MainLogic() {
        Main.mainLogic = this;
    }

    public String getDataBaseName4tableView() {
        return dataBaseName4tableView;
    }

    public void setDataBaseName4tableView(String dataBaseName4tableView) {
        this.dataBaseName4tableView = dataBaseName4tableView;
    }

    public String getTableName4tableView() {
        return tableName4tableView;
    }

    public void setTableName4tableView(String tableName4tableView) {
        this.tableName4tableView = tableName4tableView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            try {
                //load stuff for this UI
                getTableChoiceBox().valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.equals(oldValue)) onCustomQueryButtonClicked();
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
                stage4InsertNewRowsWindow.setOnCloseRequest(event -> {
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
                Main.insertNewColumnLogic.getNotNullCheckBox().selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) Main.insertNewColumnLogic.getHasDefaultCheckBox().selectedProperty().set(false);
                });
                Main.insertNewColumnLogic.getHasDefaultCheckBox().selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (newValue) Main.insertNewColumnLogic.getNotNullCheckBox().selectedProperty().set(true);
                });
                ObservableList<String> list = FXCollections.observableArrayList(List.of("int", "bigint", "decimal(38, 15)", "float(8)", "REAL", "nchar(20)", "varchar(50)", "nvarchar(max)", "bit", "money", "date", "time", "datetime2", "datetimeOffset"));
                Main.insertNewColumnLogic.getColumnTypeChoiceBox().setItems(list);
                stage4InsertNewColumnWindow.setOnCloseRequest(event -> {
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

    public ChoiceBox<String> getDatabaseSourceChoiceBox() {
        return databaseSourceChoiceBox;
    }

    public TextField getCustomQueryTextField() {
        return customQueryTextField;
    }

    @FXML
    void insertNewColumnMenuItemOnAction(ActionEvent event) {
        if (getTableView().getColumns().isEmpty()) {
            infoLabel.setText("请先选择表并查询以获取列的元数据");
            return;
        }
        stage4InsertNewColumnWindow.show();
    }

    @FXML
    public void insertNewRowMenuItemOnAction(ActionEvent event) {
        try {
            if (getTableView().getColumns().isEmpty()) {
                infoLabel.setText("请先选择表并查询以获取列的元数据");
                return;
            }
            if (!(tableName4tableView.equals(InsertNewRowLogic.tableName) && dataBaseName4tableView.equals(InsertNewRowLogic.databaseName))) {//当表名和数据库名变化时更新
                updateColumnMetaDataOfInsertNewRowTable();
            }
            InsertNewRowLogic.databaseName = dataBaseName4tableView;
            InsertNewRowLogic.tableName = tableName4tableView;
            stage4InsertNewRowsWindow.show();
        } catch (RuntimeException e) {
            Main.logger.log(e);
            Main.mainLogic.getInfoLabel().setText("不支持的操作：向本应用的users表手动中添加行");
        }
    }

    public void updateColumnMetaDataOfInsertNewRowTable() {
        Platform.runLater(() -> {
            Main.insertNewRowLogic.getTheInsertTableView().setItems(FXCollections.observableArrayList());
            ObservableList<TableColumn<Object[], ?>> columnObservableList = Main.insertNewRowLogic.getTheInsertTableView().getColumns();
            columnObservableList.clear();
            ArrayList<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(getDatabaseSourceChoiceBox().getValue()).get(getTableChoiceBox().getValue());
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
        });
    }

    @FXML
    void insertNewTableMenuItemOnAction(ActionEvent event) {
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
            Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), cmd, Main.mainLogic.getDatabaseSourceChoiceBox().getValue()));
        });
    }

    @FXML
    void mainTableOnMouseClicked(MouseEvent event) {
        selectedRowIndex = getTableView().getSelectionModel().getSelectedIndex();
        ObservableList<TablePosition> selectedCells = getTableView().getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) return;
        TablePosition position = selectedCells.get(0);
        selectedColumnIndex = position.getColumn();
        selectedColumn = position.getTableColumn();
        if (Main.user.isOp()) {
            setThisColumnNoInspectNoChangePermissionAsDefaultMenuItem.setVisible(true);
            setThisColumnCouldInspectNoChangePermissionAsDefaultMenuItem.setVisible(true);
            setThisColumnCouldInspectCouldChangePermissionAsDefaultMenuItem.setVisible(true);
        }
    }

    @FXML
    void deleteRowMenuItemOnAction(ActionEvent event) {
        if (!Main.user.isOp()) {
            List<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(dataBaseName4tableView).get(tableName4tableView);
            HashMap<String, Boolean[]> column2permissions = Main.user.getPermissions().get(dataBaseName4tableView).get(tableName4tableView);
            for (int i = 0; i < columnMetaDataList.size(); i++) {
                if (column2permissions.containsKey(columnMetaDataList.get(i).getColumnName())) {
                    if (column2permissions.get(columnMetaDataList.get(i).getColumnName())[1]) {
                        infoLabel.setText("您没有足够的写入权限这么做！");
                        return;
                    }
                }
            }
        }
        Object o = tableView.getItems().get(selectedRowIndex)[0];
        assert o != null;//第一个一般是主键，断言不为null
        StringBuilder cmd = new StringBuilder("DELETE FROM " + tableName4tableView + " WHERE [" + tableView.getColumns().getFirst().getText() + "] = " + CommonUtil.convert2SqlServerContextString(o));
        for (int i = 1; i < tableView.getColumns().size(); i++) {
            Object object = tableView.getItems().get(selectedRowIndex)[i];
            if (object == null) continue;
            cmd.append(" AND [").append(tableView.getColumns().get(i).getText()).append("] = ").append(CommonUtil.convert2SqlServerContextString(object));
        }
        CDeletePacket cDeletePacket = new CDeletePacket(Main.user.getUuid(), cmd.toString(), dataBaseName4tableView);
        Main.packetID2DeletedValueMap.put(cDeletePacket.getId(), tableView.getItems().get(selectedRowIndex));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(cDeletePacket);
    }

    @FXML
    void databaseSourceChoiceBoxOnMouseClicked(MouseEvent event) {
    }

    private boolean cantModifyPermission(String columnName) {
        if (getDataBaseName4tableView().equals(Main.clientConfig.getTheUsersDatabaseName())) {
            if (getTableName4tableView().equals(Main.clientConfig.getTheUsersTableName())) {
                getInfoLabel().setText("不能为本软件的用户表设定权限,因为它只对OP可见");
                return true;
            }
        } else {
            Optional<ColumnMetaData> columnMetaData = Main.db2table2columnMap.get(getDataBaseName4tableView()).get(tableName4tableView).stream().filter(c -> c.getColumnName().equals(columnName)).findFirst();
            if (columnMetaData.isPresent()) {
                if (columnMetaData.get().isAutoIncrement()) {
                    getInfoLabel().setText("不能为标识列设定权限, 因为它由sqlServer自动生成管理");
                    return true;
                }
            }
        }
        if (Main.user.getPermissions().getOrDefault(getDataBaseName4tableView(), new HashMap<>()).getOrDefault(getTableName4tableView(), new HashMap<>()).get(columnName) != null) {
            getInfoLabel().setText("不能为标识列设定权限, 因为已经设置过");
            return true;
        } else return false;
    }

    @FXML
    void setThisColumnCouldInspectCouldChangePermissionAsDefault(ActionEvent event) {
        String columnName = selectedColumn.getText().toLowerCase();
        if (cantModifyPermission(columnName)) return;
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(getDataBaseName4tableView(), getTableName4tableView(), columnName, false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(getDataBaseName4tableView(), getTableName4tableView(), columnName, true));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void setThisColumnCouldInspectNoChangePermissionAsDefault(ActionEvent event) {
        String columnName = selectedColumn.getText().toLowerCase();
        if (cantModifyPermission(columnName)) return;
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(getDataBaseName4tableView(), getTableName4tableView(), columnName, false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(getDataBaseName4tableView(), getTableName4tableView(), columnName, true));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void setThisColumnNoInspectNoChangePermissionAsDefault(ActionEvent event) {
        String columnName = selectedColumn.getText().toLowerCase();
        if (cantModifyPermission(columnName)) return;
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(getDataBaseName4tableView(), getTableName4tableView(), columnName, false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(getDataBaseName4tableView(), getTableName4tableView(), columnName, true));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    public void onCustomQueryButtonClicked() {//removed args: MouseEvent event
        try {
            ChoiceBox<String> choiceBox = Main.mainLogic.getDatabaseSourceChoiceBox();
            String databaseName = choiceBox.getValue() == null ? choiceBox.getItems().getFirst() : choiceBox.getValue();
            CQueryPacket cQueryPacket = new CQueryPacket(Main.user.getUuid(), "select " + getCustomQueryTextField().getText() + " from " + getTableChoiceBox().getValue(), databaseName);
            Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(cQueryPacket);
        } catch (Exception e) {
            Main.logger.log(e);
        }
    }
}
