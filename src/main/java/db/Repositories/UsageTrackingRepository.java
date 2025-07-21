package db.Repositories;

import Processes.ProcessInfo;
import Processes.UserInfo;
import db.Database;
import org.tinylog.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UsageTrackingRepository {
    private Database db;

    public UsageTrackingRepository(Database database) {
        this.db = database;
    }

    /**
     * Increments the total tracked time for a process by 2 seconds.
     * This operation is performed asynchronously.
     *
     * @param prs The Process to update.
     */
    public void updateUsageTime(ProcessInfo prs) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement(
                    "UPDATE UsageTracking SET TIME=TIME+6 WHERE USER_ID = ? and NAME = ?")) {
                stmt.setInt(1, prs.getUser_id());
                stmt.setString(2, prs.getProcess_name());
                stmt.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Error updating process time: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Checks if a process is already being tracked for usage for a specific user.
     *
     * @param prs The {@link ProcessInfo} object containing process and user details.
     * @return {@code true} if the process is tracked, {@code false} otherwise.
     */
    public boolean isUsageTracked(ProcessInfo prs) {
        try (PreparedStatement checkStmt = db.getCon().prepareStatement(
                "SELECT * FROM UsageTracking WHERE NAME = ? AND USER_ID = ?")) {
            checkStmt.setString(1, prs.getProcess_name());
            checkStmt.setInt(2, prs.getUser_id());
            try (ResultSet rs = checkStmt.executeQuery()) {
                boolean found = rs.next();
                return found;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all usage tracking records for a specific user.
     *
     * @param user The {@link UserInfo} object representing the user.
     * @return A list of {@link ProcessInfo} objects with usage time for each tracked process.
     */
    public ArrayList<ProcessInfo> getUsageTrackingTopTen(UserInfo user) {
        ArrayList<ProcessInfo> resArray = new ArrayList<>();
        try (PreparedStatement checkQuery = db.getCon().prepareStatement(
                "SELECT * FROM UsageTracking WHERE USER_ID = ? AND NOT name = 'svchost.exe' ORDER BY TIME DESC LIMIT 10")) {
            checkQuery.setInt(1, user.getId()-1);
            try (ResultSet rs = checkQuery.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("ID");
                    int userId = rs.getInt("USER_ID");
                    String processName = rs.getString("NAME");
                    int time = rs.getInt("TIME");
                    ProcessInfo processInfo = new ProcessInfo(id, userId, processName, 0, 0);
                    processInfo.setTotal_time(time);
                    resArray.add(processInfo);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving usage tracking: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return resArray;
    }
    /**
     * Retrieves all usage tracking records for a specific user.
     *
     * @param user The {@link UserInfo} object representing the user.
     * @return A list of {@link ProcessInfo} objects with usage time for each tracked process.
     */
    public ArrayList<ProcessInfo> getUsageTracking(UserInfo user) {
        ArrayList<ProcessInfo> resArray = new ArrayList<>();
        try (PreparedStatement checkQuery = db.getCon().prepareStatement(
                "SELECT * FROM UsageTracking WHERE USER_ID = ?")) {
            checkQuery.setInt(1, user.getId()-1);
            try (ResultSet rs = checkQuery.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("ID");
                    int userId = rs.getInt("USER_ID");
                    String processName = rs.getString("NAME");
                    int time = rs.getInt("TIME");
                    ProcessInfo processInfo = new ProcessInfo(id, userId, processName, 0, 0);
                    processInfo.setTotal_time(time);
                    resArray.add(processInfo);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving usage tracking: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return resArray;
    }

    /**
     * Adds a new usage tracking record for a process and user, if not already present.
     * This operation is performed asynchronously.
     *
     * @param prs The {@link ProcessInfo} object containing process and user details.
     */
    public void addUsageTime(ProcessInfo prs) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement insertStmt = db.getCon().prepareStatement(
                    "INSERT INTO UsageTracking (USER_ID, NAME, TIME) VALUES (?, ?, ?)")) {
                insertStmt.setInt(1, prs.getUser_id());
                insertStmt.setString(2, prs.getProcess_name());
                insertStmt.setInt(3, 0);
                insertStmt.executeUpdate();
                Logger.info("Adding usage time for process: " + prs.getProcess_name());
            } catch (SQLException e) {
                if (e.getMessage().contains("UNIQUE") || e.getErrorCode() == 19) {
                } else {
                    Logger.error("Error adding usage time: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
