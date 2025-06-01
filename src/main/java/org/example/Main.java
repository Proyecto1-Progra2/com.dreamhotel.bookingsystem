package org.example;

import view.LoginReceptionView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

//    @Override
//    public void start(Stage stage) throws Exception {
//        new MainView(stage);
//    }

    @Override
    public void start(Stage stage) throws Exception {
        new LoginReceptionView(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}