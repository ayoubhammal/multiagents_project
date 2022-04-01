module expertsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens project to javafx.fxml;
    exports project;
}
