package com.example.Project.scenes.walikelas;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import com.example.Project.scenes.guru.GuruAbsensiController;
import com.example.Project.scenes.guru.InputNilaiController;
import com.example.Project.scenes.guru.JadwalKelasController;
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

public class WaliKelasViewController {
    @FXML
    private Label waliKelasNameLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        updateNameLabel();
    }

    private void updateNameLabel() {
        if (user.id != null) {
            try(Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM guru WHERE nip = ?");
                stmt.setString(1, user.id);
                // Execute the query
                ResultSet rs = stmt.executeQuery();
                if (rs.next()){
                    user.username = rs.getString("nama_guru");
                    waliKelasNameLabel.setText("Wali Kelas: "+user.username);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @FXML
    void onJadwalKelasClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Jadwal Kelas");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-jadwalKelas.fxml"));
            Parent root = loader.load();

            JadwalKelasController jadwalKelasController = loader.getController();
            jadwalKelasController.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    @FXML
    void onMasukkanNilaiClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Input Nilai");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-inputNilai.fxml"));
            Parent root = loader.load();

            InputNilaiController inputNilaiController = loader.getController();
            inputNilaiController.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onCetakRaporClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Cetak Rapor");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-walikelasCetakRapor.fxml"));
            Parent root = loader.load();

            CetakRaporController cetakRaporController = loader.getController();
            cetakRaporController.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    @FXML
    void onLogOutClicked(){
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
    @FXML
    void onAbsenSiswaClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Absensi Siswa");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-absensi.fxml"));
            Parent root = loader.load();

            GuruAbsensiController guruAbsensiController = loader.getController();
            guruAbsensiController.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
