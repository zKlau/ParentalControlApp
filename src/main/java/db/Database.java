package db;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Database {
    Connection con;
    Statement stm;
    private static Database instance;
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final Thread dbThread;

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

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void executeDatabaseTask(Runnable task) {
        taskQueue.add(task); // Add the task to the queue
    }

    public void addProcess(String name, int user_id) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement("Select * from Processes WHERE PROCESS_NAME = '"+ name+"'")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Process already exists.");
                        return;
                    }
                }

                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO Processes (USER_ID,PROCESS_NAME,TOTAL_TIME) VALUES ("+user_id+",'"+name+"',0)")) {
                    System.out.println("Process added: " + name);
                }
            } catch (SQLException e) {
                System.err.println("Error adding process: " + e.getMessage());
            }
        });
    }


    public void updateTime(int process_id) {
        executeDatabaseTask(() -> {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE Processes SET TOTAL_TIME=TOTAL_TIME+2 WHERE ID = "+process_id)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error updating process time: " + e.getMessage());
            }
        });
    }


    public void setTimeLimit(int process_id, int time_limit) {
        executeDatabaseTask(() -> {
            try (PreparedStatement checkStmt = con.prepareStatement(
                    "Select * from Timelimits  WHERE PROCESS_ID= "+ process_id)) {
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = con.prepareStatement(
                                "UPDATE Timelimits SET TIME_LIMIT = "+time_limit + " WHERE PROCESS_ID="+process_id)) {
                            updateStmt.executeUpdate();
                            return;
                        }
                    }
                }

                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO Timelimits (PROCESS_ID, TIME_LIMIT) VALUES ("+process_id+","+time_limit+")")) {
                    insertStmt.executeUpdate();
                    System.out.println("Time limit added for PID: " + process_id);
                }
            } catch (SQLException e) {
                System.err.println("Error setting time limit: " + e.getMessage());
            }
        });
    }

    public synchronized int getTimeLimit(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Timelimits  WHERE PROCESS_ID= "+ process_id);
            ResultSet rs = checkQuery.executeQuery();
            if(rs.next()) {
                return rs.getInt("TIME_LIMIT");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    public synchronized int getTime(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Processes WHERE ID= "+ process_id);
            ResultSet rs = checkQuery.executeQuery();
            if(rs.next()) {
                return rs.getInt("TOTAL_TIME");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    public synchronized void removeProcess(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("DELETE FROM Processes WHERE ID="+process_id);
            ResultSet rs = checkQuery.executeQuery();

            PreparedStatement checkQuery2 = con.prepareStatement("DELETE FROM TimeLimits WHERE PROCESS_ID="+process_id);
            ResultSet rs2 = checkQuery.executeQuery();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized ArrayList<String> getProcesses(int user_id) {
        ArrayList<String> resArray = new ArrayList<>();
        try {
            PreparedStatement checkQuery = con.prepareStatement("SELECT * FROM processes WHERE user_id="+user_id);
            ResultSet rs = checkQuery.executeQuery();

            while  (rs.next()) {
                resArray.add(rs.getString("ID") + " " + rs.getString("PROCESS_NAME") + " " + rs.getString("TOTAL_TIME"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resArray;
    }
}
