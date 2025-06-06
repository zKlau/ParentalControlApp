package GUI;

import Processes.ProcessInfo;
import Processes.Program;
import GUI.ResizeHelper;
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

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * The {@code MainUI} class serves as the main entry point for the ParentalControlApp.
 * It handles the initialization of the application's user interface, the connection
 * to the backend logic, and the overall lifecycle of the application.
 */
public class MainUI extends Application {

    /**
     * Reference to the {@code UI} controller that facilitates interaction between
     * the UI components and the backend logic.
     */
    private UI controller;
    private Program program;
    private Stage pinStage;

    public TrayIcon trayIcon;
    /**
     * Starts the JavaFX application by loading the primary FXML file, initializing
     * the {@code UI} controller, and setting up the main application window.
     *
     * @param stage The primary stage for this application, which contains the main window.
     * @throws Exception if an error occurs during the loading of the FXML file or other startup processes.
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.program = new Program();
        System.setProperty("java.awt.headless", "false");
        displayPIN(stage);

    }

    public Program getProgram() {
        return program;
    }

    public void displayPIN(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pinMenu.fxml"));
            Parent root = loader.load();
            this.pinStage = stage;

            Object controller = loader.getController();
            if (controller instanceof PasswordController passController) {
                passController.setMainApp(this);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setTitle("ParentalControlApp");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void displayMainWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        controller = loader.getController();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        //stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("ParentalControlApp");
        stage.setScene(scene);
        stage.show();
        Platform.setImplicitExit(false);
        ResizeHelper.addResizeListener(stage, (Region) root);
        addAppToTray(stage);

        new Thread(() -> {

            Platform.runLater(() -> {
                controller.onProgramReady(program);
                for(ProcessInfo p : controller.program.db.getURLS(controller.program.user)) {
                    controller.program.webFilter.blockSite(p.getProcess_name());
                }
            });

        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down app...");
            controller.program.webFilter.unblockSites(controller.program.db.getURLS(controller.program.user));
        }));
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
     * @throws Exception if an error occurs during the shutdown process.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Application is stopping...");
        System.exit(0);
    }

    private void addAppToTray(Stage stage) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported!");
            return;
        }

        try {
            Toolkit.getDefaultToolkit();

            if (trayIcon != null) {
                System.out.println("Tray icon already exists");
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

    @FXML
    public void verifyPassword() {
        System.out.println("Verifying PIN");
        hidePINWindowAndShowMain();
    }
}