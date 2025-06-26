package com.example.bdsqltester;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenu extends Application {
    private static MainMenu applicationInstance;
    private Stage primaryStage;
    public static MainMenu getApplicationInstance () { return applicationInstance; }
    public Stage getPrimaryStage () { return primaryStage; }

    @Override
    public void start(Stage stage) throws IOException {
        MainMenu.applicationInstance = this;

        FXMLLoader fxmlLoader = new FXMLLoader(MainMenu.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage = stage;

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
     // If reflection for FXML is only needed:
     // opens com.example.bdsqltester.scenes.admin to javafx.fxml;

     // Or if the package API is public:
     // exports com.example.bdsqltester.scenes.admin;