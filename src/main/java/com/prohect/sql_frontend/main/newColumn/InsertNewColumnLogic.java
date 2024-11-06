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

    @FXML
    private CheckBox asDefault;
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
    private CheckBox isAutoIncrement;
    @FXML
    private CheckBox isPrimaryKey;
    @FXML
    private CheckBox isUnique;
    @FXML
    private CheckBox notNull;

    public InsertNewColumnLogic() {
        Main.insertNewColumnLogic = this;
    }

    public ChoiceBox<String> getColumnTypeChoiceBox() {
        return columnTypeChoiceBox;
    }

    public CheckBox getAsDefault() {
        return asDefault;
    }

    public CheckBox getNotNull() {
        return notNull;
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
        if (isAutoIncrement.isSelected()) {
            cmd.append(" IDENTITY(").append(autoIncrementHome.getText()).append(",").append(autoIncrementDelta.getText()).append(")");
        } else {
            if (notNull.isSelected()) cmd.append(" NOT NULL ");
            if (asDefault.isSelected())
                cmd.append(" DEFAULT ").append(CommonUtil.convert2SqlServerContextString(defaultTextField.getText()));
            if (isUnique.isSelected()) cmd.append(" UNIQUE");
        }
        if (isPrimaryKey.isSelected()) cmd.append(" PRIMARY KEY");
        CAlterPacket cAlterPacket = new CAlterPacket(Main.user.getUuid(), cmd.toString(), Main.mainLogic.getDataBase4tableView());
        Main.channel2packetsMap.computeIfAbsent(Main.ctx.channel(), c -> new LinkedBlockingQueue<>()).add(cAlterPacket);
    }
}
