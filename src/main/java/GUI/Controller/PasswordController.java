package GUI.Controller;

import java.awt.SystemTray;

import GUI.MainUI;
import org.tinylog.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

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


    private double xOffset = 0;
    private double yOffset = 0;
    private Stage primaryStage;


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private HBox topBarPane;

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
        Logger.info("Verifying Password");
        if (mainApp.getProgram().db.checkPassword(passwordField.getText())) {
            mainApp.hidePINWindowAndShowMain();
            passwordField.setText("");
        }
    }
    /**
     * Handles mouse press event for window dragging.
     */
    @FXML
    public void windowPressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    /**
     * Handles mouse drag event for window dragging.
     */
    @FXML
    public void windowDragged(javafx.scene.input.MouseEvent event) {
        Stage stage = (Stage) ((HBox) event.getSource()).getScene().getWindow();
        if (!stage.isMaximized()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    /**
     * Minimizes the main application window.
     */
    @FXML
    public void minimizeWindow() {
        primaryStage.setIconified(true);
    }

    private boolean isMaximized = false;
    private double prevX, prevY, prevWidth, prevHeight;

    /**
     * Maximizes or restores the main application window.
     */
    @FXML
    public void maximizeWindow() {
        if (!isMaximized) {
            prevX = primaryStage.getX();
            prevY = primaryStage.getY();
            prevWidth = primaryStage.getWidth();
            prevHeight = primaryStage.getHeight();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());

            isMaximized = true;
        } else {
            primaryStage.setX(prevX);
            primaryStage.setY(prevY);
            primaryStage.setWidth(prevWidth);
            primaryStage.setHeight(prevHeight);

            isMaximized = false;
        }
    }

    /**
     * Hides the main application window.
     */
    @FXML
    public void closeWindow() {
        primaryStage.hide();
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
