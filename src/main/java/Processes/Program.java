package Processes;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.tinylog.Logger;

import javax.imageio.ImageIO;

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



    private static final Set<String> WINDOWS_SYSTEM_PROCESSES = Set.of(
    "System Idle Process", "System", "smss.exe", "csrss.exe", "wininit.exe", "services.exe",
    "lsass.exe", "svchost.exe", "winlogon.exe", "explorer.exe", "spoolsv.exe", "dwm.exe",
    "taskhostw.exe", "fontdrvhost.exe", "registry", "conhost.exe", "rundll32.exe", "audiodg.exe",
    "WmiPrvSE.exe", "SearchIndexer.exe", "SearchUI.exe", "RuntimeBroker.exe", "SgrmBroker.exe",
    "StartMenuExperienceHost.exe", "ShellExperienceHost.exe", "SecurityHealthSystray.exe",
    "msmpeng.exe", "NisSrv.exe", "ctfmon.exe", "sihost.exe", "backgroundTaskHost.exe",
    "AppVShNotify.exe", "AppVClient.exe"
);


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
        Timer timer = new Timer(true);
        Logger.info("Application starting!");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (EventInfo event : db.getEvents(current_user)) {
                        //System.out.println("Checking events");
                        runEvent(event);
                    }

                    for (var i : db.getProcesses(current_user))
                        if (isProcessRunning(i.getProcess_name())) {
                            db.updateTime(i.getId());
                            int time_limit = db.getTimeLimit(i.getId());
                            if (time_limit > 0 && db.getTime(i.getId()) > time_limit) {
                                terminateProcess(i.getProcess_name());
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
                    trackAllProcesses();
                    Thread.sleep(6000);
                } catch (Exception e) {
                    System.err.println("Error during process tracking: " + e.getMessage());
                }
        }).start();
    }



    public void trackAllProcesses() {
        try {
        BufferedReader prs = getRunningProcesses();
        String line;
        while ((line = prs.readLine()) != null) {
            String[] parts = line.split(" ");
            String processName = parts[0];

            if (WINDOWS_SYSTEM_PROCESSES.contains(processName)) {
                continue;
            }

           // System.out.println(processName);

            ProcessInfo pr = new ProcessInfo(0, current_user, processName, 0);
            if (!db.isUsageTracked(pr)) {
                db.addUsageTime(pr);
                Logger.info("Tracking new process: " + processName);
            } else {
                //System.out.println("Already Tracking");
                db.updateUsageTime(pr);
            }
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }
    /**
     * Executes the specified {@link EventInfo} if its scheduled time has arrived.
     * Handles shutdown, logout, and screenshot events.
     * Removes non-repeating events after execution.
     *
     * @param event The event to run.
     */
    public static void runEvent(EventInfo event) {
        Calendar calendar = Calendar.getInstance();
        int currentTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int eventTime = event.getTime();
        long createdAt = event.getCreated_at();
        boolean shouldRun = false;

        if (event.isBefore_at()) {
            if (currentTimeMinutes == eventTime) {
                shouldRun = true;
            }
        } else {
            long minutesSinceCreation = (System.currentTimeMillis() / 60000L)- createdAt;
            if (minutesSinceCreation >= eventTime) {
                shouldRun = true;
            }
        }

        if (shouldRun) {
            String name = event.getEvent_name();
            switch (name) {
                case "Computer Shutdown":
                    Logger.info("Trigger: Computer Shutdown");
                    shutdownComputer();
                    break;
                case "User System logout":
                    Logger.info("Trigger: User System logout");
                    logoutUser();
                    break;
                case "Screenshot":
                    Logger.info("Trigger: Screenshot");
                    takeScreenshot();
                    break;
                default:
                    Logger.info("Unknown event: " + name);
                    break;
            }
            if (!event.isRepeat()) {
                db.removeEvent(event);
            } else {
               if (!event.isBefore_at()) {
                    db.setEventTime(event, System.currentTimeMillis() / 60000L);
               }
            }
        }
    }


    /**
     * Shuts down the computer immediately.
     */
    private static void shutdownComputer() {
        try {
            Runtime.getRuntime().exec("shutdown -s -t 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs out the current user.
     */
    private static void logoutUser() {
        try {
            Runtime.getRuntime().exec("shutdown -l");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a screenshot of the entire screen and saves it to the /Screenshots directory.
     */
    private static void takeScreenshot() {
    try {
        Robot r = new Robot();
        String dirPath = "Screenshots";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String path = dirPath + File.separator + System.currentTimeMillis() + ".jpg";
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle capture = new Rectangle(screenSize.width, screenSize.height);
        BufferedImage image = r.createScreenCapture(capture);
        ImageIO.write(image, "jpg", new File(path));
    } catch (AWTException | IOException e) {
        e.printStackTrace();
    }
}
    public static BufferedReader getRunningProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");

            return new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of running processes whose names match or contain the given name.
     *
     * @param pname The process name to search for.
     * @return A list of matching process names.
     */
    public static ArrayList<String> getRunningProcessesByName(String pname) {
        ArrayList<String> processes = new ArrayList<>();
        try {
            BufferedReader buffer = getRunningProcesses();

            String line;

            while ((line = buffer.readLine()) != null) {
                if (line.toLowerCase().startsWith(pname.toLowerCase()) || line.toLowerCase().contains(pname.toLowerCase())) {
                    processes.add(line.split(" ")[0]);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return processes;
    }



        /**
     * Checks if a process with the given name is currently running.
     *
     * @param pname The process name to check.
     * @return {@code true} if the process is running, {@code false} otherwise.
     */
    public static boolean isProcessRunning(String pname) {
        try {
           BufferedReader buffer = getRunningProcesses();

            String line;

            while ((line = buffer.readLine()) != null) {
                if (line.toLowerCase().startsWith(pname.toLowerCase()) || line.toLowerCase().contains(pname.toLowerCase())) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * Terminates all running processes that match the given name.
     *
     * @param pname The process name to terminate.
     */
    public static void terminateProcess(String pname) {
        try {
            ArrayList<String> processes = getRunningProcessesByName(pname);

            for (String process : processes) {
                Process prs = Runtime.getRuntime().exec("taskkill /F /IM " + process);
                prs.waitFor();
            }
            /*
            Process process = Runtime.getRuntime().exec("taskkill /F /IM " + pname);
            process.waitFor();
               */
            Logger.info("Process: " + pname + " terminated");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
