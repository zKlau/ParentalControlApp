package GUI;

import Events.EventInfo;
import Processes.ProcessInfo;
import Processes.Program;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The {@code processEditController} class manages the editing and creation of processes
 * in the application's user interface. It provides logic for populating process fields,
 * handling user input, and updating or removing processes in the database.
 * <p>
 * This controller is intended for use with a JavaFX FXML view for process editing.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class processEditController {

    /**
     * Prints a debug message indicating the controller is active.
     */
    public void print() {
        System.out.println("Editing Controller");
    }

    /**
     * Reference to the main program logic.
     */
    private Program program;

    /**
     * Sets the program reference.
     *
     * @param program The {@link Program} instance to set.
     */
    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * The currently selected process type.
     */
    private String current_type;

    /**
     * The event type selected by the user (if applicable).
     */
    private String event_type;

    /**
     * Text field for entering the process URL or name.
     */
    @FXML
    private TextField processUrl;

    /**
     * Text field for entering the process time limit.
     */
    @FXML
    private TextField time_limit;

    /**
     * The {@link ProcessInfo} object being edited or created.
     */
    public ProcessInfo prs;

    /**
     * Returns the current {@link ProcessInfo} object.
     *
     * @return The process being edited or created.
     */
    public ProcessInfo getProcess() {
        return prs;
    }

    /**
     * Menu for selecting the type of process.
     */
    @FXML
    private MenuButton typeMenu;

    /**
     * Menu for selecting the event type (if applicable).
     */
    @FXML
    private MenuButton eventTypeMenu;

    /**
     * Group containing process-related UI elements.
     */
    @FXML
    private Group ProcessGroup;

    /**
     * Group containing event-related UI elements.
     */
    @FXML
    private Group EventGroup;

    /**
     * Sets the process to be edited and populates the UI fields accordingly.
     *
     * @param process The {@link ProcessInfo} to edit.
     */
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

    /**
     * Saves the process based on the current UI input.
     * If creating a new process, adds it to the database and blocks the site if it's a URL.
     * If editing, updates the existing process in the database.
     * Closes the window after saving.
     */
    @FXML
    public void saveProcess() {
        Logger.info("Saving Process");
        if (prs == null && !processUrl.getText().isBlank()) {
            System.out.println("ss");
            ProcessInfo newProcess = new ProcessInfo(0, program.current_user, processUrl.getText(), 0, Integer.parseInt(time_limit.getText()));
            if (processUrl.getText().contains("www.") && processUrl.getText().contains(".com")) {
                program.webFilter.blockSite(processUrl.getText());
            }
            program.db.addProcess(newProcess);
        } else if (prs != null) {
            prs.setProcess_name(processUrl.getText());
            prs.setTime_limit(Integer.parseInt(time_limit.getText()));
            program.db.updateProcess(prs);
        }
        Stage stage = (Stage) processUrl.getScene().getWindow();
        stage.close();
    }

    /**
     * Checkbox for selecting if the process should run at a specific time.
     */
    @FXML
    private CheckBox runningAtCheckbox;

    /**
     * Checkbox for selecting if the process should run after a specific time.
     */
    @FXML
    private CheckBox runningAfterCheckbox;

    /**
     * Handles selection of the "running after" option, ensuring mutual exclusivity.
     */
    @FXML
    public void selectRunningAfter() {
        runningAtCheckbox.setSelected(false);
        System.out.println("running after");
    }

    /**
     * Handles selection of the "running at" option, ensuring mutual exclusivity.
     */
    @FXML
    public void selectRunningAt() {
        runningAfterCheckbox.setSelected(false);
        System.out.println("running at");
    }

    /**
     * Tracks whether the process is set to repeat (if applicable).
     */
    private boolean isRepeating = false;

    /**
     * Toggles the repeat status of the process.
     */
    @FXML
    public void selectRepeat() {
        isRepeating = !isRepeating;
        System.out.println("repeat");
    }

    /**
     * Removes the current process from the database and unblocks the site if it's a URL.
     * Closes the window after removal.
     */
    @FXML
    public void removeProcess() {
        if (processUrl.getText().contains("www.") && processUrl.getText().contains(".com")) {
            program.webFilter.unblockSite(processUrl.getText());
        }
        program.db.removeProcess(prs);
        Stage stage = (Stage) processUrl.getScene().getWindow();
        stage.close();
    }

    /**
     * Opens a file chooser dialog for selecting a process executable or file.
     * Sets the selected file's path in the process URL field.
     */
    @FXML
    public void openFileSelection() {
        Logger.info("Opening File Explorer");
        FileChooser fil_chooser = new FileChooser();
        File file = fil_chooser.showOpenDialog(null);

        if (file != null) {
            String[] path = (file.getAbsolutePath()).split("\\\\");
            System.out.println(path[path.length-1]);
            processUrl.setText(path[path.length-1]);
        }
    }
}
