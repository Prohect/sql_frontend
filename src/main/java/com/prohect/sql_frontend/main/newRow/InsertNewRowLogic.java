package com.prohect.sql_frontend.main.newRow;

import com.prohect.sql_frontend.main.Main;
import com.prohect.sql_frontend.main.MainLogic;
import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.Util;
import com.prohect.sql_frontend_common.packet.CInsertPacket;
import com.prohect.sql_frontend_common.packet.Packet;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;

public class InsertNewRowLogic implements Initializable {

    public static final String nullableString = "可选·输入";
    public static final String normalString = "请·输·入";
    public static final String hasDefaultString = "有·默认值";
    public static final String bitString = "1·OR·0";

    public static String databaseName = "";
    public static String tableName = "";
    FileChooser fileChooser;
    boolean hasIdentifier = false;
    int identifierIndex = -1;
    private boolean needUpdateMainTable = false;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<Object[]> theInsertTableView;
    private TableColumn selectedColumn;
    private int selectedRowIndex;
    private int selectedColumnIndex;

    public InsertNewRowLogic() {
        Main.insertNewRowLogic = this;
    }

    public boolean isNeedUpdateMainTable() {
        return needUpdateMainTable;
    }

    public void setNeedUpdateMainTable(boolean needUpdateMainTable) {
        this.needUpdateMainTable = needUpdateMainTable;
    }

    public int getIdentifierIndex() {
        return identifierIndex;
    }

    public void setIdentifierIndex(int identifierIndex) {
        this.identifierIndex = identifierIndex;
    }

    /**
     * if the first row of listFromCsv is columnNames, format the list to one whose column order is the same as the target tableView.
     * else, just make a new list which contains all of listFromCsv
     *
     * @param listFromCsv the source list
     * @param tableView   the target table which tells the information of the column order
     */
    private List<Object[]> getFormattedItems(List<String[]> listFromCsv, TableView<Object[]> tableView) {
        String[] columnsFromCsv = listFromCsv.getFirst();
        HashMap<Integer, Integer> listFromCsv2columnMap = new HashMap<>();
        ObservableList<TableColumn<Object[], ?>> columnsFromTableView = tableView.getColumns();
        for (int i = 0; i < columnsFromTableView.size(); i++) {
            String columnFromTableView = columnsFromTableView.get(i).getText();
            for (int i1 = 0; i1 < columnsFromCsv.length; i1++) {
                String columnFromCsv = columnsFromCsv[i1];
                if (columnFromTableView.equalsIgnoreCase(columnFromCsv)) {
                    listFromCsv2columnMap.put(i1, i);//在map中映射两类index
                    break;
                }
            }
        }
        List<Object[]> formattedItems = new ArrayList<>();
        if (!listFromCsv2columnMap.isEmpty()) {
            listFromCsv.removeFirst();
            for (Object[] originItem : listFromCsv) {
                Object[] formattedItem = getNewItem();
                listFromCsv2columnMap.forEach((k, v) -> formattedItem[v] = originItem[k]);
                formattedItems.add(formattedItem);
            }
        } else {
            formattedItems.addAll(listFromCsv);
        }
        return formattedItems;
    }

    private List<String[]> loadFromCsv(File file) throws Exception {
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

    private String getPromptOfColumn(ColumnMetaData columnMetaData) {
        if (columnMetaData.isAutoIncrement()) {
            return "×";
        } else if (columnMetaData.getColumnType().contains("bit")) {
            return bitString;
        } else if (columnMetaData.getColumnType().contains("int")) {
            return "0";
        } else if (columnMetaData.getColumnType().contains("float") || columnMetaData.getColumnType().contains("decimal") || columnMetaData.getColumnType().contains("numeric") || columnMetaData.getColumnType().contains("real")) {
            return "0.0";
        } else if (columnMetaData.getColumnType().contains("char")) {
            if (columnMetaData.isHasDefaultValue()) return hasDefaultString;
            if (columnMetaData.isNullable()) return nullableString;
            else return normalString;
        } else if (columnMetaData.isNullable()) {
            return nullableString;
        } else {
            return columnMetaData.getColumnType().toUpperCase();
        }
    }

    public TableView<Object[]> getTheInsertTableView() {
        return theInsertTableView;
    }

    @FXML
    public void newRowButtonOnAction() {
        this.getTheInsertTableView().getItems().add(getNewItem());
    }

    private Object[] getNewItem() {
        Object[] objects = new Object[this.theInsertTableView.getColumns().size()];
        ArrayList<ColumnMetaData> columnMetaDataList = Main.db2tb2columnMD.get(Main.mainLogic.getDataBaseName4tableView()).get(Main.mainLogic.getTableName4tableView());
        for (int i = 0; i < objects.length; i++) {
            ColumnMetaData columnMetaData = columnMetaDataList.get(i);
            objects[i] = getPromptOfColumn(columnMetaData);
        }
        return objects;
    }

    public boolean isHasIdentifier() {
        return hasIdentifier;
    }

    public void setHasIdentifier(boolean hasIdentifier) {
        this.hasIdentifier = hasIdentifier;
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
        try {
            theInsertTableView.getItems().remove(selectedRowIndex);
            this.infoLabel.setText("删除成功");
        } catch (IndexOutOfBoundsException ignored) {
            this.infoLabel.setText("已经删过选中的行");
        }
    }

    @FXML
    void submitTheChanges(MouseEvent event) {
        try {
            ArrayList<ColumnMetaData> columnMetaDataList = Main.db2tb2columnMD.get(Main.mainLogic.getDataBaseName4tableView()).get(Main.mainLogic.getTableName4tableView());
            ObservableList<Object[]> items = this.getTheInsertTableView().getItems();
            List<Packet> packets = new ArrayList<>();
            for (Object[] item : items) {
                boolean flag = true;//发现有非空的且没有默认值的文字类的内容，视为内容没有填完整，不进行发送
                for (int i1 = 0; i1 < item.length; i1++) {
                    if (!columnMetaDataList.get(i1).isNullable() && !columnMetaDataList.get(i1).isHasDefaultValue() && normalString.equals(item[i1])) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    this.infoLabel.setText("存在未填完非空列的行, 请检查！");
                    return;
                }
                int skipCounter = 0;//在VALUES后面要跳过的列的数目
                int[] nullIndexes = new int[columnMetaDataList.size()];
                if (identifierIndex != -1) {
                    nullIndexes[skipCounter] = identifierIndex;//如果有，略过自增长的标识列(ID)
                    skipCounter++;
                }
                for (int i1 = 0; i1 < item.length; i1++) {
                    if (i1 == identifierIndex) continue;
                    if (nullableString.equals(item[i1]) || hasDefaultString.equals(item[i1]) || item[i1] == null) {
                        nullIndexes[skipCounter] = i1;//找到可null的列，如果值为本软体默认或者空，略过 note:批量导入时可能产生null值
                        skipCounter++;
                    }
                }
                int nonNullCounter = 0;
                Object[] nonNullObjects = new Object[columnMetaDataList.size() - skipCounter];

                boolean first = true;
                StringBuilder cmd = null;
                for (int i1 = 0; i1 < columnMetaDataList.size(); i1++) {
                    boolean skip = false;
                    for (int i2 = 0; i2 < skipCounter; i2++)
                        if (nullIndexes[i2] == i1) {
                            skip = true;
                            break;
                        }
                    if (skip) continue;
                    ColumnMetaData cData = columnMetaDataList.get(i1);
                    Object object = item[i1];
                    nonNullObjects[nonNullCounter] = object;
                    nonNullCounter++;
                    if (first) {
                        first = false;
                        cmd = new StringBuilder("INSERT INTO ").append(Main.mainLogic.getTableName4tableView()).append(" ([").append(cData.getColumnName()).append("]");
                    } else {
                        cmd.append(",[").append(cData.getColumnName()).append("]");
                    }
                }
                first = true;
                for (Object object : nonNullObjects) {
                    if (first) {
                        first = false;
                        assert cmd != null;
                        cmd.append(") VALUES (").append(Util.isNumber((String) object) ? (String) object : Util.convert2SqlServerContextString(object));
                    } else {
                        if (bitString.equals(object)) object = "0";
                        cmd.append(",").append(Util.isNumber((String) object) ? (String) object : Util.convert2SqlServerContextString(object));
                    }
                }
                assert cmd != null;
                cmd.append(")");
                CInsertPacket cInsertPacket = new CInsertPacket(Main.user.getUuid(), cmd.toString(), Main.mainLogic.getDataBaseName4tableView());
                Main.packetID2insertedValueMap.put(cInsertPacket.getId(), item);
                packets.add(cInsertPacket);

            }
            for (Packet packet : packets) {
                Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(packet);
            }
        } catch (Exception e) {
            Main.logger.log(e);
        }
    }

    @FXML
    void removeAllRow(ActionEvent event) {
        theInsertTableView.getItems().clear();
        this.infoLabel.setText("删除所有行成功");
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
                List<String[]> listFromCsv0 = loadFromCsv(selectedFile);
                TableView<Object[]> tableView = theInsertTableView;
                ObservableList<TableColumn<Object[], ?>> tableColumns = tableView.getColumns();
                int columns = tableColumns.size();
                int skip = -1;
                ArrayList<ColumnMetaData> columnMetaDataArrayList = Main.db2tb2columnMD.get(databaseName).get(tableName);
                for (int i = 0; i < tableColumns.size(); i++) {
                    if (skip != -1) break;
                    TableColumn<Object[], ?> tableColumn = tableColumns.get(i);
                    for (ColumnMetaData columnMetaData : columnMetaDataArrayList)
                        if (columnMetaData.getColumnName().equals(tableColumn.getText()))
                            if (columnMetaData.isAutoIncrement()) {
                                skip = i;
                                break;
                            }
                }
                List<String[]> listFromCsv = new ArrayList<>();
                for (String[] strings : listFromCsv0) {
                    int copied = 0;
                    String[] newRow = new String[columns];
                    for (int i = 0; i < columns; i++) {
                        if (i == skip || copied >= strings.length) continue;
                        newRow[i] = strings[copied];
                        copied++;
                    }
                    listFromCsv.add(newRow);
                }
                List<Object[]> formattedItems = getFormattedItems(listFromCsv, tableView);
                tableView.getItems().addAll(formattedItems);
            } catch (Exception e) {
                Main.mainLogic.getInfoLabel().setText(e.getMessage());
                Main.logger.log(e);
            }
        } else {
            Main.logger.log("No file selected.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
