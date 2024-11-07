package com.prohect.sql_frontend.main.newTable;

import com.prohect.sql_frontend_common.ColumnMetaData;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class insertNewTableLogic implements Initializable {

//    Stage

    private HashMap<String, ColumnMetaData> name2columnMetaData;

    @FXML
    private TableView<String[]> table;

    @FXML
    void alterSelectedColumnOnAction(ActionEvent event) {

    }

    @FXML
    void insertNewColumnOnAction(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<TableColumn<String[], ?>> columns = table.getColumns();
        ObservableList<String[]> items = table.getItems();
        String[] namesOfColumnsToBeCreated = new String[columns.size()];
        items.add(namesOfColumnsToBeCreated);
        for (int i = 0; i < columns.size(); i++) {
            switch (i) {
                case 0: {
                    namesOfColumnsToBeCreated[i] = "id";
                    name2columnMetaData.put("C%d".formatted(i), new ColumnMetaData("", "bigint", true, true, false, "0", 0, 0, true, false));
                    break;
                }
                case 1: {
                    namesOfColumnsToBeCreated[i] = "名称";
                    name2columnMetaData.put("C%d".formatted(i), new ColumnMetaData("", "varchar(50)", false, false, false, "", 0, 0, true, false));
                    break;
                }
                default: {
                    namesOfColumnsToBeCreated[i] = "属性%d".formatted(i);
                    name2columnMetaData.put("C%d".formatted(i), new ColumnMetaData("", "varchar(50)", false, false, true, "", 0, 0, false, false));
                    break;
                }
            }
        }
    }
}
