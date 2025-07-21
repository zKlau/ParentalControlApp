package db.Repositories;

import Processes.UserInfo;
import db.Database;
import javafx.application.Platform;
import org.tinylog.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserRepository {
    private Database db;

    public UserRepository(Database database) {
        this.db = database;
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A list of {@link UserInfo} objects.
     */
    public ArrayList<UserInfo> getUsers() {
        ArrayList<UserInfo> resArray = new ArrayList<>();
        try(PreparedStatement checkQuery = db.getCon().prepareStatement("SELECT * FROM users")) {
            ResultSet rs = checkQuery.executeQuery();
            while (rs.next()) {
                resArray.add(new UserInfo(rs.getString("name"), rs.getInt("id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }

    /**
     * Checks if a user with the given name exists in the database.
     *
     * @param name The username to check.
     * @return {@code true} if the user exists, {@code false} otherwise.
     */
    public boolean isUserName(String name) {
        try (PreparedStatement stm = db.getCon().prepareStatement("SELECT * FROM users WHERE name=?")) {
            stm.setString(1, name);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Creates a new user with the specified name if it does not already exist.
     * This operation is performed asynchronously.
     *
     * @param name The name of the new user.
     * @return {@code true} if the user was created, {@code false} if the user already exists.
     */
    public synchronized boolean createUser(String name, Runnable onCreated) {
        if (!isUserName(name)) {
            db.executeDatabaseTask(() -> {
                try (PreparedStatement stmt = db.getCon().prepareStatement("INSERT INTO Users (NAME,IP) VALUES (?,?)")) {
                    stmt.setString(1, name);
                    stmt.setString(2, "192.168.1.1");
                    stmt.executeUpdate();

                    if (onCreated != null){
                        Platform.runLater(onCreated);
                    }
                    Logger.info("User created: " + name);
                    System.out.println("User created: " + name);
                } catch (SQLException e) {
                    Logger.error("Error setting time limit: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Deletes a user and all associated processes, time limits, and events.
     * This operation is performed asynchronously.
     *
     * @param user The {@link UserInfo} object representing the user to delete.
     */
    public synchronized void deleteUser(UserInfo user) {
        db.executeDatabaseTask(() -> {
            try {
                int userId = user.getId() - 1;
                ArrayList<Integer> processIds = new ArrayList<>();
                try (PreparedStatement getProcesses = db.getCon().prepareStatement(
                        "SELECT ID FROM Processes WHERE User_ID = ?")) {
                    getProcesses.setInt(1, userId);
                    ResultSet rs = getProcesses.executeQuery();
                    while (rs.next()) {
                        processIds.add(rs.getInt("ID"));
                    }
                }
                try (PreparedStatement deleteTimeLimits = db.getCon().prepareStatement(
                        "DELETE FROM TimeLimits WHERE Process_ID = ?")) {
                    for (int processId : processIds) {
                        deleteTimeLimits.setInt(1, processId);
                        deleteTimeLimits.executeUpdate();
                    }
                }
                try (PreparedStatement deleteEvents = db.getCon().prepareStatement(
                        "DELETE FROM Events WHERE User_ID = ?")) {
                    deleteEvents.setInt(1, userId);
                    deleteEvents.executeUpdate();
                }
                try (PreparedStatement deleteProcesses = db.getCon().prepareStatement(
                        "DELETE FROM Processes WHERE User_ID = ?")) {
                    deleteProcesses.setInt(1, userId);
                    deleteProcesses.executeUpdate();
                }
                try (PreparedStatement deleteUser = db.getCon().prepareStatement(
                        "DELETE FROM Users WHERE ID = ?")) {
                    deleteUser.setInt(1, userId + 1);
                    deleteUser.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
