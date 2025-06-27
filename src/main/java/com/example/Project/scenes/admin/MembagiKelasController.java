package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MembagiKelasController {

    @FXML
    private ChoiceBox<String> kelasChoiceBox;
    @FXML
    private ListView<String> unassignedStudentsList;
    @FXML
    private ChoiceBox<String> waliKelasChoice;
    @FXML
    private ListView<String> assignedStudentsList;
    @FXML
    private Label kelasLabel;
    @FXML
    private Label jumlahSiswaLabel;
    @FXML
    private Label waliKelasLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        initializeChoiceBox();
        update();
    }

    @FXML
    void initialize() {
        assignedStudentsList.setItems(FXCollections.observableArrayList());
    }

    void update(){
        ObservableList<String> unassigned = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM siswa WHERE id_kelas IS NULL ORDER BY nama_siswa");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                unassigned.add("(" + rs.getString("nomor_induk_siswa") + ") - " + rs.getString("nama_siswa"));
            }
            unassignedStudentsList.setItems(unassigned);
        }catch (SQLException e){
            System.out.println("Error update: " + e);
        }
    }

    void initializeChoiceBox(){
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT nama_kelas FROM kelas ORDER BY nama_kelas");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
            kelasChoiceBox.setItems(kelasList);
        }catch (SQLException e){
            System.out.println("Error initializeChoiceBox: " + e);
        }
    }



    @FXML
    void onSimpanWaliKelasClicked() {
        String selectedWali = waliKelasChoice.getValue();
        String selectedKelas = kelasChoiceBox.getValue();

        if (selectedWali == null || selectedKelas == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas dan calon wali kelas terlebih dahulu.");
            return;
        }

        try (Connection data = MainDataSource.getConnection()) {
            data.setAutoCommit(false);

            PreparedStatement stmt = data.prepareStatement("UPDATE wali_kelas SET id_kelas = NULL WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)");
            stmt.setString(1, selectedKelas);
            stmt.executeUpdate();

            stmt = data.prepareStatement("UPDATE wali_kelas SET id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) WHERE nip_guru = (SELECT nip FROM guru WHERE nama_guru = ?)");
            stmt.setString(1, selectedKelas);
            stmt.setString(2, selectedWali);
            stmt.executeUpdate();

            data.commit();

            waliKelasLabel.setText(selectedWali);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Wali kelas telah berhasil diperbarui.");
        } catch (SQLException e) {
            System.out.println("Error SimpanWali: " + e);
            showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi kesalahan saat menyimpan data wali kelas.");
        }
    }

    @FXML
    void onSimpanClicked() {
        if (kelasChoiceBox.getValue() == null){
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas terlebih dahulu sebelum menyimpan.");
            return;
        }

        try (Connection data = MainDataSource.getConnection()) {
            data.setAutoCommit(false);
            String idKelas = "(SELECT id_kelas FROM kelas WHERE nama_kelas = '" + kelasChoiceBox.getValue() + "')";

            for (String siswaInfo : assignedStudentsList.getItems()) {
                String nis = getNis(siswaInfo);
                PreparedStatement stmt = data.prepareStatement("UPDATE siswa SET id_kelas = " + idKelas + " WHERE nomor_induk_siswa = ?");
                stmt.setString(1, nis);
                stmt.executeUpdate();
            }
            for (String siswaInfo : unassignedStudentsList.getItems()) {
                String nis = getNis(siswaInfo);
                PreparedStatement stmt = data.prepareStatement("UPDATE siswa SET id_kelas = NULL WHERE nomor_induk_siswa = ?");
                stmt.setString(1, nis);
                stmt.executeUpdate();
            }
            data.commit();

            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pembagian kelas siswa telah berhasil disimpan.");
        } catch (SQLException e) {
            System.out.println("Error SimpanClicked: " + e);
            showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi kesalahan saat menyimpan pembagian kelas.");
        }
    }

    @FXML
    void onResetClicked() {
        update();
        kelasChoiceBox.setValue(null);
        waliKelasChoice.setItems(null);
        assignedStudentsList.getItems().clear();
        kelasLabel.setText("Nama Kelas");
        jumlahSiswaLabel.setText("0");
        waliKelasLabel.setText("Belum Dipilih");
    }

    @FXML
    void onTambahkanClicked() {
        String selectedSiswa = unassignedStudentsList.getSelectionModel().getSelectedItem();
        if (selectedSiswa != null) {
            assignedStudentsList.getItems().add(selectedSiswa);
            unassignedStudentsList.getItems().remove(selectedSiswa);
            jumlahSiswaLabel.setText(String.valueOf(assignedStudentsList.getItems().size()));
        }
    }

    @FXML
    void onKembalikanClicked() {
        String selectedSiswa = assignedStudentsList.getSelectionModel().getSelectedItem();
        if (selectedSiswa != null){
            unassignedStudentsList.getItems().add(selectedSiswa);
            assignedStudentsList.getItems().remove(selectedSiswa);
            jumlahSiswaLabel.setText(String.valueOf(assignedStudentsList.getItems().size()));
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-view.fxml"));
            Parent root = loader.load();
            AdminViewController adminController = loader.getController();
            adminController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private String getNis(String s){
        if (s == null || !s.contains("(") || !s.contains(")")) {
            return "";
        }
        return s.substring(s.indexOf('(') + 1, s.indexOf(')'));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}