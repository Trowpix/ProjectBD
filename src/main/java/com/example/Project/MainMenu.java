package com.example.Project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenu extends Application {
    private static MainMenu applicationInstance;
    private Stage primaryStage;

    public static MainMenu getApplicationInstance () {
        return applicationInstance;
    }

    public Stage getPrimaryStage () {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        MainMenu.applicationInstance = this;
        this.primaryStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(MainMenu.class.getResource("login-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(MainMenu.class.getResource("login-styles.css").toExternalForm());

        stage.setTitle("Sistem Informasi Sekolah - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}