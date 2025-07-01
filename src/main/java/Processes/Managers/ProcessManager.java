package Processes.Managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import org.tinylog.Logger;

import Processes.ProcessInfo;
import db.Database;

public class ProcessManager {
    private final Database db;

    
    private static final Set<String> WINDOWS_SYSTEM_PROCESSES = Set.of(
            "System Idle Process", "System", "smss.exe", "csrss.exe", "wininit.exe", "services.exe",
            "lsass.exe", "winlogon.exe", "explorer.exe", "spoolsv.exe", "dwm.exe",
            "taskhostw.exe", "fontdrvhost.exe", "registry", "conhost.exe", "rundll32.exe", "audiodg.exe",
            "WmiPrvSE.exe", "SearchIndexer.exe", "SearchUI.exe", "RuntimeBroker.exe", "SgrmBroker.exe",
            "StartMenuExperienceHost.exe", "ShellExperienceHost.exe", "SecurityHealthSystray.exe",
            "msmpeng.exe", "NisSrv.exe", "ctfmon.exe", "sihost.exe", "backgroundTaskHost.exe",
            "AppVShNotify.exe", "AppVClient.exe", "Image", "Secure", "Registry", "Memory"
    );

    public ProcessManager(Database db) {
        this.db = db;
    }
    /**
     * Tracks all currently running user processes for the specified user.
     *
     * @param current_user The ID of the user for whom processes are being tracked.
     */
    public void trackAllProcesses(int current_user) {
        try {
            BufferedReader prs = getRunningProcesses();
            String line;
            while ((line = prs.readLine()) != null) {
                String[] parts = line.split(" ");
                String processName = parts[0];

                if (WINDOWS_SYSTEM_PROCESSES.contains(processName) || !processName.contains(".exe")) {
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
     * Returns a BufferedReader for the list of currently running processes.
     *
     * @return A BufferedReader for the running processes output.
     */
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
