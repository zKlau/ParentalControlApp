package GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;

import java.awt.*;

public class PasswordController {
    @FXML
    private PasswordField passwordField;

    private MainUI mainApp;
    public void setMainApp(MainUI mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void verifyPassword(){
        System.out.println("Verifying Password");
        if (mainApp.getProgram().db.checkPassword(passwordField.getText())) {
            mainApp.hidePINWindowAndShowMain();
            passwordField.setText("");
        }
    }

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
