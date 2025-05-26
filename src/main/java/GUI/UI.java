package GUI;

import Processes.ProcessInfo;
import Processes.UserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import Processes.Program;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * The {@code UI} class is responsible for managing the graphical user interface logic for the application.
 * It is connected to the JavaFX framework and interacts with an instance of {@code Program} to display
 * processes and provide options for editing, adding, and removing them.
 */
public class UI {
    
    /**
     * Reference to the {@code Program} instance that handles backend logic.
     */
    Program program;

    /**
     * ListView in the FXML file to display the processes.
     */
    @FXML
    private ListView<ProcessInfo> processes;

    /**
     * Reference to the {@code UI} controller for handling actions in the UI.
     */

    private UI controller;


    @FXML
    private MenuButton selectUsers;
    /**
     * This method is called when the {@code Program} instance is ready and initialized.
     * It retrieves a list of processes from the program's database, updates the {@code ListView},
     * and sets up a custom cell factory to define behavior for each process entry.
     *
     * @param program The {@code Program} instance that provides the backend logic.
     */
    public void onProgramReady(Program program) {
        this.program = program;
        ArrayList<UserInfo> users = program.db.getUsers();
        System.out.println(users.toString());
        if (!users.isEmpty()) {
            populateUsersMenu(users);
            System.out.println("Users not empty");
            populateProgramList(0);
            program.allow_connection = true;
        }  else {
            createUserWindow();
        }
    }
    public void createUserWindow() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/creatingUser.fxml"));
                Parent root = loader.load();

                CreateUserController controllerUser = loader.getController();
                controllerUser.setProgram(program);
                controllerUser.setUIController(this);
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Creating User");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void populateProgramList(int user_id) {

        ArrayList<ProcessInfo> prs = program.db.getProcesses(user_id);
        processes.getItems().clear();
        processes.getItems().addAll(prs);

        processes.setCellFactory(listView -> new ListCell<>() {
            private final Button btn = new Button("EDIT");
            private final HBox hbox = new HBox(10);
            private final Label label = new Label();

            {
                hbox.getChildren().addAll(label, btn);
                btn.setOnAction(i -> {
                    ProcessInfo proc = getItem();
                    processEditMenu(proc);
                });
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
    }
    public void processEditMenu(ProcessInfo item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/processEdit.fxml"));
            Parent root = loader.load();

            processEditController controller = loader.getController();
            controller.print();
            Scene scene = new Scene(root);
            Stage stage = new Stage();


            if (item != null) {
                stage.setTitle("Editing " + item.getProcess_name());
            } else {
                item = new ProcessInfo();
                stage.setTitle("Creating Process");
            }

            controller.setProcess(item);
            controller.setProgram(program);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void refreshList() {
        updateMenu();
    }
    public void updateMenu() {
        populateUsersMenu(null);
        populateProgramList(program.current_user);
    }
    public void populateUsersMenu(ArrayList<UserInfo> users) {
        if (users == null) {
            users = program.db.getUsers();
        }
        selectUsers.getItems().clear();
        int i = 0;
        for (UserInfo user : users) {
            MenuItem item = new MenuItem(user.getName());
            item.setOnAction(e -> {
                System.out.println("Selected user: " + user.getId());
                program.current_user = user.getId()-1;
                System.out.println(System.getProperty("user.name"));
                updateMenu();
            });
            selectUsers.getItems().add(item);
        }
    }

    /**
     * Called by the JavaFX framework during the FXML loading process. This method is
     * intended for any initialization tasks related to UI components.
     */
    @FXML
    public void initialize() {
    }

    /**
     * Handles the addition of a new process. This method is triggered by a corresponding
     * UI action, such as a button click.
     */

    /**
     * Handles the removal of a selected process. This method is triggered by a corresponding
     * UI action, such as a button click.
     */


    @FXML
    public void addProcess() {
        processEditMenu(null);
    }


    @FXML
    public void createUser() {
        createUserWindow();
    }


    @FXML
    public void deleteUser() {
        System.out.println("Deleting user");
    }
}