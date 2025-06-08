package Processes;

import Events.EventInfo;
import GUI.UI;
import db.Database;
import java.awt.Robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Program {
    public static Database db = Database.getInstance();
    public boolean allow_connection = true;
    public String system_user = System.getProperty("user.name");
    public Timer timer;
    public int current_user = 0;
    public UserInfo user;
    public UI ui;
    public WebFilter webFilter = new WebFilter();
    public Program() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (EventInfo event : db.getEvents(current_user)) {
                        runEvent(event);
                    }
                    for(var i : db.getProcesses(current_user))
                        if (isProcessRunning(i.getProcess_name())) {
                            db.updateTime(i.getId());
                            int time_limit = db.getTimeLimit(i.getId());
                            if (time_limit > 0 && db.getTime(i.getId()) > time_limit) {
                                terminateProcess(i.getProcess_name());
                            }
                        }
                } catch (Exception e) {
                    System.err.println("Error during process monitoring: " + e.getMessage());
                }
                if (ui != null) {
                    javafx.application.Platform.runLater(() -> ui.updateMenu());
                }
            }
        }, 0, 3000);
    }

    public static void runEvent(EventInfo event) {
        long currentTimeMinutes = System.currentTimeMillis() / 60000;

        if (event.getTime() >= currentTimeMinutes) {
            String name = event.getEvent_name();

            switch (name) {
                case "Computer Shutdown":
                    System.out.println("Trigger: Computer Shutdown");
                    shutdownComputer();
                    break;
                case "User System logout":
                    System.out.println("Trigger: User System logout");
                    logoutUser();
                    break;
                case "Screenshot":
                    System.out.println("Trigger: Screenshot");
                    takeScreenshot();
                    break;
            }
            if(!event.isRepeat()) {
                db.removeEvent(event);
            }
        }
    }

    private static void shutdownComputer() {
        try {
            Runtime.getRuntime().exec("shutdown -s -t 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logoutUser() {
        try {
            Runtime.getRuntime().exec("shutdown -l");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void takeScreenshot() {
        try {
            Robot r = new Robot();
            String path = "/Screenshots/" + System.currentTimeMillis() + ".jpg";
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle capture = new Rectangle(screenSize.width, screenSize.height);
            BufferedImage Image = r.createScreenCapture(capture);
            ImageIO.write(Image, "jpg", new File(path));
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<String> getRunningProcessesByName(String pname) {
        ArrayList<String> processes = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("tasklist");

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

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
    public static boolean isProcessRunning(String pname) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

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

    public static void terminateProcess(String pname) {
        try {
            ArrayList<String> processes = getRunningProcessesByName(pname);

            for(String process : processes) {
                Process prs = Runtime.getRuntime().exec("taskkill /F /IM " + process);
                prs.waitFor();
            }
            /*
            Process process = Runtime.getRuntime().exec("taskkill /F /IM " + pname);
            process.waitFor();
               */
            System.out.println("Process: " + pname + " terminated");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
