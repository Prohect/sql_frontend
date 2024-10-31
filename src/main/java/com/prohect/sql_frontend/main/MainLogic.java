package com.prohect.sql_frontend.main;

import com.prohect.sql_frontend.ClientHandlerAdapter;
import com.prohect.sql_frontend.LoginUi;
import com.prohect.sql_frontend.main.insert.InsertLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.packet.CAlterPacket;
import com.prohect.sql_frontend_common.packet.CDeletePacket;
import com.prohect.sql_frontend_common.packet.CQueryPacket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


public class MainLogic {

    public MainLogic() {
        Main.mainLogic = this;
    }

    String dataBase4tableView;
    String tableName4tableView;

    public String getDataBase4tableView() {
        return dataBase4tableView;
    }

    public void setDataBase4tableView(String dataBase4tableView) {
        this.dataBase4tableView = dataBase4tableView;
    }

    public String getTableName4tableView() {
        return tableName4tableView;
    }

    public void setTableName4tableView(String tableName4tableView) {
        this.tableName4tableView = tableName4tableView;
    }

    public MenuItem getCreateColumnMenuItem() {
        return createColumnMenuItem;
    }

    public Button getCustomQueryButton() {
        return customQueryButton;
    }

    public TextField getCustomQueryTextField() {
        return customQueryTextField;
    }

    public ChoiceBox<String> getDatabaseSourceChoiceBox() {
        return databaseSourceChoiceBox;
    }

    public Label getDatabaseSourceChoiceBoxLabel() {
        return databaseSourceChoiceBoxLabel;
    }

    public Label getInfoLabel() {
        return infoLabel;
    }

    public Menu getInspectFromDBMenu() {
        return inspectFromDBMenu;
    }

    public Menu getInspectFromLocalMenu() {
        return inspectFromLocalMenu;
    }

    public TableView<Object[]> getMainTable() {
        return mainTable;
    }

    public ChoiceBox<String> getTableChoiceBox() {
        return tableChoiceBox;
    }

    @FXML
    private MenuItem createColumnMenuItem;

    @FXML
    private Button customQueryButton;

    @FXML
    private TextField customQueryTextField;

    @FXML
    private ChoiceBox<String> databaseSourceChoiceBox;

    @FXML
    private Label databaseSourceChoiceBoxLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private Menu inspectFromDBMenu;

    @FXML
    private Menu inspectFromLocalMenu;

    @FXML
    private TableView<Object[]> mainTable;

    @FXML
    private MenuItem setThisColumnCouldInspectCouldChangePermissionAsDefaultMenuItem;

    @FXML
    private MenuItem setThisColumnCouldInspectNoChangePermissionAsDefaultMenuItem;

    @FXML
    private MenuItem setThisColumnNoInspectNoChangePermissionAsDefaultMenuItem;

    @FXML
    private ChoiceBox<String> tableChoiceBox;

    public static Stage stage4InsertNewRowsWindow;
    public static Scene scene4InsertNewRowsScene;
    public static FXMLLoader insertFXMLLoader = new FXMLLoader(LoginUi.class.getResource("insert-view.fxml"));
    public static Stage stage4InsertNewColumnWindow;
    public static Scene scene4InsertNewColumnScene;
    public static FXMLLoader insertNewRowFXMLLoader = new FXMLLoader(LoginUi.class.getResource("newRow-view.fxml"));

    static {
        try {
            scene4InsertNewRowsScene = new Scene(insertFXMLLoader.load(), 640, 400);
            scene4InsertNewColumnScene = new Scene(insertNewRowFXMLLoader.load(), 359, 127);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void insertNewColumnMenuItemOnAction(ActionEvent event) {
        if (stage4InsertNewColumnWindow == null) {
            stage4InsertNewColumnWindow = new Stage();
            stage4InsertNewColumnWindow.setTitle("Alter New Column");
            stage4InsertNewColumnWindow.setScene(scene4InsertNewColumnScene);
            Main.insertNewColumnLogic.getNotNull().selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) Main.insertNewColumnLogic.getAsDefault().selectedProperty().set(true);
            });
            Main.insertNewColumnLogic.getAsDefault().selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) Main.insertNewColumnLogic.getNotNull().selectedProperty().set(false);
            });
            ObservableList<String> list = FXCollections.observableArrayList(List.of("int", "bigint", "decimal(38, 15)", "float(8)", "REAL", "nchar(20)", "varchar(50)", "nvarchar(max)", "bit", "money", "date", "time", "datetime2", "datetimeOffset"));
            Main.insertNewColumnLogic.getColumnTypeChoiceBox().setItems(list);
        }
        stage4InsertNewColumnWindow.show();
    }

    @FXML
    public void insertNewRowMenuItemOnAction(ActionEvent event) {
        try {
            if (stage4InsertNewRowsWindow == null) {
                stage4InsertNewRowsWindow = new Stage();
                stage4InsertNewRowsWindow.setTitle("Insert New Row");
                stage4InsertNewRowsWindow.setScene(scene4InsertNewRowsScene);
                Main.insertLogic.getTheInsertTableView().setEditable(true);
                stage4InsertNewRowsWindow.setAlwaysOnTop(true);
            }
            if (this.getMainTable().getColumns().isEmpty()) {
                this.infoLabel.setText("请先选择表并查询以获取列数据数据");
                return;
            }
            if (!(InsertLogic.tableName.equals(this.tableName4tableView) && InsertLogic.databaseName.equals(this.dataBase4tableView))) {
                Main.insertLogic.getTheInsertTableView().setItems(FXCollections.observableArrayList());
                ObservableList<TableColumn<Object[], ?>> columnObservableList = Main.insertLogic.getTheInsertTableView().getColumns();
                columnObservableList.clear();
                ArrayList<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(this.getDatabaseSourceChoiceBox().getValue()).get(this.getTableChoiceBox().getValue());
                for (int i = 0; i < columnMetaDataList.size(); i++) {
                    String columnName = columnMetaDataList.get(i).getColumnName();
                    TableColumn<Object[], Object> column = ClientHandlerAdapter.getTableColumn(columnName, i);
                    columnObservableList.add(column);
                    ArrayList<ColumnMetaData> columnMetaData = Main.db2table2columnMap.get(this.getDataBase4tableView()).get(this.getTableName4tableView());
                    ColumnMetaData[] array = columnMetaData.stream().filter((c) -> c.getColumnName().equals(columnName)).toArray(ColumnMetaData[]::new);
                    if (array[0].isAutoIncrement()) continue;
                    ClientHandlerAdapter.setCellFactory(column);
                    column.setOnEditCommit(event1 -> {
                        // 直接更新数据，点击提交按钮时再处理
                        int targetRowIndex = event1.getTablePosition().getRow();
                        int targetColumnIndex = event1.getTablePosition().getColumn();
                        Object[] row = event1.getTableView().getItems().get(targetRowIndex);
                        String newValue = (String) event1.getNewValue();
                        row[targetColumnIndex] = newValue;
                    });
                }
            }
            InsertLogic.databaseName = this.dataBase4tableView;
            InsertLogic.tableName = this.tableName4tableView;
            stage4InsertNewRowsWindow.show();
        } catch (RuntimeException e) {
            Main.mainLogic.getInfoLabel().setText("不支持的操作：向本应用的users表手动中添加行");
        }
    }

    private TableColumn selectedColumn;

    private int selectedRowIndex;
    private int selectedColumnIndex;

    @FXML
    void mainTableOnMouseClicked(MouseEvent event) {
        selectedRowIndex = this.getMainTable().getSelectionModel().getSelectedIndex();
        ObservableList<TablePosition> selectedCells = this.getMainTable().getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) return;
        TablePosition position = selectedCells.get(0);
        selectedColumnIndex = position.getColumn();
        selectedColumn = position.getTableColumn();
        if (Main.user.isOP()) {
            setThisColumnNoInspectNoChangePermissionAsDefaultMenuItem.setVisible(true);
            setThisColumnCouldInspectNoChangePermissionAsDefaultMenuItem.setVisible(true);
            setThisColumnCouldInspectCouldChangePermissionAsDefaultMenuItem.setVisible(true);
        }
    }

    @FXML
    void deleteRowMenuItemOnAction(ActionEvent event) {
        if (!Main.user.isOP()) {
            List<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(dataBase4tableView).get(tableName4tableView);
            HashMap<String, Boolean[]> column2permissions = Main.user.getPermissions().get(dataBase4tableView).get(tableName4tableView);
            for (int i = 0; i < columnMetaDataList.size(); i++) {
                if (column2permissions.containsKey(columnMetaDataList.get(i).getColumnName())) {
                    if (column2permissions.get(columnMetaDataList.get(i).getColumnName())[1]) {
                        infoLabel.setText("您没有足够的写入权限这么做！");
                        return;
                    }
                }
            }
        }
        Object o = mainTable.getItems().get(selectedRowIndex)[0];
        StringBuilder cmd = new StringBuilder("DELETE FROM " + tableName4tableView + " WHERE " + mainTable.getColumns().getFirst().getText() + " = " + CommonUtil.convert2SqlServerContextString(o));
        for (int i = 1; i < mainTable.getColumns().size(); i++) {
            Object object = mainTable.getItems().get(selectedRowIndex)[i];
            if (object == null) continue;
            cmd.append(" AND ").append(mainTable.getColumns().get(i).getText()).append(" = ").append(CommonUtil.convert2SqlServerContextString(object));
        }
        CDeletePacket cDeletePacket = new CDeletePacket(Main.user.getUuid(), cmd.toString(), dataBase4tableView);
        Main.packetID2DeletedValueMap.put(cDeletePacket.getId(), mainTable.getItems().get(selectedRowIndex));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(cDeletePacket);
    }

    @FXML
    void databaseSourceChoiceBoxOnMouseClicked(MouseEvent event) {
    }

    @FXML
    void setThisColumnCouldInspectCouldChangePermissionAsDefault(ActionEvent event) {
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), true));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void setThisColumnCouldInspectNoChangePermissionAsDefault(ActionEvent event) {
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), true));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void setThisColumnNoInspectNoChangePermissionAsDefault(ActionEvent event) {
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), true));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void onCustomQueryButtonClicked(MouseEvent event) {
        try {
            ChoiceBox<String> choiceBox = Main.mainLogic.getDatabaseSourceChoiceBox();
            String databaseName = choiceBox.getValue() == null ? choiceBox.getItems().getFirst() : choiceBox.getValue();
            CQueryPacket cQueryPacket = new CQueryPacket(Main.user.getUuid(), "select " + customQueryTextField.getText() + " from " + this.getTableChoiceBox().getValue(), databaseName);
            Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(cQueryPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
