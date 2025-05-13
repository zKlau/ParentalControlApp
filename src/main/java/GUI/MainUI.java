package GUI;

import detection.Program;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.spi.FileTypeDetector;

public class MainUI extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("ParentalControlApp");
        stage.show();
    }

    public static void main(String[] args) {
        Program p = new Program();
        launch();


    }
}
