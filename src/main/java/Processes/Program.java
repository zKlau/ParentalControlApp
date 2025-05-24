package Processes;

import db.Database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class Program {
    public Database db = Database.getInstance();
    public boolean allow_connection = true;
    public String system_user = System.getProperty("user.name");
    public Timer timer;


    public Program() {
        db.addProcess("msedge.exe", 0);
        db.setTimeLimit(1, 50);
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    String processName = "msedge.exe";
                    System.out.println("Checking process " + processName);

                    if (isProcessRunning(processName)) {
                        db.updateTime(1);
                        if (db.getTime(1) > db.getTimeLimit(1)) {
                            terminateProcess(processName);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error during process monitoring: " + e.getMessage());
                }
            }
        }, 0, 2000);
    }

    public static boolean isProcessRunning(String pname) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = buffer.readLine()) != null) {
                if (line.toLowerCase().startsWith(pname.toLowerCase())) {
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
            Process process = Runtime.getRuntime().exec("taskkill /F /IM " + pname);
            process.waitFor();

            System.out.println("Process: " + pname + " terminated");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
