package com.prohect.sqlFrontend.main.newRow;

import com.prohect.sqlFrontend.main.Main;
import com.prohect.sqlFrontend.main.MainLogic;
import com.prohect.sqlFrontendCommon.ColumnMetaData;
import com.prohect.sqlFrontendCommon.Util;
import com.prohect.sqlFrontendCommon.packet.CInsertPacket;
import com.prohect.sqlFrontendCommon.packet.Packet;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;

import static com.prohect.sqlFrontendCommon.CollectionUtil.structureCloneAndMerge;

public class InsertNewRowLogic implements Initializable {

    public static final String nullableString = "可选·输入";
    public static final String normalString = "请·输·入";
    public static final String hasDefaultString = "有·默认值";
    public static final String bitString = "1·OR·0";

    public static String databaseName = "";
    public static String tableName = "";
    FileChooser fileChooser;
    boolean hasIdentifier = false;
    /**
     * for other logic  of UI don't use this and the one above, no getter for this two
     */
    int identifierIndex = -1;
    private boolean needUpdateMainTable = false;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<Object[]> theInsertTableView;
    private int selectedRowIndex;

    public InsertNewRowLogic() {
        Main.insertNewRowLogic = this;
    }

    /**
     * if the tableData's first row contains something same(ignore case) with the column name of columns of tableView, map them.
     * if nothing matches, return an empty map
     */
    private static HashMap<Integer, Integer> mapColumnIndex(List<String[]> tableData, TableView<Object[]> tableView, int identifierIndex) {
        String[] columnsFromCsv = tableData.getFirst();
        HashMap<Integer, Integer> listFromCsv2columnMap = new HashMap<>();
        ObservableList<TableColumn<Object[], ?>> columnsFromTableView = tableView.getColumns();
        for (int i = 0; i < columnsFromTableView.size(); i++) {
            if (i == identifierIndex) continue;
            String columnFromTableView = columnsFromTableView.get(i).getText();
            for (int i1 = 0; i1 < columnsFromCsv.length; i1++) {
                String columnFromCsv = columnsFromCsv[i1];
                if (columnFromTableView.equalsIgnoreCase(columnFromCsv)) {
                    listFromCsv2columnMap.put(i1, i);//在map中映射两类index
                    break;
                }
            }
        }
        return listFromCsv2columnMap;
    }

    public boolean isNeedUpdateMainTable() {
        return needUpdateMainTable;
    }

    public void setNeedUpdateMainTable(boolean needUpdateMainTable) {
        this.needUpdateMainTable = needUpdateMainTable;
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
    private List<Object[]> getFormattedItems(List<String[]> listFromCsv, TableView<Object[]> tableView, int identifierIndex) {
        HashMap<Integer, Integer> listFromCsv2tableColumn = mapColumnIndex(listFromCsv, tableView, identifierIndex);
        List<Object[]> formattedItems = new ArrayList<>();
        if (!listFromCsv2tableColumn.isEmpty()) {
            listFromCsv.removeFirst();
            for (String[] originItem : listFromCsv) {
                Object[] formattedItem = getNewItem();
                listFromCsv2tableColumn.forEach((k, v) -> {
                    if (originItem[k] != null) formattedItem[v] = originItem[k];
                });
                formattedItems.add(formattedItem);
            }
        } else {
            for (String[] originItem : listFromCsv) {
                Object[] formattedItem = getNewItem();
                for (int i = 0; i < originItem.length; i++) if (originItem[i] != null) formattedItem[i] = originItem[i];
                formattedItems.add(formattedItem);
            }
        }
        return formattedItems;
    }

    private List<String[]> loadListFromCsv(File file) throws Exception {
        Main.logger.log("loading file: " + file.getAbsolutePath().toLowerCase());
        if (!file.getName().toLowerCase().endsWith(".csv")) throw new Exception("文件不已.csv结尾!");
        List<String[]> objs = new ArrayList<>();
        if (file.exists()) {//file directly exported from Excel use utf-8 bom, which add a header to the file
            BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            while (br.ready()) {
                String line = br.readLine();
                String[] values = line.split(",");
                objs.add(values);
            }
        } else {
            Main.logger.log(new FileNotFoundException(file.getAbsolutePath()));
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

    @FXML
    public void onDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".csv")) {//eg. clientConfig.json
                loadFromFile(file);
            }
        }
    }

    @FXML
    public void onDragOver(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        for (File file : files) {
            String name = file.getName();
            if (name.toLowerCase().endsWith(".csv")) {
                event.acceptTransferModes(TransferMode.COPY);
                break;
            }
        }
    }

    private String[] getNewItem() {
        String[] strings = new String[this.theInsertTableView.getColumns().size()];
        ArrayList<ColumnMetaData> columnMetaDataList = Main.db2tb2columnMD.get(Main.mainLogic.getDataBaseName4tableView()).get(Main.mainLogic.getTableName4tableView());
        for (int i = 0; i < strings.length; i++) {
            ColumnMetaData columnMetaData = columnMetaDataList.get(i);
            strings[i] = getPromptOfColumn(columnMetaData);
        }
        return strings;
    }

    public void setHasIdentifier(boolean hasIdentifier) {
        this.hasIdentifier = hasIdentifier;
    }

    @FXML
    void tableViewOnMouseClicked() {
        selectedRowIndex = theInsertTableView.getSelectionModel().getSelectedIndex();
    }

    @FXML
    void removeSelectedRow() {
        try {
            theInsertTableView.getItems().remove(selectedRowIndex);
            this.infoLabel.setText("删除成功");
        } catch (IndexOutOfBoundsException ignored) {
            this.infoLabel.setText("已经删过选中的行");
        }
    }

    @FXML
    void submitTheChanges() {
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
                Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), _ -> new LinkedBlockingQueue<>()).add(packet);
            }
        } catch (Exception e) {
            Main.logger.log(e);
        }
    }

    @FXML
    void removeAllRow() {
        theInsertTableView.getItems().clear();
        this.infoLabel.setText("删除所有行成功");
    }

    @FXML
    void loadFromCsv2tableViewOnAction() {
        if (fileChooser == null) fileChooser = new FileChooser();
        String csvPath = Main.clientConfig.getLoadFromCsvPath();
        if (csvPath != null && !csvPath.isEmpty()) fileChooser.setInitialDirectory(new File(csvPath));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("选择.csv文件", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(MainLogic.stage4InsertNewRowsWindow);
        loadFromFile(selectedFile);
        if (selectedFile != null) Main.clientConfig.setLoadFromCsvPath(selectedFile.getParent());
    }

    private void loadFromFile(File selectedFile) {
        if (selectedFile != null) {
            try {
                TableView<Object[]> tableView = theInsertTableView;
                ObservableList<TableColumn<Object[], ?>> tableColumns = tableView.getColumns();
                int columns = tableColumns.size();
                int identifierIndex = -1;//the index of the autoincrement identifier if exist
                var identifierColumn = structureCloneAndMerge(Main.db2tb2columnMD.get(databaseName).get(tableName)).stream().filter(ColumnMetaData::isAutoIncrement).findFirst();
                if (identifierColumn.isPresent()) for (int i = 0; i < columns; i++) {
                    String columnName = tableColumns.get(i).getText();
                    if (identifierColumn.get().getColumnName().equals(columnName)) {
                        identifierIndex = i;
                        break;
                    }
                }
                List<String[]> listFromCsv = new ArrayList<>();//load and format it to match the column numbers
                for (String[] strings : loadListFromCsv(selectedFile)) {
                    String[] newRow = new String[columns];
                    for (int i = 0; i < strings.length; i++) {
                        String string = strings[i];
                        newRow[identifierIndex == -1 ? i : i < identifierIndex ? i : i + 1] = string;
                    }
                    listFromCsv.add(newRow);
                }
                tableView.getItems().addAll(getFormattedItems(listFromCsv, tableView, identifierIndex));
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
