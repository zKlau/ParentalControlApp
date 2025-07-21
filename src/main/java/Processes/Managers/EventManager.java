package Processes.Managers;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.tinylog.Logger;

import Events.EventInfo;
import db.Database;

public class EventManager {
    private final Database db;

    public EventManager(Database db) {
        this.db = db;
    }

    /**
     * Executes the specified {@link EventInfo} if its scheduled time has arrived.
     * Handles shutdown, logout, and screenshot events.
     * Removes non-repeating events after execution.
     *
     * @param event The event to run.
     */
    public void runEvent(EventInfo event) {
        Calendar calendar = Calendar.getInstance();
        int currentTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int delayMinutes = event.getTime();
        long createdAtMinutes = event.getCreated_at();
        boolean shouldRun = false;

        if (event.isBefore_at()) {
            if (currentTimeMinutes == delayMinutes) {
                shouldRun = true;
            }
        } else {
            long nowMinutes = System.currentTimeMillis() / 60000L;
            long minutesSinceCreation = nowMinutes - createdAtMinutes;
            if (minutesSinceCreation >= delayMinutes) {
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
                db.eventRepository.removeEvent(event);
            } else {
                if (!event.isBefore_at()) {
                    db.eventRepository.setEventTime(event, (System.currentTimeMillis() / 60000L));
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
}
