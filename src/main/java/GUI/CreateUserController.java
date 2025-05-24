package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Processes.Program;
import javafx.stage.Stage;

public class CreateUserController {

    private Program program;
    private UI uiController;

    public void setUIController(UI uiController) {
        this.uiController = uiController;
    }
    @FXML
    private TextField userNameField;

    public void setProgram(Program program) {
        this.program = program;
    }

    @FXML
    public void createUser() {
        System.out.println("Creating user");
        String userName = userNameField.getText();
        if (!userName.isBlank() && program != null && program.db.createUser(userName)) {
            if (uiController != null) {
                uiController.updateMenu();
            }
            Stage stage = (Stage) userNameField.getScene().getWindow();
            stage.close();
            System.out.println("User created");
        } else {
            System.out.println("User already exists or program null");
        }
    }
}
