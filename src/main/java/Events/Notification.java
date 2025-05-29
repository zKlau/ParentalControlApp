package Events;

import java.awt.*;

public class Notification {
    public static void main(String[] args) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("src/main/resources/Images/icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "Notification");
                tray.add(trayIcon);
                trayIcon.displayMessage("Title", "message", TrayIcon.MessageType.INFO);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }
}
