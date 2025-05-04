package detection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Program {
    public static void main(String[] args) {
        String pname = "msedge.exe";

        boolean processRunning;
        while(true) {
            System.out.println("Checking process " + pname);
            processRunning = isProcessRunning(pname);
            if (processRunning) {
                terminateProcess(pname);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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
