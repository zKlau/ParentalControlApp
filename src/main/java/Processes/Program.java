package Processes;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Processes.Managers.EventManager;
import Processes.Managers.ProcessManager;
import Processes.Managers.UsageManager;
import org.tinylog.Logger;

import Events.EventInfo;
import GUI.UI;
import db.Database;

/**
 * The {@code Program} class serves as the core logic and backend for the Parental Control App.
 * It manages user sessions, process monitoring, event scheduling, and interaction with the database.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Periodic monitoring of user processes and enforcing time limits</li>
 *   <li>Scheduling and executing user events (shutdown, logout, screenshot, etc.)</li>
 *   <li>Managing the current user and UI state</li>
 *   <li>Providing utility methods for process management</li>
 * </ul>
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class Program {
    /**
     * Singleton instance of the {@link Database}.
     */
    public static Database db = Database.getInstance();

    private static final ProcessManager processManager = new ProcessManager(db);
    private static final EventManager eventManager = new EventManager(db);
    private static final UsageManager usageManager = new UsageManager(db);

    /**
     * Indicates whether connections are allowed (used for UI state).
     */
    public boolean allow_connection = true;

    /**
     * The system username of the current OS user.
     */
    public String system_user = System.getProperty("user.name");

    /**
     * Timer for scheduling periodic process and event checks.
     */
    public Timer timer;

    /**
     * The index of the current user (used for database queries).
     */
    public int current_user = 0;

    /**
     * The current {@link UserInfo} object.
     */
    public UserInfo user;



    


    /**
     *  Set the user with the first user from the list
     * @return user selection status
     */
    public boolean setUser() {
        ArrayList<UserInfo> u = db.getUsers();
        if(u.size() > 0) {
            user = u.get(0);
            return true;
        } else {
            user = null;
            return false;
        }
    }

    /**
     * Reference to the main UI controller.
     */
    public UI ui;

    /**
     * The {@link WebFilter} instance for blocking/unblocking sites.
     */
    public WebFilter webFilter = new WebFilter();

    /**
     * Constructs a new {@code Program} instance and starts the periodic monitoring timer.
     */
    public Program() {
        if (!setUser()) {
            mainLoop();
        } else {
            mainLoop();
        }
    }

    /**
     * Starts the periodic monitoring timer
    */
    public void mainLoop() {
        usageManager.dailyUsage(user);
        Timer timer = new Timer(true);
        Logger.info("Application starting!");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (EventInfo event : db.getEvents(current_user)) {
                        //Logger.info(event.toString());
                        eventManager.runEvent(event);
                    }

                    for (var i : db.getProcesses(current_user))
                        if (processManager.isProcessRunning(i.getProcess_name())) {
                            db.updateTime(i.getId());
                            int time_limit = db.getTimeLimit(i.getId());
                            if (time_limit > 0 && db.getTime(i.getId()) > time_limit) {
                                processManager.terminateProcess(i.getProcess_name());
                            }
                        }
                } catch (Exception e) {
                    Logger.warn("Error during process monitoring: " + e.getMessage());
                }
                if (ui != null) {
                    javafx.application.Platform.runLater(() -> ui.updateMenu());
                }
            }
        }, 0, 3000);
        new Thread( () -> {
            try {
                    processManager.trackAllProcesses(current_user);
                    Thread.sleep(6000);
                } catch (Exception e) {
                    Logger.error("Error during process tracking: " + e.getMessage());
                }
        }).start();
    }
}
