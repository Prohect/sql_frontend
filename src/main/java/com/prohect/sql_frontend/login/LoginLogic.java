package com.prohect.mysql_frontend.login;

import com.prohect.mysql_frontend.NettyClient;
import com.prohect.mysql_frontend.main.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.concurrent.atomic.AtomicBoolean;


public class LoginLogic {

    public static AtomicBoolean logged = new AtomicBoolean(false);

    public LoginLogic() {
        Main.loginLogic = this;
    }

    @FXML
    private Button loginButton;
    @FXML
    private Label loginInfo;

    @FXML
    public void login(MouseEvent event) throws Exception {
        Main.clientConfig = ClientConfig.readConfig();
        //new network client
        Main.setAndRunNewNettyClient(new NettyClient(Main.clientConfig.getServerIP(), Main.clientConfig.getPort()));
    }

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    public Label getLoginInfo() {
        return loginInfo;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    @FXML
    void autoComplete4Username(KeyEvent event) {

    }

}
