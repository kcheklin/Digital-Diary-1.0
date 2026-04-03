package com.system.fop;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportController {
    @FXML
    private DatePicker startDate;
    @FXML
    private ComboBox<String> timeRange;
    @FXML
    private TextField titleSearch;
    @FXML
    private ComboBox<String> moodSelector;
    @FXML
    private Text textField;

    private String username;
    private String filePath;
    private Stage currentStage;

    //initialize the scene by defining the textField and mood
    @FXML
    private void initialize() {
        ObservableList<String> timeRanges = FXCollections.observableArrayList("Daily", "Weekly", "Monthly");
        timeRange.setItems(timeRanges);
        ObservableList<String> moods = FXCollections.observableArrayList("Any", "Very sad", "Sad", "Neutral", "Happy", "Very happy");
        moodSelector.setItems(moods);
        moodSelector.setValue("Any");
    }

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    //check if all fields are filled
    //call exportContent if there exists the entry within selected date range
    @FXML
    public void exportToPdf() {
        String timeRange = this.timeRange.getValue();
        if (timeRange == null) {
            textField.setText("Please select a time range.");
            return;
        }

        LocalDate startDate = this.startDate.getValue();
        if (startDate == null) {
            textField.setText("Please select a start date.");
            return;
        }

        LocalDate endDate = calculateEndDate(startDate, timeRange);
        String titleFilter = titleSearch.getText().trim();
        String moodFilter = moodSelector.getValue();
        List<String[]> filteredEntries = filterEntries(startDate, endDate, titleFilter, moodFilter);

        if (filteredEntries.isEmpty()) {
            textField.setText("No entries found for the selected range.");
            return;
        }

        boolean isSuccess = exportContent(filteredEntries, timeRange, startDate, endDate);
        if (isSuccess && currentStage != null) {
            currentStage.close();
        }
    }

    //calculate the end date based on the selected time range and start date
    private LocalDate calculateEndDate(LocalDate startDate, String timeRange) {
        switch (timeRange) {
            case "Daily":
                return startDate;
            case "Weekly":
                return startDate.plusDays(6);
            case "Monthly":
                return startDate.plusMonths(1).minusDays(1);
            default:
                return null;
        }
    }

    //filter the entries based on specific date, title and mood
    //sort the entries in ascending date
    //return a list that consists of only specific variables
    private List<String[]> filterEntries(LocalDate startDate, LocalDate endDate, String titleFilter, String moodFilter) {
        List<String[]> filteredEntries = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                LocalDate entryDate = LocalDate.parse(columns[1].trim(), formatter);
                boolean isValidDate = !entryDate.isBefore(startDate) && !entryDate.isAfter(endDate);
                boolean isValidTitle = titleFilter.isEmpty() || columns[0].toLowerCase().contains(titleFilter.toLowerCase());
                String entryMood = getMoodText(columns[2].trim());
                boolean isValidMood = moodFilter.equals("Any") || entryMood.equals(moodFilter);

                if (isValidDate && isValidTitle && isValidMood) {
                    filteredEntries.add(columns);
                }
            }

            filteredEntries.sort((entry1, entry2) -> {
                LocalDate date1 = LocalDate.parse(entry1[1].trim(), formatter);
                LocalDate date2 = LocalDate.parse(entry2[1].trim(), formatter);
                return date1.compareTo(date2);
            });
        }catch (IOException e){
            e.printStackTrace();
        }
        return filteredEntries;
    }

    //change the mood data stored in double to text
    private String getMoodText(String mood) {
        switch (mood){
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

    //write the selected entries to a pdf file
    private boolean exportContent(List<String[]> entries, String timeRange, LocalDate startDate, LocalDate endDate) {
        String base = "/Diary";
        String fileExtension = ".pdf";
        int counter = 1;
        File outputFile;
        do{
            outputFile = new File("D:/users/" + username + "/MyDiaryApp", (base + counter + fileExtension));
            counter++;
        }while (outputFile.exists());

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile))) {
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph(timeRange + " Diary Entries"));
            document.add(new Paragraph("From " + startDate + " to " + endDate));
            document.add(new Paragraph(" "));

            int index = 1;
            for (String[] entry : entries) {
                String title = entry[0].trim();
                String date = entry[1].trim();
                String mood = getMoodText(entry[2].trim());
                String content = unescapeCSV(entry[3].trim());
                String imagePath = entry.length > 4 ? entry[4].trim() : "";

                document.add(new Paragraph("Diary NO. " + index));
                document.add(new Paragraph("Title           : " + title));
                document.add(new Paragraph("Date          : " + date));
                document.add(new Paragraph("Mood         : " + mood));
                document.add(new Paragraph("Content     : \n" + content));

                if (!imagePath.isEmpty()){
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()){
                        try{
                            ImageData imageData = ImageDataFactory.create(imagePath);
                            Image image = new Image(imageData);
                            image.setWidth(250);
                            image.setHeight(250);
                            document.add(image);
                            document.add(new Paragraph(" "));
                        }catch (Exception e) {
                            System.out.println("Failed to add image: " + e.getMessage());
                        }
                    }else{
                        System.out.println("Image not found for entry " + title + ": " + imagePath);
                    }
                }
                if (index < entries.size()){
                    document.add(new Paragraph("---------------------------------------------------------------------------------------------------------------------------------"));
                }
                index++;
            }
            document.close();
            showAlert("Success", "PDF exported successfully.", Alert.AlertType.INFORMATION);
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export PDF.", Alert.AlertType.ERROR);
            textField.setText("Error exporting PDF.");
            return false;
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

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
