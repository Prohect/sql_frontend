package com.prohect.sqlFrontend;

import com.prohect.sqlFrontend.main.Main;
import com.prohect.sqlFrontend.main.login.ClientConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainUi extends Application {


    public static FXMLLoader loginFXMLLoader = new FXMLLoader(MainUi.class.getResource("login-view.fxml"));
    public static FXMLLoader mainFXMLLoader = new FXMLLoader(MainUi.class.getResource("main-view.fxml"));
    public static Scene loginScene;
    public static Scene mainScene;
    private static Stage window;

    static {
        try {
            loginScene = new Scene(loginFXMLLoader.load(), 322, 375);
            mainScene = new Scene(mainFXMLLoader.load(), 1280, 720);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stage getWindow() {
        return window;
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Main.clientConfig = ClientConfig.readConfig();
        window = stage;
        stage.setTitle("登录!");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        if (Main.clientConfig != null) {
            Main.clientConfig.setLastDB(Main.mainLogic.getCurrentDataBaseName());
            Main.clientConfig.setLastTB(Main.mainLogic.getCurrentTableName());
            ClientConfig.saveConfig(Main.clientConfig);
        }
        if (Main.client != null) Main.client.close();
        for (int i = 0; i < NettyClient.threadGroups.size(); i++) {
            if (NettyClient.threadGroups.get(i) != null) NettyClient.threadGroups.get(i).shutdownGracefully();
        }
        Platform.exit();
    }


}