package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.ArrayList;
import Processes.Program;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class UI {
    Program program;




    @FXML
    private ListView<String> processes;
    private UI controller;

    public void onProgramReady(Program program) {
        //program.allow_connection = false;
        ArrayList<String> prs = program.db.getProcesses(0);
        System.out.println(prs.toString());
        processes.getItems().setAll(prs);

        processes.setCellFactory(listView -> new ListCell<>() {
            private final Button btn = new Button("EDIT");
            private final HBox hbox = new HBox(10); // spacing
            private final Label label = new Label();

            {
                hbox.getChildren().addAll(label, btn);
                btn.setOnAction(i -> {
                    String item = getItem();
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/processEdit.fxml"));
                        Parent root = loader.load();

                        controller = loader.getController();

                        Scene scene = new Scene(root);
                        Stage stage = new Stage();
                        stage.setTitle("Editing " + item);
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(hbox);
                }
            }
        });
        program.allow_connection = true;
    }

    @FXML
    public void initialize() {

    }
    @FXML
    public void addProcess() {
        System.out.println("Adding Process");
    }
}
