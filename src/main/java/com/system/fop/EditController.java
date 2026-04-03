package com.system.fop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditController {
    @FXML
    private TextArea contentField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Slider moodSlider;
    @FXML
    private TextField titleField;
    @FXML
    private VBox imageArea;
    @FXML
    private Button imageUpload;

    private String filePath;
    private String username;
    private String currentEntry;
    private String imagePath;
    private File imageFile;

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
    }

    //get the current choose of user action
    public void setCurrentEntry(String entry, String filepath){
        this.currentEntry = entry;
        //System.out.println("Selected Entry: " + currentEntry);

        try(BufferedReader br = new BufferedReader(new FileReader(filepath))){
            String line;
            br.readLine();

            while((line = br.readLine())!=null){
                String[] column = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if(column[0].equals(currentEntry)){
                    titleField.setText(column[0]);
                    datePicker.setValue(convertStringToDate(column[1]));
                    moodSlider.setValue(Double.parseDouble(column[2]));
                    contentField.setText(unescapeCSV(column[3]));
                    imagePath = "";
                    if (column.length > 4){
                        imagePath = column[4];
                        displayImage(imagePath);
                    }
                    break;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //display the image back to the VBox of Edit scene
    private void displayImage(String imagePath){
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(150);
                imageArea.getChildren().clear();
                imageArea.getChildren().add(imageView);
            }else{
                System.out.println("Image file does not exist: " + imagePath);
            }
        }
    }

    //User are able to upload the image
    @FXML
    void clickToUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(imageUpload.getScene().getWindow());

        if (selectedFile != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Image.fxml"));
                AnchorPane imageRoot = loader.load();

                ImageController imageController = loader.getController();
                imageController.setSelectedFile(selectedFile);
                imageController.setEditController(this);

                Stage imageStage = new Stage();
                imageStage.setTitle("Image Upload");
                imageStage.setScene(new Scene(imageRoot));
                imageStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load image upload scene.", Alert.AlertType.ERROR);
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    //add image to the diary same as Create
    public void addImageToVBox(File imageFile) {
        this.imageFile = imageFile;
        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(150);
        imageArea.getChildren().clear();
        imageArea.getChildren().add(imageView);
    }

    //back to  Entries scene if the user don't make any change by clicking the Back button
    @FXML
    void clickToBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Entries.fxml"));
            AnchorPane diaryRoot = loader.load();

            EntriesController entryController = loader.getController();
            entryController.setUsername(username);
            //System.out.println("Click Back to entries:  " + username);
            entryController.populate();

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) contentField.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("E-Diary");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //detect the changes make by user and save the changes to the entries.csv
    //the changes is made in the original row without creating a new diary
    @FXML
    void clickToSave(ActionEvent event) {
        //System.out.println(username + " Save");

        if (titleField.getText().isEmpty() || contentField.getText().isEmpty()) {
            showAlert("Incomplete", "Please fill in the required fields.", Alert.AlertType.ERROR);
            return;
        }

        String title = titleField.getText();
        LocalDate date = (datePicker.getValue() != null) ? datePicker.getValue() : LocalDate.now();
        double mood = moodSlider.getValue();
        String content = contentField.getText();

        File newImageFile = null;
        if (imageFile != null) {
            newImageFile = new File("D:/users/" + username + "/MyDiaryApp/" + imageFile.getName());
            try {
                int counter = 1;
                while (newImageFile.exists()) {
                    String newFileName = imageFile.getName().replaceFirst("(?)(\\.[^\\.]+$)", counter + "$1");
                    newImageFile = new File("D:/users/" + username + "/MyDiaryApp/" + newFileName);
                    counter++;
                }

                if (imagePath != null && Files.exists(Paths.get(imagePath))) {
                    deleteImage(imagePath);
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
        boolean isModified = false;

        String actualContent = escapeCSV(content);

        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            showAlert("Error", "Failed to create directory for the file.", Alert.AlertType.ERROR);
            return;  // Abort further operations
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to read the file.", Alert.AlertType.ERROR);
            return;
        }

        // Find the row to update
        boolean headerProcessed = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] columns = line.split(",");

            if (columns[0].equals(currentEntry)) {
                // Update the row with new values
                lines.set(i, title + "," + date + "," + mood + "," + actualContent + "," + (imageFile == null ? imagePath : newImageFile.getAbsolutePath()));
                isModified = true;
                break;
            }
        }

        // If the entry was not found, show an error and return
        if (!isModified) {
            showAlert("Error", "Entry not found to update.", Alert.AlertType.ERROR);
            return;
        }

        // Now write the updated content back to the file
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to write to the file.", Alert.AlertType.ERROR);
        }

        showAlert("Success", "Diary entry updated successfully!", Alert.AlertType.INFORMATION);

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

    private static void deleteImage(String imagePath) {
        Path imageFile = Paths.get(imagePath);

        if (Files.exists(imageFile)) {
            try {
                Files.delete(imageFile);
            } catch (IOException e) {
                System.out.println("Error deleting image: " + imageFile);
                e.printStackTrace();
            }
        } else {
            System.out.println("Image not found: " + imageFile);
        }
    }

    //ensure the correct CSV formatting
    private String escapeCSV(String input) {
        input = input.replace("\n", "__NEWLINE__");
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            input = input.replace("\"", "\"\"");
            input = "\"" + input + "\"";
        }
        return input;
    }

    private String unescapeCSV(String input) {
        input = input.replace("__NEWLINE__", "\n");
        if (input.startsWith("\"") && input.endsWith("\"")) {
            input = input.substring(1, input.length() - 1);
            input = input.replace("\"\"", "\"");
        }
        return input;
    }

    //proper handle the date display
    private LocalDate convertStringToDate(String dateString) {
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
