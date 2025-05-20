package GUI;

import Processes.Program;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class MainUI extends Application {

    private UI controller;


    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setTitle("ParentalControlApp");
        stage.setScene(scene);
        stage.show();

        new Thread(() -> {
            Program program = new Program();
            controller.onProgramReady(program);
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down app...");
        }));
    }

    public static void main(String[] args) { launch(); }
}
