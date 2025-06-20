package db;

import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import Events.EventInfo;
import Processes.ProcessInfo;
import Processes.UserInfo;
import javafx.application.Platform;

/**
 * The {@code Database} class provides a singleton interface for managing all database operations
 * in the Parental Control App. It supports asynchronous execution of database tasks using a
 * background thread and a blocking queue, ensuring that database access is thread-safe and
 * non-blocking for the main application.
 * <p>
 * This class handles CRUD operations for users, processes, time limits, and events, as well as
 * secure password management for admin access. It uses SQLite as the underlying database.
 * </p>
 * <h2>Features:</h2>
 * <ul>
 *   <li>Singleton pattern for a single database connection</li>
 *   <li>Asynchronous task execution via a dedicated thread</li>
 *   <li>Process and user management</li>
 *   <li>Time tracking and time limit enforcement</li>
 *   <li>Event scheduling and management</li>
 *   <li>Secure password hashing and verification</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * All write operations are executed asynchronously on a background thread. Some read operations
 * are synchronized to ensure thread safety.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class Database {
    /**
     * Database connection object.
     */
    Connection con;

    /**
     * Statement object for executing SQL commands.
     */
    Statement stm;

    /**
     * Singleton instance of the {@code Database} class.
     */
    private static Database instance;

    /**
     * A blocking queue to hold database-related tasks for asynchronous execution.
     */
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    /**
     * A dedicated thread used to process tasks in the {@code taskQueue}.
     */
    private final Thread dbThread;

    /**
     * Constructor to initialize the database connection and background task thread.
     * Creates all required tables if they do not exist.
     */
    public Database() {
        try {
            con = DriverManager.getConnection("jdbc:sqlite:data.db");
            Statement stm = con.createStatement();
            stm.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Users (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    NAME TEXT NOT NULL,
                    IP TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS Processes (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    USER_ID INTEGER NOT NULL,
                    PROCESS_NAME TEXT NOT NULL,
                    TOTAL_TIME INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID)
                );
                CREATE TABLE IF NOT EXISTS TimeLimits (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    PROCESS_ID INTEGER NOT NULL,
                    TIME_LIMIT INTEGER NOT NULL,
                    FOREIGN KEY (PROCESS_ID) REFERENCES Processes(ID)
                );
                CREATE TABLE IF NOT EXISTS UsageTracking (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    USER_ID INTEGER,
                    NAME INTEGER NOT NULL,
                    TIME INTEGER NOT NULL,
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID)
                );
                CREATE TABLE IF NOT EXISTS Events (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    USER_ID INTEGER NOT NULL,
                    EVENT_NAME TEXT NOT NULL,
                    TIME INTEGER NOT NULL,
                    BEFORE_AT INTEGER NOT NULL,
                    REPEAT INTEGER NOT NULL,
                    CREATED_AT INTEGER NOT NULL,
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID)
                );
                CREATE TABLE IF NOT EXISTS Admin (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    PASSWORD TEXT NOT NULL
                );
            """);
            System.out.println("Database successfully created");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Successfully connected to database");

        dbThread = new Thread(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    System.err.println("Database task thread interrupted!");
                    break;
                }
            }
        });
        dbThread.start();
    }

    /**
     * Retrieves the singleton instance of the database.
     *
     * @return The singleton instance of {@code Database}.
     */
    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Adds a task to the database task queue for asynchronous execution.
     *
     * @param task A {@code Runnable} task to be executed.
     */
    private void executeDatabaseTask(Runnable task) {
        taskQueue.add(task);
    }

    /**
     * Adds a new process for a specific user if it does not already exist.
     * Also sets the time limit for the process asynchronously.
     *
     * @param prs The {@link ProcessInfo} object containing process details.
     */
    public void addProcess(ProcessInfo prs) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT 1 FROM Processes WHERE PROCESS_NAME = ? AND USER_ID = ?")) {
                stmt.setString(1, prs.getProcess_name());
                stmt.setInt(2, prs.getUser_id());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Process already exists.");
                        return;
                    }
                }
                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO Processes (USER_ID, PROCESS_NAME, TOTAL_TIME) VALUES (?, ?, 0)")) {
                    insertStmt.setInt(1, prs.getUser_id());
                    insertStmt.setString(2, prs.getProcess_name());
                    insertStmt.executeUpdate();
                    System.out.println("Process added: " + prs.getProcess_name());
                }
            } catch (SQLException e) {
                System.err.println("Error adding process: " + e.getMessage());
            }
        });

        executeDatabaseTask(() -> {
            try (PreparedStatement idStmt = con.prepareStatement(
                    "Select ID from Processes WHERE USER_ID = ? AND PROCESS_NAME = ?")) {
                idStmt.setInt(1, prs.getUser_id());
                idStmt.setString(2, prs.getProcess_name());
                try (ResultSet rs = idStmt.executeQuery()) {
                    if (rs.next()) {
                        prs.setId(rs.getInt("ID"));
                        setTimeLimit(prs);
                    } else {
                        System.out.println("Process ID not found");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Increments the total tracked time for a process by 2 seconds.
     * This operation is performed asynchronously.
     *
     * @param prs The Process to update.
     */
    public void updateUsageTime(ProcessInfo prs) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE UsageTracking SET TIME=TIME+2 WHERE USER_ID = ? and NAME = ?")) {
                stmt.setInt(1, prs.getUser_id());
                stmt.setString(2, prs.getProcess_name());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error updating process time: " + e.getMessage());
            }
        });
    }

    public boolean isUsageTracked(ProcessInfo prs) {
        try (PreparedStatement checkStmt = con.prepareStatement(
                "SELECT * FROM UsageTracking WHERE NAME = ? AND USER_ID = ?")) {
            checkStmt.setString(1, prs.getProcess_name());
            checkStmt.setInt(2, prs.getUser_id());
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public ArrayList<ProcessInfo> getUsageTracking(UserInfo user) {
        ArrayList<ProcessInfo> resArray = new ArrayList<>();
        try (PreparedStatement checkQuery = con.prepareStatement(
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
            System.err.println("Error retrieving usage tracking: " + e.getMessage());
        }
        return resArray;
    }

    public void addUsageTime(ProcessInfo prs) {
        executeDatabaseTask(() -> {
            try (PreparedStatement checkStmt = con.prepareStatement(
                    "Select * from UsageTracking  WHERE NAME= ? and USER_ID=?")) {
                checkStmt.setInt(2, prs.getId());
                checkStmt.setInt(1, prs.getUser_id());
                System.out.println("Checking if usage time exists for process: " + prs.getProcess_name());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement updateStmt = con.prepareStatement(
                                "INSERT INTO UsageTracking (USER_ID,NAME,TIME) VALUES (?,?,?)")) {
                                    System.out.println("Adding usage time for process: " + prs.getProcess_name());
                            updateStmt.setInt(1, prs.getUser_id());
                            updateStmt.setString(2, prs.getProcess_name());
                            updateStmt.setInt(3,0);
                            updateStmt.executeUpdate();
                            return;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
    }
    /**
     * Increments the total tracked time for a process by 2 seconds.
     * This operation is performed asynchronously.
     *
     * @param process_id The ID of the process to update.
     */
    public void updateTime(int process_id) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE Processes SET TOTAL_TIME=TOTAL_TIME+2 WHERE ID = ?")) {
                stmt.setInt(1, process_id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error updating process time: " + e.getMessage());
            }
        });
    }

    /**
     * Sets or updates the time limit for a given process.
     * If a time limit already exists, it is updated; otherwise, a new record is inserted.
     * This operation is performed asynchronously.
     *
     * @param prs The {@link ProcessInfo} object containing process ID and time limit.
     */
    public void setTimeLimit(ProcessInfo prs) {
        executeDatabaseTask(() -> {
            try (PreparedStatement checkStmt = con.prepareStatement(
                    "Select * from Timelimits  WHERE PROCESS_ID= ?")) {
                checkStmt.setInt(1, prs.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = con.prepareStatement(
                                "UPDATE Timelimits SET TIME_LIMIT = ? WHERE PROCESS_ID= ?")) {
                            updateStmt.setInt(1, prs.getTime_limit());
                            updateStmt.setInt(2, prs.getId());
                            updateStmt.executeUpdate();
                            return;
                        }
                    }
                }
                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO Timelimits (PROCESS_ID, TIME_LIMIT) VALUES (?, ? )")) {
                    insertStmt.setInt(1, prs.getId());
                    insertStmt.setInt(2, prs.getTime_limit());
                    insertStmt.executeUpdate();
                    System.out.println("Time limit added for PID: " + prs.getId() + " with time limit: " + prs.getTime_limit());
                }
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieves the time limit for a specific process.
     * This method is thread-safe.
     *
     * @param process_id The ID of the process.
     * @return The time limit in seconds, or 0 if not set.
     */
    public synchronized int getTimeLimit(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Timelimits  WHERE PROCESS_ID= ?");
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
            PreparedStatement checkQuery = con.prepareStatement(
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
     * @return The total time in seconds.
     */
    public synchronized int getTime(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Processes WHERE ID= ?");
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
        executeDatabaseTask(() -> {
            try {
                PreparedStatement checkQuery = con.prepareStatement("DELETE FROM Processes WHERE ID=?");
                checkQuery.setInt(1, prs.getId());
                checkQuery.executeUpdate();

                PreparedStatement checkQuery2 = con.prepareStatement("DELETE FROM TimeLimits WHERE PROCESS_ID=?");
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
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM processes WHERE user_id=?");
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
     * Retrieves all users from the database.
     *
     * @return A list of {@link UserInfo} objects.
     */
    public ArrayList<UserInfo> getUsers() {
        ArrayList<UserInfo> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM users");
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
        try (PreparedStatement stm = con.prepareStatement("SELECT * FROM users WHERE name=?")) {
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
            executeDatabaseTask(() -> {
                try (PreparedStatement stmt = con.prepareStatement("INSERT INTO Users (NAME,IP) VALUES (?,?)")) {
                    stmt.setString(1, name);
                    stmt.setString(2, "192.168.1.1");
                    stmt.executeUpdate();

                    if (onCreated != null){
                        Platform.runLater(onCreated);
                    }
                    System.out.println("User created: " + name);
                } catch (SQLException e) {
                    System.err.println("Error setting time limit: " + e.getMessage());
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
        executeDatabaseTask(() -> {
            try {
                int userId = user.getId() - 1;
                ArrayList<Integer> processIds = new ArrayList<>();
                try (PreparedStatement getProcesses = con.prepareStatement(
                        "SELECT ID FROM Processes WHERE User_ID = ?")) {
                    getProcesses.setInt(1, userId);
                    ResultSet rs = getProcesses.executeQuery();
                    while (rs.next()) {
                        processIds.add(rs.getInt("ID"));
                    }
                }
                try (PreparedStatement deleteTimeLimits = con.prepareStatement(
                        "DELETE FROM TimeLimits WHERE Process_ID = ?")) {
                    for (int processId : processIds) {
                        deleteTimeLimits.setInt(1, processId);
                        deleteTimeLimits.executeUpdate();
                    }
                }
                try (PreparedStatement deleteEvents = con.prepareStatement(
                        "DELETE FROM Events WHERE User_ID = ?")) {
                    deleteEvents.setInt(1, userId);
                    deleteEvents.executeUpdate();
                }
                try (PreparedStatement deleteProcesses = con.prepareStatement(
                        "DELETE FROM Processes WHERE User_ID = ?")) {
                    deleteProcesses.setInt(1, userId);
                    deleteProcesses.executeUpdate();
                }
                try (PreparedStatement deleteUser = con.prepareStatement(
                        "DELETE FROM Users WHERE ID = ?")) {
                    deleteUser.setInt(1, userId + 1);
                    deleteUser.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Updates the name and time limit of a process.
     * This operation is performed asynchronously.
     *
     * @param prs The {@link ProcessInfo} object containing updated process data.
     */
    public void updateProcess(ProcessInfo prs) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("UPDATE Processes SET PROCESS_NAME = ? WHERE ID = ?")) {
                stmt.setString(1, prs.getProcess_name());
                stmt.setInt(2, prs.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("UPDATE TimeLimits SET TIME_LIMIT = ? WHERE PROCESS_ID = ?")) {
                stmt.setInt(1, prs.getTime_limit());
                stmt.setInt(2, prs.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
    }

    /**
     * Adds a new event for a user if it does not already exist.
     * This operation is performed asynchronously.
     *
     * @param evt The {@link EventInfo} object containing event details.
     */
    public void addEvent(EventInfo evt) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT 1 FROM Events WHERE EVENT_NAME = ? AND USER_ID = ?")) {
                stmt.setString(1, evt.getEvent_name());
                stmt.setInt(2, evt.getUser_id());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Event already exists.");
                        return;
                    }
                }
                try (PreparedStatement insertStmt = con.prepareStatement(
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
                System.err.println("Error adding Event: " + e.getMessage());
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
        executeDatabaseTask(() -> {
            try {
                PreparedStatement checkQuery = con.prepareStatement("DELETE FROM Events WHERE ID=?");
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
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM Events WHERE USER_ID = ?")) {
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

    public void setEventTime(EventInfo evt, long created_at) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("UPDATE Events SET CREATED_AT = ? WHERE ID = ?")) {
                stmt.setInt(1, (int)created_at);
                stmt.setInt(2, evt.getId());
                stmt.executeUpdate();
                System.out.println("Event creation time updated: " + evt.getEvent_name());
            } catch (SQLException e) {
                System.err.println("Error updating Event: " + e.getMessage());
            }
        });
    }

    /**
     * Updates an existing event in the database.
     * This operation is performed asynchronously.
     *
     * @param evt The {@link EventInfo} object containing updated event data.
     */
    public void updateEvent(EventInfo evt) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE Events SET EVENT_NAME = ?, TIME = ?,BEFORE_AT = ?, REPEAT = ? WHERE ID = ?")) {
                stmt.setString(1, evt.getEvent_name());
                stmt.setInt(2, evt.getTime());
                stmt.setInt(4, evt.isRepeat() ? 1 : 0);
                stmt.setInt(3, evt.isBefore_at() ? 1 : 0);
                stmt.setInt(5, evt.getId());
                stmt.executeUpdate();
                System.out.println("Event updated: " + evt.getEvent_name());
            } catch (SQLException e) {
                System.err.println("Error updating Event: " + e.getMessage());
            }
        });
    }

    /**
     * Checks the admin password against the stored hash, or sets it if not present.
     *
     * @param pass The password to check or set.
     * @return {@code true} if the password is correct or was set, {@code false} otherwise.
     */
    public boolean checkPassword(String pass) {
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM ADMIN")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedEncoded = rs.getString("PASSWORD");
                byte[] combined = Base64.getDecoder().decode(storedEncoded);
                HashedPassword stored = HashedPassword.fromBytes(combined);

                KeySpec spec = new PBEKeySpec(pass.toCharArray(), stored.getSalt(), 65536, 128);
                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] computedHash = f.generateSecret(spec).getEncoded();

                return Arrays.equals(stored.getHash(), computedHash);
            } else {
                addPassword(pass);
                return true;
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hashes a password using PBKDF2 with a random salt.
     *
     * @param pass The password to hash.
     * @return A {@link HashedPassword} object containing the salt and hash.
     */
    public HashedPassword hashPassword(String pass) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();

            return new HashedPassword(salt, hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new admin password to the database, securely hashed.
     * This operation is performed asynchronously.
     *
     * @param pass The password to add.
     */
    public synchronized void addPassword(String pass) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("INSERT INTO ADMIN(PASSWORD) VALUES(?)")) {
                HashedPassword hp = hashPassword(pass);
                String encoded = Base64.getEncoder().encodeToString(hp.toBytes());
                stmt.setString(1, encoded);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
