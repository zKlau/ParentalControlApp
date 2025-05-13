package GUI;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainUI extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("ParentalControlApp");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
