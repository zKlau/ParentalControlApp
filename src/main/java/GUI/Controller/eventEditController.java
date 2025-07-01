package GUI.Controller;

import Events.EventInfo;
import Processes.Program;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The {@code eventEditController} class manages the editing and creation of events
 * in the application's user interface. It provides logic for populating event types,
 * handling user input, and updating or removing events in the database.
 * <p>
 * This controller is intended for use with a JavaFX FXML view for event editing.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class eventEditController {

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
     * The currently selected event type.
     */
    private String current_type;

    /**
     * The event type selected by the user.
     */
    private String event_type;

    /**
     * Text field for entering the hour component of the event time.
     */
    @FXML
    private TextField hour;

    /**
     * Text field for entering the minute component of the event time.
     */
    @FXML
    private TextField minute;

    /**
     * The {@link EventInfo} object being edited or created.
     */
    public EventInfo evt;

    /**
     * Returns the current {@link EventInfo} object.
     *
     * @return The event being edited or created.
     */
    public EventInfo getEvent() {
        return evt;
    }

    /**
     * Menu for selecting the type of event.
     */
    @FXML
    private MenuButton typeMenu;

    /**
     * Menu for selecting the event type.
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
     * Populates the event type menu with available event types.
     */
    public void populateLists() {
        ArrayList<String> events = new ArrayList<>(Arrays.asList("Computer Shutdown", "User System logout", "Screenshot"));
        event_type = events.getFirst();
        for (String type : events) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                event_type = type;
                event_type_text.setText(type);
            });
            eventTypeMenu.getItems().add(item);
        }
        event_type_text.setText(events.getFirst());
    }

    /**
     * Checkbox for selecting if the event should run at a specific time.
     */
    @FXML
    private CheckBox runningAtCheckbox;

    /**
     * Checkbox for selecting if the event should run after a specific time.
     */
    @FXML
    private CheckBox runningAfterCheckbox;

    /**
     * Updates the checkbox time
     */
    @FXML
    public void updateButtonText() {
        System.out.println("updated");
        int hours = hour.getText().isBlank() ? 0 : Integer.parseInt(hour.getText());
        int minutes = minute.getText().isBlank() ? 0 : Integer.parseInt(minute.getText());
        runningAtCheckbox.setText("Event running at " + hours + ":" + minutes+ "");
        runningAfterCheckbox.setText("Event running after " + hours + "h and " + minutes + "m?");
    }

    /**
     * Checkbox for selecting if the event should repeat.
     */
    @FXML
    private CheckBox RepeatCheckbox;

    /**
     * Label displaying the selected event type.
     */
    @FXML
    private Label event_type_text;

    /**
     * Sets the event to be edited and populates the UI fields accordingly.
     *
     * @param evt The {@link EventInfo} to edit.
     */
    public void setEvent(EventInfo evt) {
        populateLists();
        if (evt.getId() == -1) {
            hour.setText("0");
            minute.setText("0");
            return;
        }
        this.evt = evt;
        hour.setText(Integer.toString(evt.getTime() / 60));
        minute.setText(Integer.toString(evt.getTime() % 60));
        RepeatCheckbox.setSelected(evt.isRepeat());
        runningAtCheckbox.setText("Event running at " + evt.getTime() / 60 + ":" + evt.getTime() % 60 + "");
        runningAfterCheckbox.setText("Event running after " + evt.getTime() / 60 + "h and " + evt.getTime() % 60 + "m?");
        runningAtCheckbox.setSelected(evt.isBefore_at());
        runningAfterCheckbox.setSelected(!evt.isBefore_at());
        event_type = evt.getEvent_name();
        event_type_text.setText(event_type);
        eventTypeMenu.setText(event_type);
    }

    /**
     * Saves the event based on the current UI input.
     * If creating a new event, adds it to the database.
     * If editing, updates the existing event in the database.
     * Closes the window after saving.
     */
    @FXML
    public void saveEvent() {
        if (evt == null && !hour.getText().isBlank()) {
            int hours = hour.getText().isBlank() ? 0 : Integer.parseInt(hour.getText());
            int minutes = minute.getText().isBlank() ? 0 : Integer.parseInt(minute.getText());
            int time = hours * 60 + minutes;
            boolean before_at = runningAtCheckbox.isSelected();
            EventInfo newEvent = new EventInfo(0, program.current_user, event_type, time, before_at, isRepeating);
            program.db.addEvent(newEvent);
        } else {
            int hours = hour.getText().isBlank() ? 0 : Integer.parseInt(hour.getText());
            int minutes = minute.getText().isBlank() ? 0 : Integer.parseInt(minute.getText());
            int time = hours * 60 + minutes;
            boolean before_at = runningAtCheckbox.isSelected();
            System.out.println(isRepeating);
            evt.setRepeat(RepeatCheckbox.isSelected());
            evt.setTime(time);
            evt.setBefore_at(before_at);
            evt.setEvent_name(event_type);
            program.db.updateEvent(evt);
        }
        Stage stage = (Stage) runningAfterCheckbox.getScene().getWindow();
        stage.close();
    }

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
     * Tracks whether the event is set to repeat.
     */
    private boolean isRepeating = false;

    /**
     * Toggles the repeat status of the event.
     */
    @FXML
    public void selectRepeat() {
        isRepeating = !isRepeating;
        System.out.println("repeat");
    }

    /**
     * Removes the current event from the database and closes the window.
     */
    @FXML
    public void removeEvent() {
        program.db.removeEvent(evt);
        Stage stage = (Stage) runningAfterCheckbox.getScene().getWindow();
        stage.close();
    }

    /**
     * Prints a debug message indicating the controller is active.
     */
    public void print() {
        System.out.println("Editing Controller");
    }
}
