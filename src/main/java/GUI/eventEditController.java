package GUI;


import Events.EventInfo;
import Processes.ProcessInfo;
import Processes.Program;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class eventEditController{
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
    private TextField hour;
    @FXML
    private TextField minute;

    public EventInfo evt;

    public EventInfo getEvent() {
        return evt;
    }


    @FXML
    private MenuButton typeMenu;
    @FXML
    private MenuButton eventTypeMenu;
    @FXML
    private Group ProcessGroup;
    @FXML
    private Group EventGroup;

    public void populateLists() {
        ArrayList<String> events = new ArrayList<>(Arrays.asList("Computer Shutdown","User System logout", "Screenshot"));
        event_type = events.getFirst();
        for(String type : events) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                event_type = type;
                event_type_text.setText(type);
            });
            eventTypeMenu.getItems().add(item);
        }
        event_type_text.setText(events.getFirst());
    }
    @FXML
    private CheckBox runningAtCheckbox;
    @FXML
    private CheckBox runningAfterCheckbox;
    @FXML
    private CheckBox RepeatCheckbox;
    @FXML
    private Label event_type_text;
    public void setEvent(EventInfo evt) {
        populateLists();
        if (evt.getId() == -1) {
            hour.setText("0");
            minute.setText("0");
            return;
        }
        this.evt = evt;
        hour.setText(Integer.toString(evt.getTime()/60));
        minute.setText(Integer.toString(evt.getTime()%60));
        RepeatCheckbox.setSelected(evt.isRepeat());
        runningAtCheckbox.setText("Event running at " + evt.getTime() / 60 + ":" + evt.getTime() % 60 + "");
        runningAfterCheckbox.setText("Event running after " + evt.getTime() / 60 + "h and " + evt.getTime() % 60 + "m?");
        runningAtCheckbox.setSelected(evt.isBefore_at());
        runningAfterCheckbox.setSelected(!evt.isBefore_at());
        event_type_text.setText(evt.getEvent_name());
        System.out.println("Merge");
    }
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
            //EventInfo newEvent = new EventInfo(, program.current_user, event_type, time,before_at, isRepeating);
            program.db.updateEvent(evt);
        }
        Stage stage = (Stage) runningAfterCheckbox.getScene().getWindow();
        stage.close();
    }


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
    public void removeEvent() {
        program.db.removeEvent(evt);
        Stage stage = (Stage) runningAfterCheckbox.getScene().getWindow();
        stage.close();
    }

}
