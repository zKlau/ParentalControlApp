package Processes;

public class ProcessManager {


    public void trackAllProcesses() {
        try {
            BufferedReader prs = getRunningProcesses();
            String line;
            while ((line = prs.readLine()) != null) {
                String[] parts = line.split(" ");
                String processName = parts[0];

                if (WINDOWS_SYSTEM_PROCESSES.contains(processName) || processName.contains(".exe") || processName.contains(".msi")) {
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
    
}
