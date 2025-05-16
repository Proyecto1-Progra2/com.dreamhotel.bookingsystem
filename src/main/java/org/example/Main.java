package org.example;

import GUI.VentanaPrincipal;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new VentanaPrincipal(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}