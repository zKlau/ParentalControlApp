package GUI;


import javafx.fxml.FXML;

public class processEditController{
    public void print() {
        System.out.println("Editict Controller");
    }

    @FXML
    public void saveProcess() {
        System.out.println("Saving Process");
    }

    @FXML
    public void removeProcess() {
        System.out.println("Removing Process");
    }
}
