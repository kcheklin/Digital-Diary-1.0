package com.system.fop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class TrackMoodController {

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button confirmButton;

    public String filePath;
    public String username;

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
    }

    //called when the Confirm button is clicked
    //check if the start end selected is before the end date selected
    @FXML
    private void onConfirmButtonClick(ActionEvent event) {
        LocalDate startDate = (startDatePicker.getValue() != null)? startDatePicker.getValue() : LocalDate.now();
        LocalDate endDate = (endDatePicker.getValue() != null)? endDatePicker.getValue() : LocalDate.now();

        if (startDate.isAfter(endDate)) {
            showAlert("Error", "Start date cannot be after end date.", Alert.AlertType.ERROR);
            return;
        }

        changeScene(startDate, endDate);
    }

    //change to the MoodChart scene
    private void changeScene(LocalDate startDate, LocalDate endDate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/MoodChart.fxml"));
            Parent root = loader.load();

            MoodChartController chartController = loader.getController();
            chartController.setUsername(username);
            chartController.setDateRange(startDate, endDate);

            Stage stage = (Stage) confirmButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert("Error", "Failed to load the chart scene.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
