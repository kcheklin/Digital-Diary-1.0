package com.system.fop;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeleteController {
    @FXML
    private Button restoreButton;
    @FXML
    private Button backButton;
    @FXML
    private ListView<String> resultsListView;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String username;
    private File entriesfilepath;
    private File binfilepath;
    private String currententry;

    public void setUsername(String username) {
        this.username = username;
        this.entriesfilepath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv");
        this.binfilepath = new File("D:/users/" + this.username + "/MyDiaryApp/bin.csv");
    }

    //click Back button to back to Entries scene and display all the saved diary
    @FXML
    private void clickToBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/system/fop/Entries.fxml"));
            AnchorPane diaryRoot = loader.load();
            EntriesController entryController = loader.getController();
            entryController.setUsername(username);
            entryController.populate();
            Scene diaryScene = new Scene(diaryRoot);

            Stage stage = (Stage) restoreButton.getScene().getWindow();
            stage.setScene(diaryScene);
            stage.setTitle("E-Diary");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //click Restore button and restore the deleted diary in the bin.csv back to entries.csv
    @FXML
    private void clickToRestore(ActionEvent event) {
        String selectedEntry = currententry;
        String[] parts = selectedEntry.split("\n");
        String title = parts[1].substring(7).trim(); // Extract the title starting from "Title: "

        if (title != null && currententry != null) {
            moveToEntries(title);
            updateBinCsv(title);
            resultsListView.getItems().remove(currententry);
            showAlert("Success","Entry moved to the entry.",Alert.AlertType.INFORMATION);

            resultsListView.getSelectionModel().clearSelection();
        }
    }

    //write back the selected entry back to entries.csv properly
    private void moveToEntries(String currentTitle) {
        String line, title = "", date = "", mood = "", content = "", imagePath = "";

        try (BufferedReader binReader = new BufferedReader(new FileReader(binfilepath))) {
            binReader.readLine();  // Skip the header row
            while ((line = binReader.readLine()) != null) {
                String[] column = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Split by comma, allowing empty fields

//                // Remove quotes around each field
//                for (int i = 0; i < column.length; i++) {
//                    column[i] = column[i].replace("\"", "").trim(); // Remove extra quotes
//                }

                // Ensure there are enough columns and the title matches
                if (column[0].equals(currentTitle)) {
                    title = column[0].trim();
                    date = column[1].trim();
                    mood = column[2].trim();
                    content = unescapeCSV(column[3]);
                    imagePath = column.length > 4 ? column[4].trim() : "";
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If a valid entry was found, restore it to entries.csv
        if (!title.isEmpty() && !date.isEmpty() && !mood.isEmpty() && !content.isEmpty()) {
            try (BufferedWriter entriesWriter = new BufferedWriter(new FileWriter(entriesfilepath, true))) {
                if (entriesfilepath.length() == 0) {
                    entriesWriter.write("Title,Date,Mood,Content,ImagePath");
                    entriesWriter.newLine();
                }
                entriesWriter.write(title + "," + date + "," + mood + "," + escapeCSV(content) + "," + imagePath);
                entriesWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to restore entry. Missing data.");
        }
    }

    //rewrite the bin.csv by removing the restored diary
    private void updateBinCsv(String title) {
        List<String> entries = getAllBinDiary();  // Get all entries from the bin

        // Remove the entry with the selected title
        entries.removeIf(entry -> entry.split(",")[0].equals(title));

        // Write the updated list back to the bin file
        try (BufferedWriter binWriter = new BufferedWriter(new FileWriter(binfilepath))) {
            binWriter.write("Title,Date,Mood,Content,ImagePath");
            binWriter.newLine();
            for (String entry : entries) {
                binWriter.write(entry);
                binWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //keep all rows in bin.csv file into a List for comparison purpose
    private List<String> getAllBinDiary() {
        List<String> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(binfilepath))) {
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

    //display list view
    public void populate() {
        List<String> deletedEntries = getDeletedEntries();

        resultsListView.getItems().setAll(deletedEntries);
        resultsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                String[] parts = item.split("\nImage: ", 2);
                String text = parts[0];
                setText(text);

                if (parts.length > 1) {
                    String imagePath = parts[1];
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        ImageView imageView = new ImageView(new Image(imageFile.toURI().toString()));
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        setGraphic(imageView);
                    }
                }
            }
            }
        });

        resultsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                currententry= newValue;
            }
        });
        // Schedule the cleanup task to run every day (24 hours)
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanupOldEntries, 0, 24, TimeUnit.HOURS);
    }

    // Search for entries in the CSV file matching the keyword
    private List<String> getDeletedEntries() {
        List<String> entries = new ArrayList<>();
        //Refresh immediately after the entry pass 30 days is deleted
        cleanupOldEntries();

        if (!binfilepath.exists()) {
            showAlert("Error", "bin.csv file not found.", Alert.AlertType.ERROR);
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(binfilepath))) {
            String line;
            reader.readLine(); // Skip the header line

            while ((line = reader.readLine()) != null) {
                String[] entryDetails = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle empty fields
                String title = entryDetails[0];
                String date = entryDetails[1];
                String mood = getMoodText(entryDetails[2]);
                String content = unescapeCSV(entryDetails[3]);
                String image = entryDetails.length > 4 ? entryDetails[4] : "";

                String result = String.format("Date: %s\nTitle: %s\nMood: %s\nContent: %s", date, title, mood, unescapeCSV(content));
                if (!image.isEmpty()) {
                    result += String.format("\nImage: %s", image);
                }
                entries.add(result);

            }
        } catch (IOException e) {
            showAlert("Error", "Failed to read the deleted entries.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }

        return entries;
    }

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

    // Utility method to show alert dialogs
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Cleanup method to delete entries older than 30 days from bin.csv
    private void cleanupOldEntries() {
        List<String> remainingEntries = new ArrayList<>();  // To hold deleted entries within 30 days

        try (BufferedReader reader = new BufferedReader(new FileReader(binfilepath))) {
            String line;
            reader.readLine();  // Skip header row
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // Date format for parsing deletion date
            Date currentDate = new Date();  // Current date to calculate the difference

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                String imagePath = columns[4];
                String deletionDateStr = columns[5];  // Deletion date column
                Date deletionDate = sdf.parse(deletionDateStr);  // Parse the deletion date

                // Calculate the difference between the current date and the deletion date
                long diffInMillis = currentDate.getTime() - deletionDate.getTime();
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);  // Convert milliseconds to days

                if (diffInDays <= 30) {
                    remainingEntries.add(line);  // Keep this entry (it's within the 30-day range)
                }else{
                    if(imagePath != "") {
                        deleteImage(imagePath);//Delete entry pass 30-day range) & it's image
                    }
                }
            }
        } catch (IOException | java.text.ParseException e) {
            e.printStackTrace();
        }

        // Rewrite the bin.csv file with the remaining entries
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(binfilepath))) {
            writer.write("Title, Date, Mood, Content, ImagePath, DeletionDate");
            writer.newLine();

            for (String entry : remainingEntries) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteImage(String imagePath) {
        Path imageFile = Paths.get(imagePath);

        // Check if the image exists and delete it
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

    private String escapeCSV(String input) {
        input = input.replace("\n", "__NEWLINE__");
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            input = input.replace("\"", "\"\"");
            input = "\"" + input + "\"";
        }
        return input;
    }

    private String unescapeCSV(String input) {
        if (input == null) {
            return "";
        }

        // Revert __NEWLINE__ back to actual newlines
        input = input.replace("__NEWLINE__", "\n");

        // If the input starts and ends with double quotes, remove them
        if (input.startsWith("\"") && input.endsWith("\"")) {
            input = input.substring(1, input.length() - 1);  // Remove the surrounding quotes
            input = input.replace("\"\"", "\"");  // Unescape double quotes
        }

        return input;
    }
}