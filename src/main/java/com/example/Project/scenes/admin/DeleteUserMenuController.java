package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import java.io.IOException;

public class DeleteUserMenuController {

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void onDeleteSiswaClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Hapus Data Siswa");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-deleteSiswa.fxml"));
            Parent root = loader.load();
            DeleteSiswaController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onDeleteGuruClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Hapus Data Guru");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-deleteGuru.fxml"));
            Parent root = loader.load();
            DeleteGuruController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin Dashboard");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-view.fxml"));
            Parent root = loader.load();
            AdminViewController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}