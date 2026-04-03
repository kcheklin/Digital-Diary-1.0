package com.system.fop;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.fxml.FXML;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CreateController {
    @FXML
    private TextField titleField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextArea contentField;
    @FXML
    private Slider moodSlider;
    @FXML
    private Button imageUpload;
    @FXML
    private VBox imageArea;

    private String filePath;
    private String username;
    private File imageFile;

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
    }

    //click the Create button to create and save a new entry under a folder named with the username
    //ensure the user enter the necessary textfield which are title and content
    //set default date and mood for user if user not choose anything
    @FXML
    private void onCreateButtonClick(){
        //System.out.println(username + " Create");

        if (titleField.getText().isEmpty() || contentField.getText().isEmpty()){
            showAlert("Incomplete","Please fill in the required fields.", Alert.AlertType.ERROR);
            return;
        }

        String title = titleField.getText();
        LocalDate date = (datePicker.getValue() != null)? datePicker.getValue() : LocalDate.now();
        String content = contentField.getText();
        double mood = moodSlider.getValue();

        File newImageFile = null;
        if (imageFile != null && imageFile.exists()){
            newImageFile = new File("D:/users/" + username + "/MyDiaryApp/" + imageFile.getName());
            try {
                int counter = 1;
                while (newImageFile.exists()) {
                    String newFileName = imageFile.getName().replaceFirst("(?)(\\.[^\\.]+$)", counter + "$1");
                    newImageFile = new File("D:/users/" + username + "/MyDiaryApp/" + newFileName);
                    counter++;
                }
                Files.copy(imageFile.toPath(), newImageFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to upload and save the image.", Alert.AlertType.ERROR);
            }
        }

        if (filePath == null || filePath.isEmpty()) {
            filePath = new File("D:/users/" + username + "/MyDiaryApp/entries.csv").getAbsolutePath();
            return;
        }

        File file = new File(filePath);
        boolean isNewFile = false;

        String actualContent = escapeCSV(content);

        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                showAlert("Error", "Failed to create directory for the file.", Alert.AlertType.ERROR);
                return;  // Abort further operations
            }

            if (!file.exists()){
                isNewFile = file.createNewFile();
            }

            //save the diary created into csv file
            try (FileWriter writer = new FileWriter(file, true)){
                if (isNewFile){
                    writer.write("Title,Date,Mood,Content,Path\n");
                }
                writer.write(title+","+date+","+mood+","+actualContent+","+(newImageFile == null ? "" : newImageFile.getAbsolutePath())+"\n");
                showAlert("Success","Diary entry is created successfully!", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to write to the file.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error","Diary entry creation failed.",Alert.AlertType.ERROR);
        }

        //jump to Entries scene that display all the previous saved diary
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Entries.fxml"));
            AnchorPane diaryRoot = loader.load();

            EntriesController entriesController = loader.getController();
            entriesController.setUsername(username);
            entriesController.populate();

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ensure proper CSV formatting
    //escaping double quotes by doubling them,enclosing the string in double quotes, replacing newline characters with __NEWLINE__
    private String escapeCSV(String input) {
        input = input.replace("\n", "__NEWLINE__");
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            input = input.replace("\"", "\"\"");
            input = "\"" + input + "\"";
        }
        return input;
    }

    //click Cancel button back to Entries page when the user don't want to create any entry
    @FXML
    private void onCancelButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Entries.fxml"));
            AnchorPane diaryRoot = loader.load();

            EntriesController entriesController = loader.getController();
            entriesController.setUsername(username);
            entriesController.populate();

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("E-Diary");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //click upload image button to upload one picture from local system
    @FXML
    private void onImageUploadClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(imageUpload.getScene().getWindow());

        if (selectedFile != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Image.fxml"));
                AnchorPane imageRoot = loader.load();

                ImageController imageController = loader.getController();
                imageController.setSelectedFile(selectedFile);
                imageController.setCreateController(this);

                Stage imageStage = new Stage();
                imageStage.setTitle("Image Upload");
                imageStage.setScene(new Scene(imageRoot));
                imageStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load image.", Alert.AlertType.ERROR);
            }
        }else{
            showAlert("Error", "No image selected.", Alert.AlertType.ERROR);
        }
    }

    //display image
    public void addImageToVBox(File imageFile) {
        this.imageFile = imageFile;
        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(150);

        imageArea.getChildren().clear();
        imageArea.getChildren().add(imageView);
    }

    //pop up a window to alert the user
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
