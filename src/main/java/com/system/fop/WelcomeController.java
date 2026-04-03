package com.system.fop;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class WelcomeController {
    private Stage mainStage;

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @FXML
    private Button exitButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    //called when Login button is clicked
    //jump to Login page
    @FXML
    void clickToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Login.fxml"));
            Parent loginRoot = loader.load();
            Scene loginScene = new Scene(loginRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("E-Diary Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //called when the Register button is pressed
    //jump to the Register page
    @FXML
    void clickToRegister(ActionEvent event) {
        try {
            // Load the RegisterPage.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Register.fxml"));
            Parent registerRoot = loader.load();
            Scene registerScene = new Scene(registerRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(registerScene);
            stage.setTitle("E-Diary Register");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //called when Exit button is pressed
    //exit the program
    @FXML
    void clickToExit(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
}
