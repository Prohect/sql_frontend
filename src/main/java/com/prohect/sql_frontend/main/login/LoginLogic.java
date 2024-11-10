package com.prohect.sql_frontend.main.login;

import com.prohect.sql_frontend.ClientHandlerAdapter;
import com.prohect.sql_frontend.NettyClient;
import com.prohect.sql_frontend.main.Main;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;


public class LoginLogic implements Initializable {

    public static AtomicBoolean logged = new AtomicBoolean(false);
    @FXML
    public Label loginInfo;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label usernameTipLable;
    @FXML
    private VBox vBox;

    public LoginLogic() {
        Main.loginLogic = this;
    }

    private static void login1() throws Exception {
        //new network client
        String serverHost = Main.clientConfig.getServerHost();
        int port = Main.clientConfig.getPort();
        Bootstrap b = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Main.setAndRunNewNettyClient(new NettyClient(serverHost, port, b, workerGroup, new ClientHandlerAdapter(serverHost, port, b, workerGroup)));
    }

    public VBox getvBox() {
        return vBox;
    }

    public Label getLoginInfo() {
        return loginInfo;
    }

    @FXML
    public void login() throws Exception {
        login1();
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    @FXML
    public void tryAutoComplete() {
        String text = getUsernameField().getText();
        var tip = text;
        boolean flag = false;
        if (!text.isBlank()) {
            for (String username : Main.clientConfig.getUsernames()) {
                if (username.startsWith(text)) {
                    tip = username;
                    flag = true;
                    break;
                }
            }
        }
        if (flag)
            usernameTipLable.setText(tip);
        else usernameTipLable.setText("");
    }

    @FXML
    public void autoComplete4Username(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            if (!usernameTipLable.getText().isEmpty()) getUsernameField().setText(usernameTipLable.getText());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getUsernameField().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!Character.isLetterOrDigit(event.getCharacter().charAt(0)))//不是字母或数字
                if (event.getCode() != KeyCode.TAB && event.getCode() != KeyCode.BACK_SPACE)//不是tab或者backspace则无视event
                    event.consume();
        });
    }
}
