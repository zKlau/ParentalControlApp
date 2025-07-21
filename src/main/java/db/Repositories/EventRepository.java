package db.Repositories;

import Events.EventInfo;
import db.Database;
import org.tinylog.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EventRepository {
    private Database db;

    public EventRepository(Database database) {
        this.db = database;
    }

    /**
     * Adds a new event for a user if it does not already exist.
     * This operation is performed asynchronously.
     *
     * @param evt The {@link EventInfo} object containing event details.
     */
    public void addEvent(EventInfo evt) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement("SELECT 1 FROM Events WHERE EVENT_NAME = ? AND USER_ID = ?")) {
                stmt.setString(1, evt.getEvent_name());
                stmt.setInt(2, evt.getUser_id());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Logger.error("Event already exists.");
                        return;
                    }
                }
                try (PreparedStatement insertStmt = db.getCon().prepareStatement(
                        "INSERT INTO Events (USER_ID, EVENT_NAME,TIME, BEFORE_AT, REPEAT,CREATED_AT) VALUES (?, ?, ?,?,?,?)")) {
                    insertStmt.setInt(1, evt.getUser_id());
                    insertStmt.setString(2, evt.getEvent_name());
                    insertStmt.setInt(3, evt.getTime());
                    insertStmt.setInt(4, evt.isBefore_at() ? 1 : 0);
                    insertStmt.setInt(5, evt.isRepeat() ? 1 : 0);
                    insertStmt.setLong(6, evt.getCreated_at());
                    insertStmt.executeUpdate();
                    System.out.println("Event added: " + evt.getEvent_name());
                }
            } catch (SQLException e) {
                Logger.error("Error adding Event: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Removes an event from the database.
     * This operation is performed asynchronously.
     *
     * @param evt The {@link EventInfo} object representing the event to remove.
     */
    public synchronized void removeEvent(EventInfo evt) {
        db.executeDatabaseTask(() -> {
            try {
                PreparedStatement checkQuery = db.getCon().prepareStatement("DELETE FROM Events WHERE ID=?");
                checkQuery.setInt(1, evt.getId());
                checkQuery.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieves all events for a specific user.
     * This method is thread-safe.
     *
     * @param userId The ID of the user.
     * @return A list of {@link EventInfo} objects for the user.
     */
    public synchronized ArrayList<EventInfo> getEvents(int userId) {
        ArrayList<EventInfo> events = new ArrayList<>();
        try (PreparedStatement stmt = db.getCon().prepareStatement("SELECT * FROM Events WHERE USER_ID = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EventInfo evt = new EventInfo(
                        rs.getInt("ID"),
                        rs.getInt("USER_ID"),
                        rs.getString("EVENT_NAME"),
                        rs.getInt("TIME"),
                        rs.getInt("BEFORE_AT") == 1,
                        rs.getInt("REPEAT") == 1,
                        rs.getLong("CREATED_AT")
                );
                events.add(evt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving events: " + e.getMessage(), e);
        }
        return events;
    }

    /**
     * Sets the creation time for an event in the database.
     * This operation is performed asynchronously.
     *
     * @param evt        The {@link EventInfo} object representing the event to update.
     * @param created_at The new creation time to set.
     */
    public void setEventTime(EventInfo evt, long created_at) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement("UPDATE Events SET CREATED_AT = ? WHERE ID = ?")) {
                stmt.setInt(1, (int)created_at);
                stmt.setInt(2, evt.getId());
                stmt.executeUpdate();
                Logger.info("Event creation time updated: " + evt.getEvent_name());
            } catch (SQLException e) {
                Logger.error("Error updating Event: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Updates an existing event in the database.
     * This operation is performed asynchronously.
     *Add commentMore actions
     * @param evt The {@link EventInfo} object containing updated event data.
     */
    public void updateEvent(EventInfo evt) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement(
                    "UPDATE Events SET EVENT_NAME = ?, TIME = ?,BEFORE_AT = ?, REPEAT = ? WHERE ID = ?")) {
                stmt.setString(1, evt.getEvent_name());
                stmt.setInt(2, evt.getTime());
                stmt.setInt(4, evt.isRepeat() ? 1 : 0);
                stmt.setInt(3, evt.isBefore_at() ? 1 : 0);
                stmt.setInt(5, evt.getId());
                stmt.executeUpdate();
                Logger.info("Event updated: " + evt.getEvent_name());
            } catch (SQLException e) {
                Logger.error("Error updating Event: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
