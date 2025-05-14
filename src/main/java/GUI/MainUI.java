package GUI;

import detection.Program;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class MainUI extends Application {

    private Program program; // keep a reference if needed

    @Override
    public void start(Stage stage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(root);

        stage.setTitle("ParentalControlApp");
        stage.setScene(scene);
        stage.show();

        Thread backgroundThread = new Thread(() -> {
            Program p = new Program();
        });
        backgroundThread.setDaemon(true);
        backgroundThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down app...");
        }));
    }

    public static void main(String[] args) {
        launch();
    }
}
