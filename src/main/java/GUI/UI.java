package GUI;

import Events.EventInfo;
import Processes.ProcessInfo;
import Processes.UserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import Processes.Program;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import jdk.jfr.Event;

/**
 * The {@code UI} class is responsible for managing the graphical user interface logic of the application.
 * It is integrated with JavaFX and communicates with the {@link Program} backend to manage users, processes,
 * and event data for display and editing.
 */
public class UI {

    /**
     * Reference to the {@link Program} instance that provides backend logic.
     */
    Program program;

    /**
     * ListView UI component displaying user-specific process entries.
     */
    @FXML
    private ListView<ProcessInfo> processes;

    /**
     * ListView UI component displaying user-specific event entries.
     */
    @FXML
    private ListView<EventInfo> events;

    @FXML
    private Label currentUser;


    /**
     * MenuButton used to list and select users dynamically.
     */
    @FXML
    private MenuButton selectUsers;

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage primaryStage;

    @FXML
    private HBox topBarPane;
    /**
     * This method is called when the {@link Program} instance is ready and connected.
     * It attempts to load existing users and initialize their associated data (processes/events),
     * or prompts to create a new user if none exist.
     *
     * @param program the {@link Program} instance initialized at application start.
     */
    public void onProgramReady(Program program) {

        this.primaryStage = (Stage) processes.getScene().getWindow();
        this.program = program;
        this.program.ui = this;

        ArrayList<UserInfo> users = program.db.getUsers();
        if (!users.isEmpty()) {
            populateUsersMenu(users);
            populateProgramList(users.getFirst());
            program.allow_connection = true;
            program.user = users.getFirst();
            program.current_user = program.user.getId() - 1;
            currentUser.setText("Current user: " + program.user.getName() + " (" + program.user.getId() + ")");
        } else {
            createUserWindow();
        }


    }


    @FXML
    public void windowPressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void windowDragged(javafx.scene.input.MouseEvent event) {
        Stage stage = (Stage) ((HBox) event.getSource()).getScene().getWindow();
        if (!stage.isMaximized()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    @FXML
    public void minimizeWindow() {
        primaryStage.setIconified(true);
    }

    private boolean isMaximized = false;
    private double prevX, prevY, prevWidth, prevHeight;

    @FXML
    public void maximizeWindow() {
        if (!isMaximized) {
            prevX = primaryStage.getX();
            prevY = primaryStage.getY();
            prevWidth = primaryStage.getWidth();
            prevHeight = primaryStage.getHeight();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());

            isMaximized = true;
        } else {
            primaryStage.setX(prevX);
            primaryStage.setY(prevY);
            primaryStage.setWidth(prevWidth);
            primaryStage.setHeight(prevHeight);

            isMaximized = false;
        }
    }


    @FXML
    public void closeWindow() {
        Platform.exit();
    }

    /**
     * Displays a window for creating a new user.
     */
    public void createUserWindow() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/creatingUser.fxml"));
                Parent root = loader.load();

                CreateUserController controllerUser = loader.getController();
                controllerUser.setProgram(program);
                controllerUser.setUIController(this);

                Stage stage = new Stage();
                stage.setTitle("Creating User");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load user creation window", e);
            }
        });
    }

    /**
     * Populates the {@code processes} and {@code events} ListViews with data belonging to the selected user.
     *
     * @param user the Object of the selected user.
     */
    public void populateProgramList(UserInfo user) {
        int user_id = user.getId() - 1;
        ArrayList<ProcessInfo> prs = program.db.getProcesses(user_id);
        processes.getItems().setAll(prs);
        processes.setCellFactory(listView -> new ListCell<>() {
            private final Button btn = new Button("EDIT");
            private final HBox hbox = new HBox(10);
            private final Label label = new Label();

            {
                hbox.getChildren().addAll(label, btn);
                btn.setOnAction(e -> processEditMenu(getItem()));
            }

            @Override
            protected void updateItem(ProcessInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item.getProcess_name() + "   TIME OPEN: " + item.getTotal_time());
                    setGraphic(hbox);
                }
            }
        });

        ArrayList<EventInfo> evts = program.db.getEvents(user_id);
        events.getItems().setAll(evts);
        events.setCellFactory(listView -> new ListCell<>() {
            private final Button btn = new Button("EDIT");
            private final HBox hbox = new HBox(10);
            private final Label label = new Label();

            {
                hbox.getChildren().addAll(label, btn);
                btn.setOnAction(e -> eventEditMenu(getItem()));
            }

            @Override
            protected void updateItem(EventInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText("Event: " + item.getEvent_name() + "  @ " + item.getTime() / 60 + "H:" + item.getTime() % 60 + "M   R:" + item.isRepeat());
                    setGraphic(hbox);
                }
            }
        });
    }

    /**
     * Opens an editing window for the selected {@link ProcessInfo}. If null, creates a new process.
     *
     * @param item the {@code ProcessInfo} object to be edited or {@code null} to create a new one.
     */
    public void processEditMenu(ProcessInfo item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/processEdit.fxml"));
            Parent root = loader.load();

            processEditController controller = loader.getController();
            controller.print();
            controller.setProgram(program);

            if (item == null) {
                item = new ProcessInfo();
                controller.setProcess(item);
            } else {
                controller.setProcess(item);
            }

            Stage stage = new Stage();
            stage.setTitle(item.getProcess_name() != null ? "Editing " + item.getProcess_name() : "Creating Process");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open process edit window", e);
        }
    }

    /**
     * Placeholder method for editing an {@link EventInfo}.
     *
     * @param item the {@code EventInfo} object to be edited.
     */
    private void eventEditMenu(EventInfo item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eventEdit.fxml"));
            Parent root = loader.load();

            eventEditController controller = loader.getController();
            controller.print();
            controller.setProgram(program);

            if (item == null) {
                item = new EventInfo();
                controller.setEvent(item);
            } else {
                controller.setEvent(item);
            }

            Stage stage = new Stage();
            stage.setTitle(item.getEvent_name() != null ? "Editing " + item.getEvent_name() : "Creating Event");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open process edit window", e);
        }
    }

    /**
     * Refreshes the entire UI, including the user list and associated data.
     */
    @FXML
    public void refreshList() {
        updateMenu();
    }

    /**
     * Updates the users menu and repopulates the ListViews with the current user's data.
     */
    public void updateMenu() {
        populateUsersMenu(null);
        populateProgramList(program.user);
    }



    /**
     * Populates the user selection menu with available users from the database.
     *
     * @param users an optional list of users; if null, they are fetched from the database.
     */
    public void populateUsersMenu(ArrayList<UserInfo> users) {
        if (users == null) {
            users = program.db.getUsers();
        }
        //currentUser.setText("Current user: " + users.get(program.current_user).getName() + " (" + users.get(program.current_user).getId() + ")");
        selectUsers.getItems().clear();

        for (UserInfo user : users) {
            MenuItem item = new MenuItem(user.getName());
            item.setOnAction(e -> {
                program.current_user = user.getId() - 1;
                program.user = user;
                currentUser.setText("Current user: " + user.getName() + " (" + user.getId() + ")" );
                updateMenu();
            });
            selectUsers.getItems().add(item);
        }
    }

    /**
     * Handles the action for adding a new process entry.
     * This will open the process editing window in "create" mode.
     */
    @FXML
    public void addProcess() {
        processEditMenu(null);
    }

    @FXML
    public void addEvent() {
        eventEditMenu(null);
    }

    /**
     * Triggers the user creation window via a button or menu action.
     */
    @FXML
    public void createUser() {
        createUserWindow();
    }

    /**
     * Deletes the currently selected user.
     * Placeholder for delete logic (to be implemented).
     */
    @FXML
    public void deleteUser() {
        program.db.deleteUser(program.user);
        System.out.println("Deleting user");
    }

    /**
     * Displays currently running system processes in the console using the `tasklist` command.
     * Works on Windows systems only.
     */
    @FXML
    public void showRunningProcesses() {
        System.out.println("Showing running processes");
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = buffer.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute tasklist command", e);
        }
    }


    @FXML
    private AnchorPane processGroup;
    @FXML
    private AnchorPane eventsGroup;
    @FXML
    private AnchorPane userGroup;
    @FXML
    public void processesButtonPressed() {
        eventsGroup.setVisible(false);
        processGroup.setVisible(true);
        userGroup.setVisible(false);
    }


    @FXML
    public void eventsButtonPressed() {
        processGroup.setVisible(false);
        eventsGroup.setVisible(true);
        userGroup.setVisible(false);

    }


    @FXML
    public void usersButtonPressed() {
        userGroup.setVisible(true);
        processGroup.setVisible(false);
        eventsGroup.setVisible(false);
    }


}
