package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.tinylog.Logger;

import db.Repositories.AdminRepository;
import db.Repositories.DailyUsageRepository;
import db.Repositories.EventRepository;
import db.Repositories.ProcessRepository;
import db.Repositories.UsageTrackingRepository;
import db.Repositories.UserRepository;

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
    public final UserRepository userRepository;
    public final ProcessRepository processRepository;
    public final UsageTrackingRepository usageTrackingRepository;
    public final EventRepository eventRepository;
    public final AdminRepository adminRepository;
    public final DailyUsageRepository dailyUsageRepository;

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


    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    public Statement getStm() {
        return stm;
    }

    public void setStm(Statement stm) {
        this.stm = stm;
    }

    /**
     * Constructor to initialize the database connection and background task thread.
     * Creates all required tables if they do not exist.
     */
    public Database() {
        userRepository = new UserRepository(this);
        processRepository = new ProcessRepository(this);
        usageTrackingRepository = new UsageTrackingRepository(this);
        eventRepository = new EventRepository(this);
        adminRepository = new AdminRepository(this);
        dailyUsageRepository = new DailyUsageRepository(this);
        
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
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID),
                    UNIQUE(USER_ID,NAME)
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
                CREATE TABLE IF NOT EXISTS DailyUsage (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    USER_ID INTEGER NOT NULL,
                    DATE TEXT NOT NULL,
                    USAGE_SECONDS INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID),
                    UNIQUE(USER_ID, DATE)
                );
            """);
            Logger.info("Database successfully created");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Logger.info("Successfully connected to database");

        dbThread = new Thread(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Logger.error("Database task thread interrupted!");
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
    public void executeDatabaseTask(Runnable task) {
        taskQueue.add(task);
    }
}
