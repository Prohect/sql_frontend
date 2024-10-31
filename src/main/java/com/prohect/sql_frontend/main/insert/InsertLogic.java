package com.prohect.sql_frontend.main.insert;

import com.prohect.sql_frontend.main.Main;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.Packet;
import com.prohect.sql_frontend_common.packet.CInsertPacket;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class InsertLogic {

    public static final String nullableString = "可选·输入";
    public static final String normalString = "请·输·入";

    public static String databaseName = "";
    public static String tableName = "";

    public InsertLogic() {
        Main.insertLogic = this;
    }

    public TableView<Object[]> getTheInsertTableView() {
        return theInsertTableView;
    }

    @FXML
    private CheckBox hasIdentifierCheckBox;

    @FXML
    private TextField identifierOfTableTextField;

    @FXML
    private Label infoLabel;

    @FXML
    private TableView<Object[]> theInsertTableView;

    @FXML
    void newRowButtonOnAction(ActionEvent event) {
        Object[] objects = new Object[this.theInsertTableView.getColumns().size()];
        ArrayList<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(Main.mainLogic.getDataBase4tableView()).get(Main.mainLogic.getTableName4tableView());
        for (int i = 0; i < objects.length; i++) {
            ColumnMetaData columnMetaData = columnMetaDataList.get(i);
            if (columnMetaData.isAutoIncrement()) {
                objects[i] = "×";//TODO:I have no idea why this always false, from the server from the microsoft api, that isAutoIncrement always false
            } else if (columnMetaData.getColumnType().contains("bit")) {
                objects[i] = "true OR false";
            } else if (columnMetaData.getColumnType().contains("int")) {
                objects[i] = "0";
            } else if (columnMetaData.getColumnType().contains("float") || columnMetaData.getColumnType().contains("decimal") || columnMetaData.getColumnType().contains("numeric") || columnMetaData.getColumnType().contains("real")) {
                objects[i] = "0.0";
            } else if (columnMetaData.getColumnType().contains("char")) {
                if (columnMetaData.isNullable()) objects[i] = nullableString;
                else objects[i] = normalString;
            } else if (columnMetaData.isNullable()) {
                objects[i] = nullableString;
            } else {
                objects[i] = columnMetaData.getColumnType().toUpperCase();
            }
        }
        this.getTheInsertTableView().getItems().add(objects);
    }

    private TableColumn selectedColumn;

    private int selectedRowIndex;
    private int selectedColumnIndex;

    @FXML
    void tableViewOnMouseClicked(MouseEvent event) {
        selectedRowIndex = theInsertTableView.getSelectionModel().getSelectedIndex();
        ObservableList<TablePosition> selectedCells = theInsertTableView.getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) return;
        TablePosition position = selectedCells.get(0);
        selectedColumnIndex = position.getColumn();
        selectedColumn = position.getTableColumn();
    }

    @FXML
    void removeSelectedRow(ActionEvent event) {
        theInsertTableView.getItems().remove(selectedRowIndex);
    }

    @FXML
    void submitTheChanges(MouseEvent event) {
        try {
            ArrayList<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(Main.mainLogic.getDataBase4tableView()).get(Main.mainLogic.getTableName4tableView());
            boolean hasIdentifier = hasIdentifierCheckBox.isSelected();
            int identifierIndex = -1;
            if (hasIdentifier) {
                for (int i = 0; i < columnMetaDataList.size(); i++) {
                    if (columnMetaDataList.get(i).getColumnName().equals(identifierOfTableTextField.getText().toLowerCase())) {
                        identifierIndex = i;
                        break;
                    }
                }
                if (identifierIndex == -1) {
                    infoLabel.setText("info: 没有该列名" + identifierOfTableTextField.getText() + "!");
                    return;
                }
            }
            ObservableList<Object[]> items = this.getTheInsertTableView().getItems();
            List<Packet> packets = new ArrayList<>();
            for (Object[] item : items) {
                boolean flag = true;
                for (int i1 = 0; i1 < item.length; i1++) {
                    if (columnMetaDataList.get(i1).isNullable()) continue;
                    if (item[i1].equals(normalString)) {
                        flag = false;
                        break;
                    }
                }
                int skipCounter = 0;
                int[] nullIndexerAlpha = new int[columnMetaDataList.size()];
                if (identifierIndex != -1) {
                    nullIndexerAlpha[skipCounter] = identifierIndex;
                    skipCounter++;
                }
                for (int i1 = 0; i1 < item.length; i1++) {
                    if (item[i1].equals(nullableString)) {
                        nullIndexerAlpha[skipCounter] = i1;
                        skipCounter++;
                    }
                }
                int[] nullIndexerBeta = new int[skipCounter];
                int nonNullCounter = 0;
                Object[] nonNullObjects = new Object[columnMetaDataList.size() - skipCounter];
                System.arraycopy(nullIndexerAlpha, 0, nullIndexerBeta, 0, skipCounter);

                boolean first = true;
                if (flag) {
                    StringBuilder cmd = null;
                    for (int i1 = 0; i1 < columnMetaDataList.size(); i1++) {
                        boolean skip = false;
                        for (int i2 = 0; i2 < nullIndexerBeta.length; i2++) {
                            if (nullIndexerBeta[i2] == i1) {
                                skip = true;
                                break;
                            }
                        }
                        if (skip) continue;
                        ColumnMetaData cData = columnMetaDataList.get(i1);
                        Object object = item[i1];
                        nonNullObjects[nonNullCounter] = object;
                        nonNullCounter++;
                        if (first) {
                            first = false;
                            cmd = new StringBuilder("INSERT INTO ").append(Main.mainLogic.getTableName4tableView()).append(" (").append(cData.getColumnName());
                        } else {
                            cmd.append(",").append(cData.getColumnName());
                        }
                    }
                    first = true;
                    for (int i1 = 0; i1 < nonNullObjects.length; i1++) {
                        Object object = nonNullObjects[i1];
                        if (first) {
                            first = false;
                            assert cmd != null;
                            cmd.append(") VALUES (").append(CommonUtil.isNumber((String) object) ? (String) object : CommonUtil.convert2SqlServerContextString(object));
                        } else {
                            cmd.append(",").append(CommonUtil.isNumber((String) object) ? (String) object : CommonUtil.convert2SqlServerContextString(object));
                        }
                    }
                    assert cmd != null;
                    cmd.append(")");
                    CInsertPacket cInsertPacket = new CInsertPacket(Main.user.getUuid(), cmd.toString(), Main.mainLogic.getDataBase4tableView());
                    Main.packetID2insertedValueMap.put(cInsertPacket.getId(), item);
                    packets.add(cInsertPacket);
                }
            }
            for (Packet packet : packets) {
                Main.ctx2packetsMap.computeIfAbsent(Main.ctx, c -> new LinkedBlockingQueue<>()).add(packet);
            }
        } catch (Exception ignored) {
        }
    }
}
