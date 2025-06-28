package com.example.Project.scenes.siswa;

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

public class SiswaViewController {

    @FXML
    private Label nameLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        updateNameLabel();
    }
    private void updateNameLabel() {
        try (Connection data = MainDataSource.getConnection()){
            if (user.id != null) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM siswa WHERE nomor_induk_siswa = ?");
                stmt.setString(1, user.id);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    user.username = rs.getString("nama_siswa");
                    nameLabel.setText("Selamat datang, "+user.username);
                }
            }
        }catch (SQLException e){
            System.out.println("Error updateNameLabelSQL");
        }
    }
    @FXML
    void initialize(){
        updateNameLabel();
    }
    @FXML
    void onBiodataButtonClicked(){
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa Biodata");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-biodata.fxml"));
            Parent root = loader.load();
            SiswaBiodataController siswaBiodataController = loader.getController();
            siswaBiodataController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    void onScheduleButtonClicked(){
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa Schedule");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-jadwal.fxml"));
            Parent root = loader.load();
            SiswaJadwalController siswaJadwalController = loader.getController();
            siswaJadwalController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    void onGradeButtonClicked(){
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa Grade");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-grade.fxml"));
            Parent root = loader.load();
            SiswaGradeController siswaGradeController = loader.getController();
            siswaGradeController.setUser(user );
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onExtracurricularButtonClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Pendaftaran Ekstrakurikuler");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-pendaftaran-eskul.fxml"));
            Parent root = loader.load();

            SiswaPendaftaranEkskulController controller = loader.getController();
            controller.setUser(this.user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            System.err.println("Gagal memuat halaman pendaftaran ekstrakurikuler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onAbsensiButtonClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Riwayat Absensi");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-absensi.fxml"));
            Parent root = loader.load();

            SiswaAbsensiController controller = loader.getController();
            controller.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onBackToLoginClicked(){
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
