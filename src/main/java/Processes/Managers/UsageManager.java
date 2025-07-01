package Processes.Managers;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import Processes.DailyUsageInfo;
import Processes.ProcessInfo;
import Processes.UserInfo;
import db.Database;
import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

public class UsageManager {
    private final Database db;

    public UsageManager(Database db) {
        this.db = db;
    }
    /**
     * Checks and adds information about daily usage
     *
     * @param user information about the current user
     */
    public void dailyUsage(UserInfo user) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        String date = String.format("%04d-%02d-%02d", currentYear, currentMonth, currentDay);

        var dailyUsages = db.getDailyUsage(user);
        boolean hasToday = dailyUsages.stream().anyMatch(info -> info.getDate().toString().equals(date));
        System.out.println(hasToday);
        if (!hasToday) {
            int svchostTotal = 0;
            ArrayList<ProcessInfo> pr = db.getUsageTracking(user);
            for(ProcessInfo prs : pr) {
                if (prs.getProcess_name().contains("svchost.exe")) {
                    svchostTotal = prs.getTotal_time();
                    break;
                }
            }

            int previousTotal = dailyUsages.stream().mapToInt(info -> info.getTimeSpent()).sum();
            int todayTime = svchostTotal - previousTotal;
            if (todayTime < 0) {
                todayTime = 0;
            }

            db.addDailyUsage(new DailyUsageInfo(user.getId(), date, todayTime));
        }
    };
    /**
     * Shows daily usage data on a line chart.
     * Each point is a day, and the value is hours used.
     *
     * @param chart The LineChart to display the data on.
     * @param dailyUsages The list of daily usage info (date and seconds used).
     */
    public static void displayDailyUsage(LineChart chart, ArrayList<DailyUsageInfo> dailyUsages) {
        chart.getData().clear();
        XYChart.Series series = new XYChart.Series();
        series.setName("Daily Usage (hours)");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        dailyUsages.sort(Comparator.comparing(DailyUsageInfo::getDate));
        ArrayList<String> allDates = new ArrayList<>();
        for (DailyUsageInfo info : dailyUsages) {
            String dateLabel = info.getDate().format(formatter);
            double hours = info.getTimeSpent() / 3600.0;
            series.getData().add(new XYChart.Data(dateLabel, hours));
            allDates.add(dateLabel);
        }
        chart.getData().add(series);
        CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();
        xAxis.setCategories(FXCollections.observableArrayList(allDates));
        chart.getXAxis().setTickLabelRotation(45);
    }
}
