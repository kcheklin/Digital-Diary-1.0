package com.system.fop;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MoodChartController {

    @FXML
    private LineChart<String, Number> moodLineChart;
    @FXML
    private NumberAxis yAxis;

    public String filePath;
    public String username;
    private LocalDate startDate;
    private LocalDate endDate;

    //initialize the y-axis
    @FXML
    private void initialize() {
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(5);
        yAxis.setTickUnit(1);
        yAxis.setAutoRanging(false);
        yAxis.setMinorTickCount(0);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                switch (object.intValue()) {
                    case 0:
                        return "Very Sad";
                    case 1:
                        return "Sad";
                    case 2:
                        return "Neutral";
                    case 3:
                        return "Happy";
                    case 4:
                        return "Very Happy";
                    default:
                        return "";
                }
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
        this.filePath = new File("D:/users/" + this.username + "/MyDiaryApp/entries.csv").getAbsolutePath();
    }

    //calculate the total days within the selected date and print as title
    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        moodLineChart.setTitle("Mood Tracker : Last " + ChronoUnit.DAYS.between(this.startDate, this.endDate) + " days");
        displayMoodChart();
    }

    //calculate the mean for mood if there is more than one entry in a same day
    //sort the date in ascending order
    //display the mood over time using line chart
    private void displayMoodChart() {
        List<String[]> moodEntries = readMoodData(filePath);
        List<String[]> filteredEntries = filterEntries(moodEntries);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (filteredEntries.isEmpty()) {
            series.setName("No data available for the selected date range.");
            moodLineChart.getData().clear();
            moodLineChart.getData().add(series);
            return;
        }

        series.setName("Mood Over Time");
        filteredEntries.sort((entry1, entry2) -> {
            LocalDate date1 = LocalDate.parse(entry1[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate date2 = LocalDate.parse(entry2[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date1.compareTo(date2);
        });

        for (int i = 0; i < filteredEntries.size(); i++) {
            String date = filteredEntries.get(i)[0];
            double totalMood = 0.0;
            int count = 0;
            for (int j = 0; j < filteredEntries.size(); j++) {
                if (date.equals(filteredEntries.get(j)[0])) {
                    totalMood += Double.parseDouble(filteredEntries.get(j)[1]);
                    count++;
                }
            }
            int meanMood = (int) (totalMood / count);
            series.getData().add(new XYChart.Data<>(date, meanMood));
        }
        moodLineChart.getData().clear();
        moodLineChart.getData().add(series);
    }

    //read the entries from the entries file and return a list that consists of date and mood only
    private List<String[]> readMoodData(String filePath) {
        List<String[]> moodEntries = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return moodEntries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String date = parts[1];
                String mood = parts[2];
                moodEntries.add(new String[]{date, mood});
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return moodEntries;
    }

    //filtered the list based on the selected date
    private List<String[]> filterEntries(List<String[]> moodEntries) {
        List<String[]> filteredEntries = new ArrayList<>();
        for (String[] entry : moodEntries) {
            LocalDate entryDate = LocalDate.parse(entry[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }
}
