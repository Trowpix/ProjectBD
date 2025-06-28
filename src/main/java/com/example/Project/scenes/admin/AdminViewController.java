package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminViewController {

    @FXML
    private Label adminNameLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        update();
    }

    @FXML
    void initialize() {
    }

    void update() {
        try(Connection data = MainDataSource.getConnection()) {
            if (user.id != null) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM admins WHERE admin_code = ?");
                stmt.setString(1, user.id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    user.username = rs.getString("nama_admin");
                    adminNameLabel.setText("Admin: " + user.username);
                }
            }
        } catch (SQLException e) {
            System.out.println("Update Failed");
        }
    }

    @FXML
    void onInputDataSiswaClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Input Data Siswa");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-inputDataSiswa.fxml"));
            Parent root = loader.load();
            InputDataSiswaController inputDataSiswaController = loader.getController();
            inputDataSiswaController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onInputDataGuruClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Input Data Guru");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-inputDataGuru.fxml"));
            Parent root = loader.load();
            InputDataGuruController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onInputJadwalKelasClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Input Jadwal Kelas");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-inputJadwalKelas.fxml"));
            Parent root = loader.load();
            InputJadwalKelasController inputJadwalKelasController = loader.getController();
            inputJadwalKelasController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onBagiKelasClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Pembagian Kelas");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-membagiKelas.fxml"));
            Parent root = loader.load();
            MembagiKelasController membagiKelasController = loader.getController();
            membagiKelasController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onDeleteUserClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Hapus Pengguna");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-deleteUser-menu.fxml"));
            Parent root = loader.load();

            DeleteUserMenuController controller = loader.getController();
            controller.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onLogOutClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MainMenu.class.getResource("login-styles.css").toExternalForm());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}