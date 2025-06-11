package GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;

import java.awt.*;

/**
 * The {@code PasswordController} class manages the logic for verifying the admin password
 * (PIN) in the application's authentication window. It interacts with the {@link MainUI}
 * to transition between the PIN entry and the main application window, and to handle
 * secure application shutdown.
 * <p>
 * This controller is intended for use with a JavaFX FXML view for password entry.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class PasswordController {

    /**
     * The password field for entering the admin PIN (injected by FXML).
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Reference to the main application UI.
     */
    private MainUI mainApp;

    /**
     * Sets the main application reference.
     *
     * @param mainApp The {@link MainUI} instance to set.
     */
    public void setMainApp(MainUI mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Handles password verification for unlocking the main application window.
     * If the password is correct, transitions to the main window and clears the field.
     */
    @FXML
    public void verifyPassword() {
        System.out.println("Verifying Password");
        if (mainApp.getProgram().db.checkPassword(passwordField.getText())) {
            mainApp.hidePINWindowAndShowMain();
            passwordField.setText("");
        }
    }

    /**
     * Handles password verification for closing the application.
     * If the password is correct, removes the tray icon and exits the app.
     */
    @FXML
    public void closeAppVerifyPassword() {
        if (mainApp.getProgram().db.checkPassword(passwordField.getText())) {
            passwordField.setText("");
            SystemTray.getSystemTray().remove(mainApp.trayIcon);
            Platform.exit();
            System.exit(0);
        }
    }
}
