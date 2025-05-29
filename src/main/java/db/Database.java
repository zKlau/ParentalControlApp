package db;
import Events.EventInfo;
import Processes.ProcessInfo;
import Processes.UserInfo;

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
                CREATE TABLE IF NOT EXISTS Events (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    USER_ID INTEGER NOT NULL,
                    EVENT_NAME TEXT NOT NULL,
                    TIME INTEGER NOT NULL,
                    REPEAT INTEGER NOT NULL,
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID)
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
                    "Select ID from Processes WHERE USER_ID = ? AND PROCESS_NAME = ?")){
                idStmt.setInt(1,prs.getUser_id());
                idStmt.setString(2,prs.getProcess_name());

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
     * Retrieves a list of process names for a specific user.
     *
     * @param user_id The ID of the user to query.
     * @return An {@code ArrayList} of process names for the user.
     */
    public synchronized ArrayList<ProcessInfo> getProcesses(int user_id) {
        ArrayList<ProcessInfo> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM processes WHERE user_id=?");
            checkQuery.setInt(1, user_id);
            ResultSet rs = checkQuery.executeQuery();

            while  (rs.next()) {
                int time_limit = getTimeLimit(rs.getInt("ID"));
                resArray.add(new ProcessInfo(rs.getInt("ID"), rs.getInt("USER_ID"), rs.getString("PROCESS_NAME"),rs.getInt("TOTAL_TIME"), time_limit));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }

    public ArrayList<UserInfo> getUsers() {
        ArrayList<UserInfo> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM users");
            ResultSet rs = checkQuery.executeQuery();

            while  (rs.next()) {
                resArray.add(new UserInfo(rs.getString("name"),rs.getInt("id")));
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

    public void updateProcess(ProcessInfo prs) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("UPDATE Processes SET PROCESS_NAME = ? WHERE ID = ?")) {
                stmt.setString(1,prs.getProcess_name());
                stmt.setInt(2, prs.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("UPDATE TimeLimits SET TIME_LIMIT = ? WHERE PROCESS_ID = ?")) {
                stmt.setInt(1,prs.getTime_limit());
                stmt.setInt(2, prs.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
    }



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
                        "INSERT INTO Events (USER_ID, EVENT_NAME, TIME,REPEAT) VALUES (?, ?, ?,?)")) {
                    insertStmt.setInt(1, evt.getUser_id());
                    insertStmt.setString(2, evt.getEvent_name());
                    insertStmt.setInt(3, evt.getTime());
                    insertStmt.setInt(4,evt.isRepeat() ? 1 : 0);
                    insertStmt.executeUpdate();
                    System.out.println("Event added: " + evt.getEvent_name ());
                }
            } catch (SQLException e) {
                System.err.println("Error adding Event: " + e.getMessage());
            }
        });
    }


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
     *
     * @param userId The ID of the user.
     * @return A list of {@code EventInfo} objects for the user.
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
                        rs.getInt("REPEAT") == 1
                );
                events.add(evt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving events: " + e.getMessage(), e);
        }
        return events;
    }

    /**
     * Updates an existing event in the database.
     *
     * @param evt The {@code EventInfo} object containing updated event data.
     */
    public void updateEvent(EventInfo evt) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE Events SET EVENT_NAME = ?, TIME = ?, REPEAT = ? WHERE ID = ?")) {
                stmt.setString(1, evt.getEvent_name());
                stmt.setInt(2, evt.getTime());
                stmt.setInt(3, evt.isRepeat() ? 1 : 0);
                stmt.setInt(4, evt.getId());
                stmt.executeUpdate();
                System.out.println("Event updated: " + evt.getEvent_name());
            } catch (SQLException e) {
                System.err.println("Error updating Event: " + e.getMessage());
            }
        });
    }

}
