module it.polimi.ingsw.am48 {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.polimi.ingsw.am48 to javafx.fxml;
    exports it.polimi.ingsw.am48;
}