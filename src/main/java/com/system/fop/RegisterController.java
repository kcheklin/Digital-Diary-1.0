package com.system.fop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class RegisterController {
    @FXML
    private Button buttonBack;
    @FXML
    private Button buttonRegister;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField usernameField;
    private static String USER_FILE = "users.csv";

    //called when Back button is clicked
    //return to the Welcome page
    @FXML
    void clickBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Welcome.fxml"));
            Parent mainPageRoot = loader.load();
            Scene mainPageScene = new Scene(mainPageRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(mainPageScene);
            stage.setTitle("E-Diary");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //called when Register button is clicked
    //check if all fields are filled then proceed to registration
    @FXML
    void clickRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }

        registerUser(username, email, password);
    }

    //register a new user by saving the details to the user file
    //call the isUserExits to check whether user already exits
    //jump to Create page after successful registration
    private void registerUser(String userName, String email, String password) {
        if (isUserExists(userName, email)) {
            showAlert("Error", "Username or email already exists!", Alert.AlertType.ERROR);
            return;
        }

        try (FileWriter writer = new FileWriter(USER_FILE, true)) {
            writer.write(userName + "," + email + "," + password + "\n");
            showAlert("Success", "Registration successful! Welcome, " + userName,Alert.AlertType.INFORMATION);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Create.fxml"));
            AnchorPane diaryRoot = loader.load();

            CreateController createController = loader.getController();
            createController.setUsername(userName);
            
            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("E-Diary");
            stage.setScene(diaryScene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Unable to save user data.",Alert.AlertType.ERROR);
            e.printStackTrace();
        }

        setFileDirectory(userName);
    }

    //set a fixed directory for each user to store their data
    public void setFileDirectory(String username){
        File directory = new File("D:/users/" + username + "/MyDiaryApp");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    //check if the username or email already exists
    //return true if exists, false otherwise
    private boolean isUserExists(String userName, String email) {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile(); // Create the file if it doesn't exist
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Unable to create user file.", Alert.AlertType.ERROR);
            }
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userDetails = line.split(",");
                if (userDetails[0].equals(userName) || userDetails[1].equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
