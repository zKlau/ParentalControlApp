package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Processes.Program;
import javafx.stage.Stage;

/**
 * The {@code CreateUserController} class handles the logic for creating a new user
 * via the application's user interface. It interacts with the {@link Program} and {@link UI}
 * classes to add users to the database and update the UI accordingly.
 * <p>
 * This controller is intended for use with a JavaFX FXML view.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class CreateUserController {

    /**
     * Reference to the main program logic.
     */
    private Program program;

    /**
     * Reference to the UI controller for updating the menu after user creation.
     */
    private UI uiController;

    /**
     * Sets the UI controller reference.
     *
     * @param uiController The UI controller to set.
     */
    public void setUIController(UI uiController) {
        this.uiController = uiController;
    }

    /**
     * Text field for entering the new user's name (injected by FXML).
     */
    @FXML
    private TextField userNameField;

    /**
     * Sets the program reference.
     *
     * @param program The {@link Program} instance to set.
     */
    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * Handles the creation of a new user when triggered from the UI.
     * If the username is valid and does not already exist, the user is added to the database,
     * the menu is updated, and the window is closed.
     * Otherwise, an error message is printed.
     */
    @FXML
    public void createUser() {
        System.out.println("Creating user");
        String userName = userNameField.getText();
        if (!userName.isBlank() && program != null && program.db.createUser(userName, () -> {
            if (uiController != null) {
                uiController.updateMenu();
            }
        })) {
            
            
            Stage stage = (Stage) userNameField.getScene().getWindow();
            stage.close();
            System.out.println("User created");
        } else {
            System.out.println("User already exists or program null");
        }
    }
}
