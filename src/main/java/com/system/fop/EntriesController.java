package com.system.fop;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class EntriesController{
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button trackMoodButton;
    @FXML
    private ListView<String> listView;

    private String username;
    private String filePath;
    private File binfilepath;
    String currentChoose;
    private boolean isDeleting = false;

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
        this.binfilepath = new File("D:/users/" + this.username + "/MyDiaryApp/bin.csv");
    }

    //click and jump to Create scene to create new diary
    @FXML
    void clickToCreateNew(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Create.fxml"));
            AnchorPane diaryRoot = loader.load();
            CreateController createController = loader.getController();
            createController.setUsername(username);
            Scene diaryScene = new Scene(diaryRoot);

            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("Create New Entry");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //click Edit button to edit the selected previous diary
    @FXML
    void clickToEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Edit.fxml"));
            AnchorPane diaryRoot = loader.load();

            EditController editController = loader.getController();
            editController.setUsername(this.username);
            editController.setCurrentEntry(currentChoose,filePath);

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("Edit Entry");
            stage.setWidth(614);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //is called in the LoginController before Entries scene to ensure the ListView is displaying all previous diary when enter Entries scene
    public void populate() {
        if (filePath == null || filePath.isEmpty()) {
            filePath = new File("D:/users/" + username + "/MyDiaryApp/entries.csv").getAbsolutePath();
        }

        List<String> entries = getDiaryEntries();
        listView.getItems().addAll(entries);

        //detect the current choose of user in the ListView
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                currentChoose = newValue;
                //System.out.println("Current choose: " + currentChoose);
            }
        });
    }

    //get all diary entries in entries.csv file
    private List<String> getDiaryEntries() {
        List<String> entries = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            showAlert("File: " + filePath);
            return entries;
        }

        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            br.readLine();
            String line;
            while((line = br.readLine())!=null){
                String[] column = line.split(",");
                if (column.length > 0) {
                    String title = column[0].trim();
                    //System.out.println("Title: " + title);
                    entries.add(title);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return entries;
    }

    //close the application when click
    @FXML
    void clickToExit(ActionEvent event) {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    //click to export the selected entries into pdf file
    @FXML
    private void clickToExport(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Export.fxml"));
            AnchorPane exportRoot = loader.load();

            Scene scene = new Scene(exportRoot);
            Stage stage = new Stage();
            stage.setTitle("Export to PDF");
            stage.setScene(scene);
            stage.show();

            ExportController exportController = loader.getController();
            exportController.setUsername(this.username);
            exportController.setCurrentStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //click search button and enter the keyword, the ListView will display all the entries contain the keyword
    @FXML
    private void clickToSearch(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Search.fxml"));
            AnchorPane diaryRoot = loader.load();

            SearchController searchController = loader.getController();
            searchController.setUsername(this.username);

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("Search Entries");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //click the TrackMood button and jump to the Track Mood scene
    @FXML
    private void clickToTrackMood(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/TrackMood.fxml"));
            AnchorPane root = loader.load();

            TrackMoodController trackMoodController = loader.getController();
            trackMoodController.setUsername(this.username);

            Stage stage = new Stage();
            stage.setTitle("Mood Tracker");
            stage.setScene(new Scene(root));
            stage.show();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //when click the delete button, the selected entry is deleted from the ListView of Entries scene
    //jump to the recycle bin scene
    @FXML
    void clickToDelete(ActionEvent event) {
        isDeleting = true;
        boolean isNewFile = false;

        try {
            if (!binfilepath.getParentFile().exists() && !binfilepath.getParentFile().mkdirs()) {
                showAlert("Failed to create directory for the file.");
                return;  // Abort further operations
            }
            if (!binfilepath.exists()){
                isNewFile = binfilepath.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (currentChoose != null) {
            moveToBin(currentChoose);
            //System.out.println("BUTTON: " + currentChoose);
            updateEntriesCsv();
            listView.getItems().remove(currentChoose);
            showAlert("Entry moved to the bin.");

            listView.getSelectionModel().clearSelection();
            currentChoose = null;
        }
        isDeleting = false;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Delete.fxml"));
            AnchorPane diaryRoot = loader.load();
            DeleteController deleteController = loader.getController();
            deleteController.setUsername(username);
            deleteController.populate();

            Scene diaryScene = new Scene(diaryRoot);
            Stage stage = (Stage) deleteButton.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();  // Handle the exception if loading the new scene fails
        }
    }

    //rewrite the entries.csv file to remove the deleted diary
    private void updateEntriesCsv() {
        List<String> entries = getAllDiary(); // Get all entries from the CSV
        // Assuming currentChoose holds the selected title to be removed
        String titleToDelete = currentChoose;
        //System.out.println("TITLE: " + titleToDelete);

        // Find and remove the entry that matches the selected title
        entries.removeIf(entry -> entry.split(",")[0].equals(titleToDelete));  // Assuming title is the first column

        // Write the remaining entries back to the CSV file
        try (BufferedWriter entriesWriter = new BufferedWriter(new FileWriter(filePath))) {
            entriesWriter.write("Title, Date, Mood, Content, ImagePath");
            entriesWriter.newLine();
            for (String entry : entries) {
                entriesWriter.write(entry);
                entriesWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //keep all diaries in the entries.csv file for the comparison purpose
    private List<String> getAllDiary() {
        List<String> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); //skip header
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    //write the selected/ deleted diary into bin.csv file and keep for 30 days before permanently deleted
    private void moveToBin(String entry) {
        String line, content = "", title="", date="", imagePath="", mood = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String deletionDate = sdf.format(new Date());

        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            br.readLine();
            while((line = br.readLine())!=null){
                String[] column = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if(column[0].equals(currentChoose)){
                    title = column[0];
                    date = column[1];
                    mood = column[2];
                    content = unescapeCSV(column[3]);

                    if (column.length > 4){
                        imagePath = column[4];
                    }else{
                        imagePath = "";
                    }
                    break;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        title = escapeCSV(title);
        content = escapeCSV(content);
        imagePath = escapeCSV(imagePath);

        try (BufferedWriter binWriter = new BufferedWriter(new FileWriter(binfilepath, true))) {
            if(binfilepath.length() ==0){
                binWriter.write("Title, Date, Mood, Content, ImagePath, DeletionDate");
                binWriter.newLine();
            }
            binWriter.write(title + "," + date + "," + mood + "," + escapeCSV(content) + "," + imagePath + "," + deletionDate);
            binWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //handle the CSV format
    private String escapeCSV(String input) {
        if (input == null) {
            return "";
        }
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

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }
}