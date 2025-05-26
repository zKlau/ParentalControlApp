package GUI;


import Processes.ProcessInfo;
import Processes.Program;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class processEditController{
    public void print() {
        System.out.println("Editing Controller");
    }

    private Program program;
    public void setProgram(Program program) {
        this.program = program;
    }

    @FXML
    private TextField processUrl;

    @FXML
    private TextField time_limit;

    public ProcessInfo prs;

    public ProcessInfo getProcess() {
        return prs;
    }


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
    public void removeProcess() {
        System.out.println("Removing Process");
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
