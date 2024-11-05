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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;


public class MainLogic implements Initializable {
    public static Stage stage4InsertNewRowsWindow;
    public static Scene scene4InsertNewRowsScene;
    public static FXMLLoader insertFXMLLoader = new FXMLLoader(MainUi.class.getResource("insert-view.fxml"));
    public static Stage stage4InsertNewColumnWindow;
    public static Scene scene4InsertNewColumnScene;
    public static FXMLLoader insertNewColumnFXMLLoader = new FXMLLoader(MainUi.class.getResource("newColumn-view.fxml"));
    String dataBase4tableView;
    String tableName4tableView;
    TextInputDialog textInputDialog4newTableName;
    @FXML
    private MenuItem createColumnMenuItem;
    @FXML
    private MenuItem createRowMenuItem;
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
    private TableView<Object[]> tableView;
    @FXML
    private AnchorPane pane4TableView;
    @FXML
    private MenuItem setThisColumnCouldInspectCouldChangePermissionAsDefaultMenuItem;
    @FXML
    private MenuItem setThisColumnCouldInspectNoChangePermissionAsDefaultMenuItem;
    @FXML
    private MenuItem setThisColumnNoInspectNoChangePermissionAsDefaultMenuItem;
    @FXML
    private ChoiceBox<String> tableChoiceBox;
    private TableColumn selectedColumn;
    private int selectedRowIndex;
    private int selectedColumnIndex;

    public MainLogic() {
        Main.mainLogic = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            try {
//                AnchorPane.setTopAnchor(tableView, 0.0);
//                AnchorPane.setLeftAnchor(tableView, 0.0);
//                AnchorPane.setBottomAnchor(tableView, 0.0);
//                AnchorPane.setRightAnchor(tableView, 0.0);
//                pane4TableView.widthProperty().addListener((observable, oldValue, newValue) -> {
//                    tableView.setPrefWidth(newValue.doubleValue());
//                    System.out.println("MainLogic.initialize.pane4TableView.widthProperty() = " + newValue.doubleValue());
//                });
//                pane4TableView.heightProperty().addListener((observable, oldValue, newValue) -> {
//                    tableView.setPrefHeight(newValue.doubleValue());
//                    System.out.println("MainLogic.initialize.pane4TableView.heightProperty() = " + newValue.doubleValue());
//                });
                scene4InsertNewRowsScene = new Scene(insertFXMLLoader.load(), 640, 400);
                scene4InsertNewColumnScene = new Scene(insertNewColumnFXMLLoader.load(), 359, 127);

                stage4InsertNewColumnWindow = new Stage();
                stage4InsertNewColumnWindow.setTitle("Alter New Column");
                stage4InsertNewColumnWindow.setScene(scene4InsertNewColumnScene);
                stage4InsertNewColumnWindow.setResizable(false);
                Main.insertNewColumnLogic.getNotNull().selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) Main.insertNewColumnLogic.getAsDefault().selectedProperty().set(true);
                });
                Main.insertNewColumnLogic.getAsDefault().selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (!newValue) Main.insertNewColumnLogic.getNotNull().selectedProperty().set(false);
                });
                ObservableList<String> list = FXCollections.observableArrayList(List.of("int", "bigint", "decimal(38, 15)", "float(8)", "REAL", "nchar(20)", "varchar(50)", "nvarchar(max)", "bit", "money", "date", "time", "datetime2", "datetimeOffset"));
                Main.insertNewColumnLogic.getColumnTypeChoiceBox().setItems(list);

                stage4InsertNewRowsWindow = new Stage();
                stage4InsertNewRowsWindow.setTitle("Insert New Row");
                stage4InsertNewRowsWindow.setScene(scene4InsertNewRowsScene);
                stage4InsertNewRowsWindow.setOnCloseRequest(event -> {
                    stage4InsertNewRowsWindow.close();
                    this.onCustomQueryButtonClicked();
                });
                stage4InsertNewRowsWindow.initOwner(MainUi.getWindow());
                stage4InsertNewRowsWindow.initModality(Modality.WINDOW_MODAL);
                stage4InsertNewRowsWindow.setMinWidth(520);
                stage4InsertNewRowsWindow.setMinHeight(260);
                Main.insertNewRowLogic.getTheInsertTableView().setEditable(true);

                textInputDialog4newTableName = new TextInputDialog("表1");
                textInputDialog4newTableName.setTitle("创建新表");
                textInputDialog4newTableName.setHeaderText("请输入要创建的表的名称:");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

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

    public TableView<Object[]> getTableView() {
        return tableView;
    }

    public ChoiceBox<String> getTableChoiceBox() {
        return tableChoiceBox;
    }

    @FXML
    void insertNewColumnMenuItemOnAction(ActionEvent event) {
        stage4InsertNewColumnWindow.show();
    }

    @FXML
    public void insertNewRowMenuItemOnAction(ActionEvent event) {
        try {
            if (this.getTableView().getColumns().isEmpty()) {
                this.infoLabel.setText("请先选择表并查询以获取列数据数据");
                return;
            }
            if (!(InsertNewRowLogic.tableName.equals(this.tableName4tableView) && InsertNewRowLogic.databaseName.equals(this.dataBase4tableView))) {//当表名和数据库名变化时更新
                updateColumnMetaDataOfInsertNewRowTable();
            }
            InsertNewRowLogic.databaseName = this.dataBase4tableView;
            InsertNewRowLogic.tableName = this.tableName4tableView;
            stage4InsertNewRowsWindow.show();
        } catch (RuntimeException e) {
            e.printStackTrace();
            Main.mainLogic.getInfoLabel().setText("不支持的操作：向本应用的users表手动中添加行");
        }
    }

    public void updateColumnMetaDataOfInsertNewRowTable() {
        Platform.runLater(() -> {
            Main.insertNewRowLogic.getTheInsertTableView().setItems(FXCollections.observableArrayList());
            ObservableList<TableColumn<Object[], ?>> columnObservableList = Main.insertNewRowLogic.getTheInsertTableView().getColumns();
            columnObservableList.clear();
            ArrayList<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(this.getDatabaseSourceChoiceBox().getValue()).get(this.getTableChoiceBox().getValue());
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
        selectedRowIndex = this.getTableView().getSelectionModel().getSelectedIndex();
        ObservableList<TablePosition> selectedCells = this.getTableView().getSelectionModel().getSelectedCells();
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
        Object o = tableView.getItems().get(selectedRowIndex)[0];
        assert o != null;//第一个一般是主键，断言不为null
        StringBuilder cmd = new StringBuilder("DELETE FROM " + tableName4tableView + " WHERE " + tableView.getColumns().getFirst().getText() + " = " + CommonUtil.convert2SqlServerContextString(o));
        for (int i = 1; i < tableView.getColumns().size(); i++) {
            Object object = tableView.getItems().get(selectedRowIndex)[i];
            if (object == null) continue;
            cmd.append(" AND ").append(tableView.getColumns().get(i).getText()).append(" = ").append(CommonUtil.convert2SqlServerContextString(object));
        }
        CDeletePacket cDeletePacket = new CDeletePacket(Main.user.getUuid(), cmd.toString(), dataBase4tableView);
        Main.packetID2DeletedValueMap.put(cDeletePacket.getId(), tableView.getItems().get(selectedRowIndex));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(cDeletePacket);
    }

    @FXML
    void databaseSourceChoiceBoxOnMouseClicked(MouseEvent event) {
    }

    @FXML
    void setThisColumnCouldInspectCouldChangePermissionAsDefault(ActionEvent event) {
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), true));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void setThisColumnCouldInspectNoChangePermissionAsDefault(ActionEvent event) {
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 1", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), true));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void setThisColumnNoInspectNoChangePermissionAsDefault(ActionEvent event) {
        String sql1 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), false));
        String sql2 = String.format("ALTER TABLE " + Main.clientConfig.getTheUsersTableName() + " ADD %s BIT NOT NULL DEFAULT 0", CommonUtil.permissionColumnNameEncode(this.getDataBase4tableView(), this.getTableName4tableView(), selectedColumn.getText(), true));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql1, Main.clientConfig.getTheUsersDatabaseName()));
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(new CAlterPacket(Main.user.getUuid(), sql2, Main.clientConfig.getTheUsersDatabaseName()));
    }

    @FXML
    void onCustomQueryButtonClicked() {//removed args: MouseEvent event
        try {
            ChoiceBox<String> choiceBox = Main.mainLogic.getDatabaseSourceChoiceBox();
            String databaseName = choiceBox.getValue() == null ? choiceBox.getItems().getFirst() : choiceBox.getValue();
            CQueryPacket cQueryPacket = new CQueryPacket(Main.user.getUuid(), "select " + customQueryTextField.getText() + " from " + this.getTableChoiceBox().getValue(), databaseName);
            Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(cQueryPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
