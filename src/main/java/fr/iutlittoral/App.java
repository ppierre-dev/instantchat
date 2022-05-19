package fr.iutlittoral;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var messaging = new Messaging();
        var scene = new Scene(new StackPane(messaging), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Messaging");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}