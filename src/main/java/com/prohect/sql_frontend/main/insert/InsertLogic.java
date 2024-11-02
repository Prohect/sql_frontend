package com.prohect.sql_frontend.main.insert;

import com.prohect.sql_frontend.main.Main;
import com.prohect.sql_frontend.main.MainLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.packet.CInsertPacket;
import com.prohect.sql_frontend_common.packet.Packet;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class InsertLogic {

    public static final String nullableString = "可选·输入";
    public static final String normalString = "请·输·入";

    public static String databaseName = "";
    public static String tableName = "";
    FileChooser fileChooser;
    @FXML
    private CheckBox hasIdentifierCheckBox;
    @FXML
    private TextField identifierOfTableTextField;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<Object[]> theInsertTableView;
    private TableColumn selectedColumn;
    private int selectedRowIndex;
    private int selectedColumnIndex;

    public InsertLogic() {
        Main.insertLogic = this;
    }

    /**
     * if the first row of listFromCsv is columnNames, format the list to one whose column order is the same as the target tableView.
     * else, just make a new list which contains all of listFromCsv
     *
     * @param listFromCsv the source list
     * @param tableView   the target table which tells the information of the column order
     */
    private static List<Object[]> getFormattedItems(List<String[]> listFromCsv, TableView<Object[]> tableView) {
        String[] columnsFromCsv = listFromCsv.getFirst();
        HashMap<Integer, Integer> listFromCsv2columnMap = new HashMap<>();
        ObservableList<TableColumn<Object[], ?>> columnsFromTableView = tableView.getColumns();
        for (int i = 0; i < columnsFromTableView.size(); i++) {
            String columnFromTableView = columnsFromTableView.get(i).getText();
            for (int i1 = 0; i1 < columnsFromCsv.length; i1++) {
                String columnFromCsv = columnsFromCsv[i1];
                if (columnFromCsv.equalsIgnoreCase(columnFromTableView)) {
                    listFromCsv2columnMap.put(i1, i);
                    break;
                }
            }
        }
        List<Object[]> formattedItems = new ArrayList<>();
        if (!listFromCsv2columnMap.isEmpty()) {
            listFromCsv.removeFirst();
            for (Object[] originItem : listFromCsv) {
                Object[] formattedItem = new Object[columnsFromTableView.size()];
                listFromCsv2columnMap.forEach((k, v) -> formattedItem[v] = originItem[k]);
                formattedItems.add(formattedItem);
            }
        } else {
            formattedItems.addAll(listFromCsv);
        }
        return formattedItems;
    }

    private static List<String[]> loadFromCsv(File file) throws Exception {
        if (!file.getName().toLowerCase().endsWith(".csv")) throw new Exception("文件不已.csv结尾!");
        List<String[]> objs = new ArrayList<>();
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()) {
                String line = br.readLine();
                String[] values = line.split(",");
                objs.add(values);
            }
        } else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return objs;
    }

    public TableView<Object[]> getTheInsertTableView() {
        return theInsertTableView;
    }

    @FXML
    void newRowButtonOnAction(ActionEvent event) {
        Object[] objects = new Object[this.theInsertTableView.getColumns().size()];
        ArrayList<ColumnMetaData> columnMetaDataList = Main.db2table2columnMap.get(Main.mainLogic.getDataBase4tableView()).get(Main.mainLogic.getTableName4tableView());
        for (int i = 0; i < objects.length; i++) {
            ColumnMetaData columnMetaData = columnMetaDataList.get(i);
            if (columnMetaData.isAutoIncrement()) {
                objects[i] = "×";//I have no idea why this always false, from the server from the microsoft api, that isAutoIncrement always false
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
                boolean flag = true;//发现有非空的文字类的内容，视为内容没有填完整，不进行发送
                for (int i1 = 0; i1 < item.length; i1++) {
                    if (columnMetaDataList.get(i1).isNullable()) continue;
                    if (item[i1].equals(normalString)) {
                        flag = false;
                        break;
                    }
                }
                int skipCounter = 0;//在VALUES后面要跳过的列的数目
                int[] nullIndexerAlpha = new int[columnMetaDataList.size()];
                if (identifierIndex != -1) {
                    nullIndexerAlpha[skipCounter] = identifierIndex;//如果有，略过自增长的标识列(ID)
                    skipCounter++;
                }
                for (int i1 = 0; i1 < item.length; i1++) {
                    if (nullableString.equals(item[i1]) || item[i1] == null) {
                        nullIndexerAlpha[skipCounter] = i1;//找到可null的列，如果值为默认或者空，略过 note:批量导入时可能产生null值
                        skipCounter++;
                    }
                }
                int nonNullCounter = 0;
                Object[] nonNullObjects = new Object[columnMetaDataList.size() - skipCounter];

                boolean first = true;
                if (flag) {
                    StringBuilder cmd = null;
                    for (int i1 = 0; i1 < columnMetaDataList.size(); i1++) {
                        boolean skip = false;
                        for (int i2 = 0; i2 < skipCounter; i2++) {
                            if (nullIndexerAlpha[i2] == i1) {
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
                    for (Object object : nonNullObjects) {
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
            ignored.printStackTrace();
        }
    }

    @FXML
    void removeAllRow(ActionEvent event) {
        theInsertTableView.getItems().clear();
    }

    @FXML
    void loadFromCsv2tableViewOnAction(ActionEvent event) {
        if (fileChooser == null) fileChooser = new FileChooser();

        // 可以设置初始目录（可选）
        // fileChooser.setInitialDirectory(new File("C:\\"));

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("选择.csv文件", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(MainLogic.stage4InsertNewRowsWindow);

        if (selectedFile != null) {
            try {
                List<String[]> listFromCsv = loadFromCsv(selectedFile);
                TableView<Object[]> tableView = theInsertTableView;
                List<Object[]> formattedItems = getFormattedItems(listFromCsv, tableView);
                tableView.getItems().addAll(formattedItems);
            } catch (Exception e) {
                Main.mainLogic.getInfoLabel().setText(e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected.");
        }
    }
}
