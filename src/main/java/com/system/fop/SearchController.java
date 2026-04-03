package com.system.fop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchController {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> resultsListView;

    public String username;
    public String filePath;

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
    }

    //called when Search button is clicked
    //called the searchEntries method and search for the entries based on the input keyword
    //display the entries filtered on the listView
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            showAlert("Error", "Please enter a keyword to search.", Alert.AlertType.ERROR);
            return;
        }

        List<String> results = searchEntries(keyword);

        if (results.isEmpty()) {
            results.add("No matching entries found.");
        }

        resultsListView.getItems().setAll(results);
        resultsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                }else{
                    String[] parts = item.split("\nImage: ", 2);
                    String text = parts[0];
                    setText(text);

                    if (parts.length > 1) {
                        String imagePath = parts[1];
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()){
                            ImageView imageView = new ImageView(new Image(imageFile.toURI().toString()));
                            imageView.setFitHeight(50);
                            imageView.setFitWidth(50);
                            setGraphic(imageView);
                        }else{
                            showAlert("Error", "Image file not found.", Alert.AlertType.ERROR);
                        }
                    }
                }
            }
        });
    }

    // Search for entries in the CSV file matching the keyword
    private List<String> searchEntries(String keyword) {
        List<String> results = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            showAlert("Error", "Diary entries file not found.", Alert.AlertType.ERROR);
            return results;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] entryDetails = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle empty fields
                String title = entryDetails[0];
                String date = entryDetails[1];
                String mood = getMoodText(entryDetails[2]);
                String content = unescapeCSV(entryDetails[3]);
                String image = entryDetails.length > 4 ? entryDetails[4] : "";

                if (title.toLowerCase().contains(keyword.toLowerCase()) || content.toLowerCase().contains(keyword.toLowerCase())) {
                    String result = String.format("Date: %s\nTitle: %s\nMood: %s\nContent: %s", date, title, mood, content);
                    if (!image.isEmpty()) {
                        result += String.format("\nImage: %s", image);
                    }
                    results.add(result);
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to read the diary entries.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        return results;
    }

    //change the mood from double to readable text
    private String getMoodText(String mood) {
        switch (mood) {
            case "0.0":
                return "Very sad";
            case "1.0":
                return "Sad";
            case "2.0":
                return "Neutral";
            case "3.0":
                return "Happy";
            case "4.0":
                return "Very happy";
            default:
                return "";
        }
    }

    //called when Back button is clicked
    //jump back to the Entries page
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Entries.fxml"));
            AnchorPane diaryRoot = loader.load();

            EntriesController entryController = loader.getController();
            entryController.setUsername(username);

            //System.out.println("Click back to entries:  " + username);
            entryController.populate();

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("E-Diary");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String unescapeCSV(String input) {
        input = input.replace("__NEWLINE__", "\n");
        if (input.startsWith("\"") && input.endsWith("\"")) {
            input = input.substring(1, input.length() - 1);
            input = input.replace("\"\"", "\"");
        }
        return input;
    }

    // Utility method to show alert dialogs
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
