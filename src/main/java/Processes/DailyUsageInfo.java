package Processes;

import java.time.LocalDate;
import java.util.Date;

public class DailyUsageInfo {
    int userId;
    private LocalDate date;
    private int timeSpent;

    public DailyUsageInfo(int userId, LocalDate date, int timeSpent) {
        this(date,timeSpent);
        this.userId = userId;
    }
    public DailyUsageInfo(int userId, String date, int timeSpent) {
        this(date,timeSpent);
        this.userId = userId;
    }
    public DailyUsageInfo(String date, int timeSpent) {
        this.date = LocalDate.parse(date);
        this.timeSpent = timeSpent;
    }
    public DailyUsageInfo(LocalDate date, int timeSpent) {
        this.date = date;
        this.timeSpent = timeSpent;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }
}
