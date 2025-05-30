package GUI;


import Events.EventInfo;
import Processes.ProcessInfo;
import Processes.Program;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class processEditController{
    public void print() {
        System.out.println("Editing Controller");
    }

    private Program program;
    public void setProgram(Program program) {
        this.program = program;
    }
    private String current_type;
    private String event_type;
    @FXML
    private TextField processUrl;

    @FXML
    private TextField time_limit;

    public ProcessInfo prs;

    public ProcessInfo getProcess() {
        return prs;
    }





    @FXML
    private MenuButton typeMenu;
    @FXML
    private MenuButton eventTypeMenu;
    @FXML
    private Group ProcessGroup;
    @FXML
    private Group EventGroup;

    public void setProcess(ProcessInfo process) {
        if (process.getId() == -1) {
            time_limit.setText("0");
            return;
        }
        this.prs = process;
        processUrl.setText(process.getProcess_name());
        time_limit.setText(Integer.toString(process.getTime_limit()));
        System.out.println("Merge");
    }

    @FXML
    public void saveProcess() {
            System.out.println("Saving Process");
            if (prs == null && !processUrl.getText().isBlank()) {
                System.out.println("ss");
                ProcessInfo newProcess = new ProcessInfo(0, program.current_user, processUrl.getText(), 0, Integer.parseInt(time_limit.getText()));
                program.db.addProcess(newProcess);
            } else if (prs != null) {
                prs.setProcess_name(processUrl.getText());
                prs.setTime_limit(Integer.parseInt(time_limit.getText()));
                program.db.updateProcess(prs);
            }
    }

    @FXML
    private CheckBox runningAtCheckbox;
    @FXML
    private CheckBox runningAfterCheckbox;


    @FXML
    public void selectRunningAfter() {
        runningAtCheckbox.setSelected(false);
        System.out.println("running after");
    }
    @FXML
    public void selectRunningAt() {
        runningAfterCheckbox.setSelected(false);
        System.out.println("running at");
    }

    private boolean isRepeating = false;
    @FXML
    public void selectRepeat() {
        isRepeating = !isRepeating;
        System.out.println("repeat");
    }


    @FXML
    public void removeProcess() {
        program.db.removeProcess(prs);
    }

    @FXML
    public void openFileSelection() {
        System.out.println("Opening File Explorer");
        FileChooser fil_chooser = new FileChooser();
        File file = fil_chooser.showOpenDialog(null);

        if (file != null) {

            processUrl.setText(file.getAbsolutePath());
        }
    }

}
