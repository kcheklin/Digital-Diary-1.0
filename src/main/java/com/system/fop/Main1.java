package com.system.fop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Main1 extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader welcomeLoader = new FXMLLoader(getClass().getResource("/com/system/fop/Welcome.fxml"));
            AnchorPane welcomePage = welcomeLoader.load();

            Scene welcomeScene = new Scene(welcomePage);
            primaryStage.setScene(welcomeScene);
            primaryStage.setTitle("E-Diary");

            primaryStage.show();

            WelcomeController welcomeController = welcomeLoader.getController();
            welcomeController.setMainStage(primaryStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
