package GUI;

import Processes.ProcessInfo;
import Processes.Program;
import GUI.ResizeHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

    /**
     * Starts the JavaFX application by loading the primary FXML file, initializing
     * the {@code UI} controller, and setting up the main application window.
     *
     * @param stage The primary stage for this application, which contains the main window.
     * @throws Exception if an error occurs during the loading of the FXML file or other startup processes.
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        stage.initStyle(StageStyle.TRANSPARENT);
        //stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("ParentalControlApp");
        stage.setScene(scene);
        stage.show();
        ResizeHelper.addResizeListener(stage, (Region) root);


        new Thread(() -> {
            Program program = new Program();
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
}