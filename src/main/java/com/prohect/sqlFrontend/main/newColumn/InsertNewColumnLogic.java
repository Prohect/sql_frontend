package com.prohect.sqlFrontend.main.newColumn;

import com.prohect.sqlFrontend.main.Main;
import com.prohect.sqlFrontend.main.MainLogic;
import com.prohect.sqlFrontendCommon.Util;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class InsertNewColumnLogic {
    private boolean needUpdateMainTable = false;
    @FXML
    private CheckBox hasDefaultCheckBox;
    @FXML
    private TextField autoIncrementDelta;
    @FXML
    private TextField autoIncrementHome;
    @FXML
    private TextField columnNameTextField;
    @FXML
    private ChoiceBox<String> columnTypeChoiceBox;
    @FXML
    private TextField defaultTextField;
    @FXML
    private CheckBox isAutoIncrementCheckBox;
    @FXML
    private CheckBox isPrimaryKeyCheckBox;
    @FXML
    private CheckBox isUniqueCheckBox;
    @FXML
    private CheckBox notNullCheckBox;

    public InsertNewColumnLogic() {
        Main.insertNewColumnLogic = this;
    }

    public boolean isNeedUpdateMainTable() {
        return needUpdateMainTable;
    }

    public void setNeedUpdateMainTable(boolean needUpdateMainTable) {
        this.needUpdateMainTable = needUpdateMainTable;
    }

    public ChoiceBox<String> getColumnTypeChoiceBox() {
        return columnTypeChoiceBox;
    }

    public CheckBox getHasDefaultCheckBox() {
        return hasDefaultCheckBox;
    }

    public CheckBox getNotNullCheckBox() {
        return notNullCheckBox;
    }

    @FXML
    void submit() {
        if (!Main.user.isOp()) {
            Main.mainLogic.getInfoLabel().setText("您的权限不足以执行此操作!");
            return;
        }
        if (getColumnTypeChoiceBox().getValue() == null || columnNameTextField.getText().isBlank()) {
            Main.mainLogic.getInfoLabel().setText("请至少提供列名和类型! ");
            return;
        }
        StringBuilder cmd = new StringBuilder("ALTER TABLE " + Main.mainLogic.getCurrentTableName() + " ADD [" + columnNameTextField.getText() + "] " + columnTypeChoiceBox.getValue());
        if (isAutoIncrementCheckBox.isSelected()) {
            cmd.append(" IDENTITY(").append(autoIncrementHome.getText()).append(",").append(autoIncrementDelta.getText()).append(")");
        } else {
            if (notNullCheckBox.isSelected()) cmd.append(" NOT NULL ");
            if (hasDefaultCheckBox.isSelected())
                cmd.append(" DEFAULT ").append(Util.convert2SqlServerContextString(defaultTextField.getText()));
            if (isUniqueCheckBox.isSelected()) cmd.append(" UNIQUE");
        }
        if (isPrimaryKeyCheckBox.isSelected()) cmd.append(" PRIMARY KEY");
        MainLogic.sendSqlAlterCommands2targetDB(Main.mainLogic.getCurrentDataBaseName(), cmd.toString());
    }
}
