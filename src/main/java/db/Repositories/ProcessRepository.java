package db.Repositories;

import Processes.ProcessInfo;
import Processes.UserInfo;
import db.Database;
import org.tinylog.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProcessRepository {
    private Database db;

    public ProcessRepository(Database database) {
        this.db = database;
    }

    /**
     * Increments the total tracked time for a process by 2 sedb.getCon()ds.
     * This operation is performed asynchronously.
     *
     * @param process_id The ID of the process to update.
     */
    public void updateTime(int process_id) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement(
                    "UPDATE Processes SET TOTAL_TIME=TOTAL_TIME+2 WHERE ID = ?")) {
                stmt.setInt(1, process_id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Error updating process time: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sets or updates the time limit for a given process.
     * If a time limit already exists, it is updated; otherwise, a new record is inserted.
     * This operation is performed asynchronously.
     *
     * @param prs The {@link ProcessInfo} object db.getCon()taining process ID and time limit.
     */
    public void setTimeLimit(ProcessInfo prs) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement checkStmt = db.getCon().prepareStatement(
                    "Select * from Timelimits  WHERE PROCESS_ID= ?")) {
                checkStmt.setInt(1, prs.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = db.getCon().prepareStatement(
                                "UPDATE Timelimits SET TIME_LIMIT = ? WHERE PROCESS_ID= ?")) {
                            updateStmt.setInt(1, prs.getTime_limit());
                            updateStmt.setInt(2, prs.getId());
                            updateStmt.executeUpdate();
                            return;
                        }
                    }
                }
                try (PreparedStatement insertStmt = db.getCon().prepareStatement(
                        "INSERT INTO Timelimits (PROCESS_ID, TIME_LIMIT) VALUES (?, ? )")) {
                    insertStmt.setInt(1, prs.getId());
                    insertStmt.setInt(2, prs.getTime_limit());
                    insertStmt.executeUpdate();
                    Logger.info("Time limit added for PID: " + prs.getId() + " with time limit: " + prs.getTime_limit());
                }
            } catch (SQLException e) {
                Logger.error("Error setting time limit: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieves the time limit for a specific process.
     * This method is thread-safe.
     *
     * @param process_id The ID of the process.
     * @return The time limit in sedb.getCon()ds, or 0 if not set.
     */
    public synchronized int getTimeLimit(int process_id) {
        try {
            PreparedStatement checkQuery = db.getCon().prepareStatement("Select * from Timelimits  WHERE PROCESS_ID= ?");
            checkQuery.setInt(1, process_id);
            ResultSet rs = checkQuery.executeQuery();
            if (rs.next()) {
                return rs.getInt("TIME_LIMIT");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * Retrieves all processes for a given user that appear to be URLs (by extension).
     *
     * @param user The {@link UserInfo} object representing the user.
     * @return A list of {@link ProcessInfo} objects representing URL-like processes.
     */
    public synchronized ArrayList<ProcessInfo> getURLS(UserInfo user) {
        ArrayList<ProcessInfo> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = db.getCon().prepareStatement(
                    "SELECT * FROM Processes WHERE USER_ID = ? AND " +
                            "(process_name LIKE '%.com%' OR process_name LIKE '%.net%' OR process_name LIKE '%.org%' OR process_name LIKE '%.edu%')"
            );
            checkQuery.setInt(1, user.getId() - 1);
            ResultSet rs = checkQuery.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID");
                int userId = rs.getInt("USER_ID");
                String processName = rs.getString("Process_name");
                int totalTime = rs.getInt("TOTAL_TIME");
                int timeLimit = getTimeLimit(id);
                resArray.add(new ProcessInfo(id, userId, processName, totalTime, timeLimit));
            }
            rs.close();
            checkQuery.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }

    /**
     * Retrieves the total tracked time for a specific process.
     * This method is thread-safe.
     *
     * @param process_id The ID of the process.
     * @return The total time in sedb.getCon()ds.
     */
    public synchronized int getTime(int process_id) {
        try {
            PreparedStatement checkQuery = db.getCon().prepareStatement("Select * from Processes WHERE ID= ?");
            checkQuery.setInt(1, process_id);
            ResultSet rs = checkQuery.executeQuery();
            if (rs.next()) {
                return rs.getInt("TOTAL_TIME");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * Removes a process and its associated time limits from the database.
     * This operation is performed asynchronously.
     *
     * @param prs The {@link ProcessInfo} object representing the process to remove.
     */
    public synchronized void removeProcess(ProcessInfo prs) {
        db.executeDatabaseTask(() -> {
            try {
                PreparedStatement checkQuery = db.getCon().prepareStatement("DELETE FROM Processes WHERE ID=?");
                checkQuery.setInt(1, prs.getId());
                checkQuery.executeUpdate();

                PreparedStatement checkQuery2 = db.getCon().prepareStatement("DELETE FROM TimeLimits WHERE PROCESS_ID=?");
                checkQuery2.setInt(1, prs.getId());
                checkQuery2.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieves all processes for a specific user.
     * This method is thread-safe.
     *
     * @param user_id The ID of the user.
     * @return A list of {@link ProcessInfo} objects.
     */
    public synchronized ArrayList<ProcessInfo> getProcesses(int user_id) {
        ArrayList<ProcessInfo> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = db.getCon().prepareStatement("SELECT * FROM processes WHERE user_id=?");
            checkQuery.setInt(1, user_id);
            ResultSet rs = checkQuery.executeQuery();
            while (rs.next()) {
                int time_limit = getTimeLimit(rs.getInt("ID"));
                resArray.add(new ProcessInfo(rs.getInt("ID"), rs.getInt("USER_ID"), rs.getString("PROCESS_NAME"), rs.getInt("TOTAL_TIME"), time_limit));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }

    /**
     * Updates the name and time limit of a process.
     * This operation is performed asynchronously.
     *
     * @param prs The {@link ProcessInfo} object db.getCon()taining updated process data.
     */
    public void updateProcess(ProcessInfo prs) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement("UPDATE Processes SET PROCESS_NAME = ? WHERE ID = ?")) {
                stmt.setString(1, prs.getProcess_name());
                stmt.setInt(2, prs.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Error setting time limit: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement("UPDATE TimeLimits SET TIME_LIMIT = ? WHERE PROCESS_ID = ?")) {
                stmt.setInt(1, prs.getTime_limit());
                stmt.setInt(2, prs.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Error setting time limit: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Adds a new process for a specific user if it does not already exist.
     * Also sets the time limit for the process asynchronously.
     *
     * @param prs The {@link ProcessInfo} object containing process details.
     */
    public void addProcess(ProcessInfo prs) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement("SELECT 1 FROM Processes WHERE PROCESS_NAME = ? AND USER_ID = ?")) {
                stmt.setString(1, prs.getProcess_name());
                stmt.setInt(2, prs.getUser_id());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Logger.warn("Process already exists.");
                        return;
                    }
                }
                try (PreparedStatement insertStmt = db.getCon().prepareStatement(
                        "INSERT INTO Processes (USER_ID, PROCESS_NAME, TOTAL_TIME) VALUES (?, ?, 0)")) {
                    insertStmt.setInt(1, prs.getUser_id());
                    insertStmt.setString(2, prs.getProcess_name());
                    insertStmt.executeUpdate();
                    Logger.info("Process added: " + prs.getProcess_name());
                }
            } catch (SQLException e) {
                Logger.error("Error adding process: " + e.getMessage());
            }
        });

       db.executeDatabaseTask(() -> {
            try (PreparedStatement idStmt = db.getCon().prepareStatement(
                    "Select ID from Processes WHERE USER_ID = ? AND PROCESS_NAME = ?")) {
                idStmt.setInt(1, prs.getUser_id());
                idStmt.setString(2, prs.getProcess_name());
                try (ResultSet rs = idStmt.executeQuery()) {
                    if (rs.next()) {
                        prs.setId(rs.getInt("ID"));
                        setTimeLimit(prs);
                    } else {
                        Logger.warn("Process ID not found");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
