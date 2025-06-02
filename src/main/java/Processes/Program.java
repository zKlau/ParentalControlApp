package Processes;

import GUI.UI;
import db.Database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Program {
    public Database db = Database.getInstance();
    public boolean allow_connection = true;
    public String system_user = System.getProperty("user.name");
    public Timer timer;
    public int current_user = 0;
    public UserInfo user;
    public UI ui;
    public WebFilter webFilter = new WebFilter();
    public Program() {

        Timer timer = new Timer(true);
        //webFilter.blockSite("www.facebook.com");


        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
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
