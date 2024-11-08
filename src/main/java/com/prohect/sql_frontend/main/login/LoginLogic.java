package com.prohect.sql_frontend.main.login;

import com.prohect.sql_frontend.ClientHandlerAdapter;
import com.prohect.sql_frontend.NettyClient;
import com.prohect.sql_frontend.main.Main;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.prohect.sql_frontend.LoginUi.window;


public class LoginLogic {

    public static AtomicBoolean logged = new AtomicBoolean(false);
    @FXML
    private Button loginButton;
    @FXML
    private Label loginInfo;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private MediaView mediaView;

    @FXML
    public void initialize() {
        URL resourceUrl = getClass().getResource("/com/prohect/gui/background.mp4");
        if (resourceUrl == null) {
            throw new RuntimeException("无法找到资源文件");
        }
        Media media = new Media(resourceUrl.toString());
        mediaView.setPreserveRatio(false);
        mediaView.setFitWidth(1280);
        mediaView.setFitHeight(720);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    }

    public LoginLogic() {
        Main.loginLogic = this;
    }

    @FXML
    public void login(MouseEvent event) throws Exception {
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
