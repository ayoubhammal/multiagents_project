package project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation((this.getClass().getResource("/fxml/main_pane.fxml")));
        ScrollPane mainPane = loader.<ScrollPane>load();

        Scene scene = new Scene(mainPane);
        scene.getStylesheets().add(this.getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("Expert System");
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }

}
