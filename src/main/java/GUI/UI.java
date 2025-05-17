package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import detection.Program;

public class UI {
    Program program;




    @FXML
    private ListView<String> processes;


    public void onProgramReady(Program program) {
        //program.allow_connection = false;
        ArrayList<String> prs = program.db.getProcesses(0);
        System.out.println(prs.toString());

        javafx.application.Platform.runLater(() -> {
            processes.getItems().setAll(prs);
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
