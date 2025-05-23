package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Processes.Program;

public class CreateUserController {

    private Program program;

    @FXML
    private TextField userNameField;

    public void setProgram(Program program) {
        this.program = program;
    }

    @FXML
    public void createUser() {
        String userName = userNameField.getText();
        if (program != null && program.db.createUser(userName)) {
            System.out.println("User created");
        } else {
            System.out.println("User already exists or program null");
        }
    }
}
