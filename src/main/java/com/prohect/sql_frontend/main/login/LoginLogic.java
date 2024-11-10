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
import javafx.scene.input.KeyEvent;

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

    public LoginLogic() {
        Main.loginLogic = this;
    }

    private static void login1() throws Exception {
        Main.clientConfig = ClientConfig.readConfig();
        //new network client
        String serverHost = Main.clientConfig.getServerHost();
        int port = Main.clientConfig.getPort();
        Bootstrap b = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Main.setAndRunNewNettyClient(new NettyClient(serverHost, port, b, workerGroup, new ClientHandlerAdapter(serverHost, port, b, workerGroup)));
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
    @SuppressWarnings("unused")
    void autoComplete4Username(KeyEvent event) {//TODO:

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getUsernameField().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!Character.isLetterOrDigit(event.getCharacter().charAt(0))) event.consume();
        });
    }
}
