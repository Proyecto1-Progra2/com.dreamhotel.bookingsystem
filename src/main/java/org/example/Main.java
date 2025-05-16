package org.example;

import view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new MainView(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}