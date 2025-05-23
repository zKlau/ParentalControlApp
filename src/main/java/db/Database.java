package db;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * The {@code Database} class manages database operations such as adding, updating,
 * and removing processes, as well as handling time tracking. It uses a singleton
 * pattern to ensure there is only one instance of the database connection.
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
        taskQueue.add(task); // Add the task to the queue
    }

    /**
     * Adds a new process to the database for a specific user.
     *
     * @param name    The name of the process.
     * @param user_id The ID of the user the process belongs to.
     */
    public void addProcess(String name, int user_id) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("SELECT 1 FROM Processes WHERE PROCESS_NAME = ?")) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Process already exists.");
                        return;
                    }
                }

                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO Processes (USER_ID, PROCESS_NAME, TOTAL_TIME) VALUES (?, ?, 0)")) {
                    insertStmt.setInt(1, user_id);
                    insertStmt.setString(2, name);
                    insertStmt.executeUpdate();
                    System.out.println("Process added: " + name);
                }
            } catch (SQLException e) {
                System.err.println("Error adding process: " + e.getMessage());
            }
        });
    }

    /**
     * Updates the last recorded time of a process.
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
     * Sets a time limit for a given process.
     *
     * @param process_id The ID of the process.
     * @param time_limit The time limit to assign (in seconds).
     */
    public void setTimeLimit(int process_id, int time_limit) {
        executeDatabaseTask(() -> {
            try (PreparedStatement checkStmt = con.prepareStatement(
                    "Select * from Timelimits  WHERE PROCESS_ID= ?")) {
                checkStmt.setInt(1, process_id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = con.prepareStatement(
                                "UPDATE Timelimits SET TIME_LIMIT = ? WHERE PROCESS_ID= ?")) {
                            updateStmt.setInt(1, time_limit);
                            updateStmt.setInt(2, process_id);
                            updateStmt.executeUpdate();
                            return;
                        }
                    }
                }

                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO Timelimits (PROCESS_ID, TIME_LIMIT) VALUES (?, ? )")) {
                    insertStmt.setInt(1, process_id);
                    insertStmt.setInt(2, time_limit);
                    insertStmt.executeUpdate();
                    System.out.println("Time limit added for PID: " + process_id);
                }
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieves the time limit for a specific process.
     *
     * @param process_id The ID of the process to query.
     * @return The time limit assigned to the process (in seconds).
     */
    public synchronized int getTimeLimit(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Timelimits  WHERE PROCESS_ID= ?");
            checkQuery.setInt(1, process_id);
            ResultSet rs = checkQuery.executeQuery();
            if(rs.next()) {
                return rs.getInt("TIME_LIMIT");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }


    /**
     * Retrieves the currently tracked time for a specific process.
     *
     * @param process_id The ID of the process to query.
     * @return The current time tracked for the process (in seconds).
     */
    public synchronized int getTime(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Processes WHERE ID= ?");
            checkQuery.setInt(1, process_id);
            ResultSet rs = checkQuery.executeQuery();
            if(rs.next()) {
                return rs.getInt("TOTAL_TIME");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    /**
     * Removes a process from the database.
     *
     * @param process_id The ID of the process to remove.
     */
    public synchronized void removeProcess(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("DELETE FROM Processes WHERE ID=?");
            checkQuery.setInt(1, process_id);
            ResultSet rs = checkQuery.executeQuery();

            PreparedStatement checkQuery2 = con.prepareStatement("DELETE FROM TimeLimits WHERE PROCESS_ID=?");
            checkQuery2.setInt(1, process_id);
            ResultSet rs2 = checkQuery.executeQuery();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a list of process names for a specific user.
     *
     * @param user_id The ID of the user to query.
     * @return An {@code ArrayList} of process names for the user.
     */
    public synchronized ArrayList<String> getProcesses(int user_id) {
        ArrayList<String> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM processes WHERE user_id=?");
            checkQuery.setInt(1, user_id);
            ResultSet rs = checkQuery.executeQuery();

            while  (rs.next()) {
                resArray.add(rs.getString("PROCESS_NAME") + " " + rs.getString("TOTAL_TIME"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }

    public ArrayList<String> getUsers() {
        ArrayList<String> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM users");
            ResultSet rs = checkQuery.executeQuery();

            while  (rs.next()) {
                resArray.add(rs.getString("ID") + " " + rs.getString("NAME"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }
    public boolean isUserName(String name) {
        try (PreparedStatement stm = con.prepareStatement("SELECT * FROM users WHERE name=?")) {
            stm.setString(1,name);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public synchronized boolean createUser(String name)     {
        if(!isUserName(name)) {
            executeDatabaseTask(() -> {
                try (PreparedStatement stmt = con.prepareStatement("INSERT INTO Users (NAME,IP) VALUES (?,?)")) {
                    stmt.setString(1, name);
                    stmt.setString(2,"192.168.1.100");
                    stmt.executeUpdate();
                    System.out.println("User created: " + name);

                } catch (SQLException e) {
                    System.err.println("Error setting time limit: " + e.getMessage());
                }
            });
            return true;
        }
        return false;
    }
}
