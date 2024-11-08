package com.prohect.sql_frontend.main.newColumn;

import com.prohect.sql_frontend.main.Main;
import com.prohect.sql_frontend_common.CommonUtil;
import com.prohect.sql_frontend_common.packet.CAlterPacket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.util.concurrent.LinkedBlockingQueue;

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
    void submit(ActionEvent event) {
        if (!Main.user.isOp()) {
            Main.mainLogic.getInfoLabel().setText("您的权限不足以执行此操作!");
            return;
        }
        if (getColumnTypeChoiceBox().getValue() == null || columnNameTextField.getText().isBlank()) {
            Main.mainLogic.getInfoLabel().setText("请至少提供列名和类型! ");
            return;
        }
        StringBuilder cmd = new StringBuilder("ALTER TABLE " + Main.mainLogic.getTableName4tableView() + " ADD [" + columnNameTextField.getText() + "] " + columnTypeChoiceBox.getValue());
        if (isAutoIncrementCheckBox.isSelected()) {
            cmd.append(" IDENTITY(").append(autoIncrementHome.getText()).append(",").append(autoIncrementDelta.getText()).append(")");
        } else {
            if (notNullCheckBox.isSelected()) cmd.append(" NOT NULL ");
            if (hasDefaultCheckBox.isSelected())
                cmd.append(" DEFAULT ").append(CommonUtil.convert2SqlServerContextString(defaultTextField.getText()));
            if (isUniqueCheckBox.isSelected()) cmd.append(" UNIQUE");
        }
        if (isPrimaryKeyCheckBox.isSelected()) cmd.append(" PRIMARY KEY");
        CAlterPacket cAlterPacket = new CAlterPacket(Main.user.getUuid(), cmd.toString(), Main.mainLogic.getDataBaseName4tableView());
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(cAlterPacket);
    }
}
