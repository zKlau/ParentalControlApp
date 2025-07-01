package GUI;

import GUI.Controller.PasswordController;
import Processes.ProcessInfo;
import Processes.Program;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.tinylog.Logger;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * The {@code MainUI} class serves as the main entry point for the ParentalControlApp.
 * It handles the initialization of the application's user interface, the connection
 * to the backend logic, and the overall lifecycle of the application.
 * <p>
 * This class manages the display of the PIN entry window, the main application window,
 * and the system tray integration for minimizing the app.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class MainUI extends Application {

    /**
     * Reference to the {@code UI} controller that facilitates interaction between
     * the UI components and the backend logic.
     */
    private UI controller;

    /**
     * Reference to the main {@link Program} logic.
     */
    private Program program;

    /**
     * The stage used for the PIN entry window.
     */
    private Stage pinStage;

    /**
     * The application's tray icon for system tray integration.
     */
    public TrayIcon trayIcon;


@Override
public void init() throws Exception {
    // This runs before start() and before any GUI is shown
    this.program = new Program();
    // You can do other setup here
}
    /**
     * Starts the JavaFX application by loading the primary FXML file, initializing
     * the {@code UI} controller, and setting up the main application window.
     *
     * @param stage The primary stage for this application, which contains the main window.
     * @throws Exception if an error occurs during the loading of the FXML file or other startup processes.
     */
    @Override
    public void start(Stage stage) throws Exception {
        //this.program = new Program();
        System.setProperty("java.awt.headless", "false");
        //displayMainWindow();

        new Thread(() -> {
            Platform.runLater(() -> {
                //controller.onProgramReady(program);
                for (ProcessInfo p : program.db.getURLS(program.user)) {
                    program.webFilter.blockSite(p.getProcess_name());
                };
            });
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.warn("Shutting down app...");
            program.webFilter.unblockSites(program.db.getURLS(program.user));
        }));
        displayPIN(stage);
    }

    /**
     * Returns the main {@link Program} instance.
     *
     * @return The {@link Program} instance.
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Displays the PIN entry window for authentication.
     *
     * @param stage The stage to use for the PIN window.
     */
    public void displayPIN(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pinMenu.fxml"));
            Parent root = loader.load();
            this.pinStage = stage;

            Object controller = loader.getController();
            if (controller instanceof PasswordController passController) {
                passController.setMainApp(this);
                passController.setPrimaryStage(stage);
            }

            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setTitle("ParentalControlApp");
            stage.setScene(scene);
            Platform.setImplicitExit(false);
            stage.setResizable(false);
            addAppToTray(stage);
            stage.show();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Displays the main application window after successful authentication.
     *
     * @throws IOException if an error occurs during loading the main FXML.
     */
    public void displayMainWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        controller = loader.getController();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("ParentalControlApp");
        stage.setScene(scene);
        stage.show();
        Platform.setImplicitExit(false);
        ResizeHelper.addResizeListener(stage, (Region) root);

        controller.onProgramReady(program);
/*
        new Thread(() -> {
            Platform.runLater(() -> {
                controller.onProgramReady(program);
                for (ProcessInfo p : controller.program.db.getURLS(controller.program.user)) {
                    controller.program.webFilter.blockSite(p.getProcess_name());
                }
            });
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down app...");
            controller.program.webFilter.unblockSites(controller.program.db.getURLS(controller.program.user));
        }));*/
    }

    /**
     * The main method serves as the entry point for the application.
     * It delegates execution to the JavaFX platform's {@code launch()} method.
     *
     * @param args Command-line arguments passed to the program.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Invoked when the application is stopping. This method is used to perform
     * cleanup actions.
     *
     * @throws Exception if an error occurs during the shutdown process.
     */
    @Override
    public void stop() throws Exception {
        Logger.warn("Application is stopping...");
        System.exit(0);
    }

    /**
     * Adds the application to the system tray and sets up the tray icon menu.
     *
     * @param stage The main application stage.
     */
    private void addAppToTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            Logger.warn("System tray not supported!");
            return;
        }

        try {
            Toolkit.getDefaultToolkit();

            if (trayIcon != null) {
                Logger.warn("Tray icon already exists");
                return;
            }

            URL imageUrl = getClass().getResource("/Images/icon.png");
            Image image = Toolkit.getDefaultToolkit().getImage(imageUrl);

            final PopupMenu popup = new PopupMenu();
            trayIcon = new TrayIcon(image, "ParentalControlApp", popup);
            trayIcon.setImageAutoSize(true);

            MenuItem openItem = new MenuItem("Open");
            openItem.addActionListener(e -> Platform.runLater(() -> {
                if (pinStage != null) {
                    pinStage.show();
                    pinStage.toFront();
                } else {
                    try {
                        Stage newStage = new Stage();
                        //SystemTray.getSystemTray().remove(trayIcon);
                        //Platform.exit();
                        //System.exit(0);
                        displayPIN(newStage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }));

            popup.add(openItem);
            popup.addSeparator();

            SystemTray.getSystemTray().add(trayIcon);

            Platform.runLater(() -> {
                stage.setOnCloseRequest(event -> {
                    event.consume();
                    stage.hide();
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hides the PIN window and displays the main application window.
     */
    public void hidePINWindowAndShowMain() {
        if (pinStage != null) {
            pinStage.hide();
        }

        Platform.runLater(() -> {
            try {
                displayMainWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles PIN verification and transitions to the main application window.
     */
    @FXML
    public void verifyPassword() {
        Logger.info("Verifying PIN");
        hidePINWindowAndShowMain();
    }
}