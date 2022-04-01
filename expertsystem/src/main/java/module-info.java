module expertsystem {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    opens project to javafx.fxml;
    exports project;
    exports project.controllers;
}
