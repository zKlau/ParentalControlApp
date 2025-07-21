package db.Repositories;

import Processes.DailyUsageInfo;
import Processes.ProcessInfo;
import Processes.UserInfo;
import db.Database;
import org.tinylog.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DailyUsageRepository {
    private Database db;

    public DailyUsageRepository(Database database) {
        this.db = database;
    }

    /**
     * Adds a new daily usage record to the database.
     * This operation is performed asynchronously.
     *
     * @param info The {@link DailyUsageInfo} object containing daily usage details.
     */
    public synchronized void addDailyUsage(DailyUsageInfo info) {
        db.executeDatabaseTask(() -> {
            // User ID, Date, Time
            try (PreparedStatement stmt = db.getCon().prepareStatement("INSERT INTO DailyUsage(USER_ID,DATE,USAGE_SECONDS) VALUES(?,?,?)")) {
                stmt.setInt(1,info.getUserId());
                stmt.setString(2,info.getDate().toString());
                stmt.setInt(3,info.getTimeSpent());
                stmt.executeUpdate();
            } catch(SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieves all usage tracking records for a specific user.
     *
     * @param user The {@link UserInfo} object representing the user.
     * @return A list of {@link ProcessInfo} objects with usage time for each tracked process.
     */
    public ArrayList<DailyUsageInfo> getDailyUsage(UserInfo user) {
        ArrayList<DailyUsageInfo> resArray = new ArrayList<>();
        try (PreparedStatement checkQuery = db.getCon().prepareStatement(
                "SELECT * FROM DailyUsage WHERE USER_ID = ?")) {
            checkQuery.setInt(1, user.getId());
            try (ResultSet rs = checkQuery.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("DATE");
                    int time = rs.getInt("USAGE_SECONDS");

                    DailyUsageInfo dailyInfo = new DailyUsageInfo(date, time);
                    resArray.add(dailyInfo);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving usage tracking: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return resArray;
    }
}
