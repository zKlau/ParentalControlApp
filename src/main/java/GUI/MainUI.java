package GUI;

import detection.Program;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.ArrayList;

public class MainUI extends Application {

    private static Program program;
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
