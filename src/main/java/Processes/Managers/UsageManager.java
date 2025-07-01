package Processes.Managers;

import Processes.UserInfo;
import db.Database;

import java.util.Calendar;

public class UsageManager {
    private final Database db;

    public UsageManager(Database db) {
        this.db = db;
    }
    public void dailyUsage(UserInfo user) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        String date = String.format("%02d-%02d-%04d", currentDay, currentMonth, currentYear);

        db.getUsageTracking(user).forEach(processInfo -> {
            if(processInfo.getProcess_name().equals("svchost.exe")) {

            }
        });
    };
}
