package com.system.fop;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageController {
    @FXML
    private Button confirmButton;

    @FXML
    private ImageView imageView;

    private File selectedFile;
    private CreateController createController;
    private EditController editController;

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        loadImage();
    }

    public void setCreateController(CreateController createController) {
        this.createController = createController;
    }

    public void setEditController(EditController editController) {
        this.editController = editController;
    }

    // load an image from a selected file and display it in an ImageView
    public void loadImage() {
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image);
            }catch (Exception e) {
                showAlert("Error", "Failed to load image.", Alert.AlertType.ERROR);
            }
        }else{
            showAlert("Error", "No file selected.", Alert.AlertType.ERROR);
        }
    }

    //click Confirm button to close the current window
    //adds the selected image file to a VBox of Edit/Create Scene
    @FXML
    private void onConfirmButtonClick(){
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();

        if (createController != null) {
            createController.addImageToVBox(selectedFile);
        }

        if (editController != null) {
            editController.addImageToVBox(selectedFile);
        }
    }

    //pop up a small window to alert the user
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
